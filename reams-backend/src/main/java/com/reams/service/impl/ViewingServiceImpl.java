package com.reams.service.impl;

import com.reams.common.constant.MessageScenes;
import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.House;
import com.reams.entity.Message;
import com.reams.entity.SysCustomer;
import com.reams.entity.Viewing;
import com.reams.mapper.HouseMapper;
import com.reams.mapper.SysCustomerMapper;
import com.reams.mapper.ViewingMapper;
import com.reams.service.MessageService;
import com.reams.service.ViewingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ViewingServiceImpl implements ViewingService {

    private static final long COMPLETE_GRACE_PERIOD_MILLIS = 30L * 60 * 1000;
    private static final long SYSTEM_ADMIN_ID = 0L;
    private static final int USER_TYPE_ADMIN = 1;
    private static final int USER_TYPE_AGENT = 2;
    private static final int USER_TYPE_CUSTOMER = 3;

    @Autowired
    private ViewingMapper viewingMapper;

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private SysCustomerMapper sysCustomerMapper;

    @Autowired
    private MessageService messageService;

    @Override
    public PageResult<Viewing> getViewingPage(Map<String, Object> params) {
        Integer pageNum = (Integer) params.get("pageNum");
        Integer pageSize = (Integer) params.get("pageSize");
        if (pageNum == null) {
            pageNum = 1;
        }
        if (pageSize == null) {
            pageSize = 10;
        }

        params.put("offset", (pageNum - 1) * pageSize);
        params.put("limit", pageSize);

        List<Viewing> list = viewingMapper.selectPage(params);
        long total = viewingMapper.count(params);
        return PageResult.of(total, pageNum, pageSize, list);
    }

    @Override
    public Result<?> getViewingDetail(Long id) {
        Viewing viewing = viewingMapper.selectById(id);
        if (viewing == null) {
            return Result.error("带看记录不存在");
        }
        return Result.success(viewing);
    }

    @Override
    @Transactional
    public Result<?> bookViewing(Viewing viewing) {
        House house = houseMapper.selectById(viewing.getHouseId());
        if (house == null) {
            return Result.error("房源不存在");
        }
        if (!Integer.valueOf(1).equals(house.getHouseStatus())) {
            return Result.error("房源未发布");
        }

        viewing.setAgentId(house.getAgentId());

        SysCustomer customer = sysCustomerMapper.selectById(viewing.getCustomerId());
        if (customer != null) {
            viewing.setCustomerPhone(customer.getPhone());
        }

        viewing.setStatus(0);
        viewing.setCreateTime(new Date());
        viewingMapper.insert(viewing);

        sendViewingRequestNotification(
                viewing,
                viewing.getCustomerId(),
                USER_TYPE_CUSTOMER,
                viewing.getAgentId(),
                USER_TYPE_AGENT,
                "客户发起了带看申请，请及时确认看房时间。"
        );

        return Result.success("预约看房成功，等待中介确认", viewing.getId());
    }

    @Override
    @Transactional
    public Result<?> confirmViewing(Long id) {
        Viewing viewing = viewingMapper.selectById(id);
        if (viewing == null) {
            return Result.error("带看记录不存在");
        }
        if (!Integer.valueOf(0).equals(viewing.getStatus())) {
            return Result.error("当前状态无法确认");
        }

        House house = houseMapper.selectById(viewing.getHouseId());
        if (house != null) {
            viewing.setViewingAddress(house.getAddress());
        }

        viewing.setStatus(1);
        viewingMapper.update(viewing);
        return Result.success("已确认看房时间");
    }

    @Override
    @Transactional
    public Result<?> completeViewing(Long id, String feedback) {
        Viewing viewing = viewingMapper.selectById(id);
        if (viewing == null) {
            return Result.error("带看记录不存在");
        }
        if (!Integer.valueOf(1).equals(viewing.getStatus())) {
            return Result.error("当前状态无法完成");
        }
        if (!canCompleteViewing(viewing)) {
            return Result.error("未到可完成时间，预约开始30分钟后才能完成带看");
        }

        viewing.setStatus(2);
        viewing.setActualTime(new Date());
        if (feedback != null && !feedback.trim().isEmpty()) {
            viewing.setRemark(feedback.trim());
        }
        viewingMapper.update(viewing);
        sendViewingReviewInvite(viewing);
        return Result.success("带看已完成");
    }

    @Override
    @Transactional
    public Result<?> cancelViewing(Long id, String reason) {
        Viewing viewing = viewingMapper.selectById(id);
        if (viewing == null) {
            return Result.error("带看记录不存在");
        }
        if (Integer.valueOf(2).equals(viewing.getStatus())) {
            return Result.error("已完成的记录无法取消");
        }

        viewing.setStatus(3);
        if (reason != null && !reason.trim().isEmpty()) {
            viewing.setCancelReason(reason.trim());
        }
        viewingMapper.update(viewing);
        return Result.success("已取消看房");
    }

    @Override
    public Result<?> getCustomerViewings(Long customerId) {
        return Result.success(viewingMapper.selectByCustomerId(customerId));
    }

    @Override
    public Result<?> getAgentViewings(Long agentId) {
        return Result.success(viewingMapper.selectByAgentId(agentId));
    }

    @Override
    public Result<?> getAgentCustomers(Long agentId) {
        return Result.success(viewingMapper.selectDistinctCustomersByAgentId(agentId));
    }

    @Override
    @Transactional
    public Result<?> agentBookViewing(Viewing viewing) {
        House house = houseMapper.selectById(viewing.getHouseId());
        if (house == null) {
            return Result.error("房源不存在");
        }
        if (!Integer.valueOf(1).equals(house.getHouseStatus())) {
            return Result.error("房源未发布");
        }

        SysCustomer customer = sysCustomerMapper.selectById(viewing.getCustomerId());
        if (customer == null) {
            return Result.error("客户不存在");
        }

        viewing.setCustomerPhone(customer.getPhone());
        viewing.setStatus(4);
        viewing.setCreateTime(new Date());
        viewingMapper.insert(viewing);

        sendViewingRequestNotification(
                viewing,
                viewing.getAgentId(),
                USER_TYPE_AGENT,
                viewing.getCustomerId(),
                USER_TYPE_CUSTOMER,
                "中介发起了带看申请，请确认是否参加本次看房。"
        );

        return Result.success("带看申请已发送，等待客户确认", viewing.getId());
    }

    @Override
    @Transactional
    public Result<?> customerConfirmViewing(Long id) {
        Viewing viewing = viewingMapper.selectById(id);
        if (viewing == null) {
            return Result.error("带看记录不存在");
        }
        if (!Integer.valueOf(4).equals(viewing.getStatus())) {
            return Result.error("当前状态无法确认");
        }

        House house = houseMapper.selectById(viewing.getHouseId());
        if (house != null) {
            viewing.setViewingAddress(house.getAddress());
        }

        viewing.setStatus(1);
        viewingMapper.update(viewing);
        return Result.success("已确认带看时间");
    }

    private boolean canCompleteViewing(Viewing viewing) {
        if (viewing.getAppointTime() == null) {
            return false;
        }
        long availableAt = viewing.getAppointTime().getTime() + COMPLETE_GRACE_PERIOD_MILLIS;
        return System.currentTimeMillis() >= availableAt;
    }

    private void sendViewingRequestNotification(Viewing viewing,
                                                Long senderId,
                                                Integer senderType,
                                                Long receiverId,
                                                Integer receiverType,
                                                String content) {
        if (receiverId == null || receiverType == null) {
            return;
        }

        Message request = new Message();
        request.setSenderId(senderId);
        request.setSenderType(senderType);
        request.setReceiverId(receiverId);
        request.setReceiverType(receiverType);
        request.setMessageType(1);
        request.setMessageScene(MessageScenes.VIEWING_REQUEST);
        request.setHouseId(viewing.getHouseId());
        request.setViewingId(viewing.getId());
        request.setTitle("带看申请");
        request.setContent(content);
        request.setIsRead(0);
        messageService.sendMessage(request);
    }

    private void sendViewingReviewInvite(Viewing viewing) {
        Message invite = new Message();
        invite.setSenderId(SYSTEM_ADMIN_ID);
        invite.setSenderType(USER_TYPE_ADMIN);
        invite.setReceiverId(viewing.getCustomerId());
        invite.setReceiverType(USER_TYPE_CUSTOMER);
        invite.setMessageType(1);
        invite.setMessageScene(MessageScenes.VIEWING_REVIEW_INVITE);
        invite.setHouseId(viewing.getHouseId());
        invite.setViewingId(viewing.getId());
        invite.setTitle("带看服务评价邀请");
        invite.setContent("本次带看已完成，欢迎对中介服务进行评价，帮助我们持续提升看房体验。");
        invite.setIsRead(0);
        messageService.sendMessage(invite);
    }
}
