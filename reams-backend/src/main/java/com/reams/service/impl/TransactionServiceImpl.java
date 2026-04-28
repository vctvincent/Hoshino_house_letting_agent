package com.reams.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reams.common.constant.MessageScenes;
import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.House;
import com.reams.entity.Message;
import com.reams.entity.SysAgent;
import com.reams.entity.SysCustomer;
import com.reams.entity.Transaction;
import com.reams.mapper.HouseMapper;
import com.reams.mapper.SysAgentMapper;
import com.reams.mapper.SysCustomerMapper;
import com.reams.mapper.TransactionMapper;
import com.reams.service.MessageService;
import com.reams.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易服务实现类
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private static final int HOUSE_STATUS_PUBLISHED = 1;
    private static final int HOUSE_STATUS_SOLD = 2;
    private static final int HOUSE_STATUS_DELISTED = 3;
    private static final int HOUSE_STATUS_IN_TRANSACTION = 5;
    private static final int HOUSE_AUDIT_APPROVED = 2;
    private static final int USER_STATUS_ACTIVE = 1;
    private static final int TRANSACTION_STATUS_PENDING = 0;

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private HouseMapper houseMapper;

    @Autowired
    private SysCustomerMapper customerMapper;

    @Autowired
    private SysAgentMapper agentMapper;

    @Autowired
    private MessageService messageService;

    @Override
    public PageResult<Transaction> getTransactionPage(Map<String, Object> params) {
        Integer pageNum = (Integer) params.get("pageNum");
        Integer pageSize = (Integer) params.get("pageSize");
        if (pageNum == null) pageNum = 1;
        if (pageSize == null) pageSize = 10;

        params.put("offset", (pageNum - 1) * pageSize);
        params.put("limit", pageSize);

        List<Transaction> list = transactionMapper.selectPage(params);
        long total = transactionMapper.count(params);

        return PageResult.of(total, pageNum, pageSize, list);
    }

    @Override
    public Result<?> getTransactionDetail(Long id) {
        Transaction transaction = transactionMapper.selectById(id);
        if (transaction == null) {
            return Result.error("交易不存在");
        }
        return Result.success(transaction);
    }


    //创建交易记录
    @Override
    @Transactional
    public Result<?> createTransaction(Transaction transaction) {
        // 校验房源状态
        House house = houseMapper.selectById(transaction.getHouseId());
        Result<?> houseValidation = validateHouseForCreate(house);
        if (houseValidation != null) {
            return houseValidation;
        }
        // 校验客户状态
        SysCustomer customer = customerMapper.selectById(transaction.getCustomerId());
        Result<?> customerValidation = validateCustomerForCreate(customer);
        if (customerValidation != null) {
            return customerValidation;
        }
        // 校验中介状态
        SysAgent agent = agentMapper.selectById(transaction.getAgentId());
        Result<?> agentValidation = validateAgentForCreate(agent);
        if (agentValidation != null) {
            return agentValidation;
        }
        // 校验交易输入参数
        Result<?> inputValidation = validateCreateTransactionInput(transaction);
        if (inputValidation != null) {
            return inputValidation;
        }
        // 初始化交易信息
        initializeTransaction(transaction);
        transactionMapper.insert(transaction);
        // 更新房源状态为，防止重复交易
        houseMapper.updateStatus(transaction.getHouseId(), HOUSE_STATUS_IN_TRANSACTION);
        // 发送交易创建通知给客户
        sendTransactionCreatedNotification(transaction, house, agent);
        return Result.success("交易创建成功", transaction);
    }

    private Result<?> validateHouseForCreate(House house) {
        if (house == null) {
            return Result.error("房源不存在");
        }
        if (Integer.valueOf(HOUSE_STATUS_SOLD).equals(house.getHouseStatus())) {
            return Result.error("房源已售出，无法创建交易");
        }
        if (Integer.valueOf(HOUSE_STATUS_DELISTED).equals(house.getHouseStatus())) {
            return Result.error("房源已下架，无法创建交易");
        }
        if (Integer.valueOf(HOUSE_STATUS_IN_TRANSACTION).equals(house.getHouseStatus())) {
            return Result.error("该房源正在交易中，无法创建新交易");
        }
        if (!Integer.valueOf(HOUSE_AUDIT_APPROVED).equals(house.getAuditStatus())) {
            return Result.error("房源未通过审核，无法创建交易");
        }
        return null;
    }

    private Result<?> validateCustomerForCreate(SysCustomer customer) {
        if (customer == null) {
            return Result.error("客户不存在");
        }
        if (!Integer.valueOf(USER_STATUS_ACTIVE).equals(customer.getStatus())) {
            return Result.error("客户状态异常，无法创建交易");
        }
        return null;
    }

    private Result<?> validateAgentForCreate(SysAgent agent) {
        if (agent == null) {
            return Result.error("中介不存在");
        }
        if (!Integer.valueOf(USER_STATUS_ACTIVE).equals(agent.getStatus())) {
            return Result.error("中介状态异常，无法创建交易");
        }
        return null;
    }

    private Result<?> validateCreateTransactionInput(Transaction transaction) {
        if (transaction.getFinalPrice() == null || transaction.getFinalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("成交价格必须大于 0");
        }

        if (transaction.getDeposit() != null && transaction.getDeposit().compareTo(transaction.getFinalPrice()) > 0) {
            return Result.error("定金不能超过成交价格");
        }

        if (transaction.getPaymentMethod() != null && !isValidPaymentMethod(transaction.getPaymentMethod())) {
            return Result.error("付款方式不合法，请选择：全款、分期或贷款");
        }

        return null;
    }

    private boolean isValidPaymentMethod(String paymentMethod) {
        return "全款".equals(paymentMethod)
            || "分期".equals(paymentMethod)
            || "贷款".equals(paymentMethod);
    }

    private void initializeTransaction(Transaction transaction) {
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setStatus(TRANSACTION_STATUS_PENDING);
        transaction.setStatusHistory("[]");
    }

    private String generateTransactionNo() {
        return "TX" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
            + String.format("%04d", (int) (Math.random() * 10000));
    }

    private void sendTransactionCreatedNotification(Transaction transaction, House house, SysAgent agent) {
        try {
            Message customerMsg = new Message();
            customerMsg.setSenderId(transaction.getAgentId());
            customerMsg.setSenderType(2);
            customerMsg.setReceiverId(transaction.getCustomerId());
            customerMsg.setReceiverType(3);
            customerMsg.setMessageType(1);
            customerMsg.setMessageScene(MessageScenes.TRANSACTION_CREATED);
            customerMsg.setHouseId(transaction.getHouseId());
            customerMsg.setViewingId(transaction.getViewingId());
            customerMsg.setTitle("🎉 交易创建通知");
            customerMsg.setContent(buildTransactionCreatedContent(transaction, house, agent));
            customerMsg.setIsRead(0);
            customerMsg.setCreateTime(new Date());
            customerMsg.setUpdateTime(new Date());

            messageService.sendMessage(customerMsg);
            System.out.println("✅ [交易消息] 已发送交易创建通知给客户 ID: " + transaction.getCustomerId());
        } catch (Exception e) {
            System.err.println("⚠️ [交易消息] 发送客户通知失败: " + e.getMessage());
        }
    }

    private String buildTransactionCreatedContent(Transaction transaction, House house, SysAgent agent) {
        return String.format(
            "👋 %s 已为您创建了一笔新交易，请及时查看详情。\n\n" +
                "🏠 房源名称：%s\n" +
                "💰 成交价格：%.2f 万元\n" +
                "💵 定金金额：%.2f 万元\n" +
                "📋 交易单号：%s\n\n" +
                "👉 请尽快登录系统查看交易详情并确认。\n" +
                "💡 确认后交易将进入谈判阶段，您可以与中介协商价格。",
            agent.getName(),
            house.getTitle(),
            transaction.getFinalPrice(),
            transaction.getDeposit() != null ? transaction.getDeposit() : BigDecimal.ZERO,
            transaction.getTransactionNo()
        );
    }

    @Override
    public Result<?> updateTransaction(Transaction transaction) {
        Transaction existing = transactionMapper.selectById(transaction.getId());
        if (existing == null) {
            return Result.error("交易不存在");
        }
        transactionMapper.update(transaction);
        return Result.success("交易更新成功");
    }

    @Override
    @Transactional
    public Result<?> updateTransactionStatus(Long id, Integer status, String remark) {
        Transaction transaction = transactionMapper.selectById(id);
        if (transaction == null) {
            return Result.error("交易不存在");
        }

        if (!validateStatusChange(transaction.getStatus(), status)) {
            return Result.error("非法的状态流转：不允许从状态 " + transaction.getStatus() + " 直接跳转到状态 " + status);
        }

        String newHistory = buildStatusHistoryRecord(
            transaction.getStatusHistory(),
            transaction.getStatus(),
            status,
            remark
        );

        transactionMapper.updateStatus(id, status, newHistory, remark);

        if (status == 3) {
            houseMapper.updateStatus(transaction.getHouseId(), HOUSE_STATUS_SOLD);
        } else if (status == 4) {
            houseMapper.updateStatus(transaction.getHouseId(), HOUSE_STATUS_PUBLISHED);
        }

        try {
            House house = houseMapper.selectById(transaction.getHouseId());
            SysCustomer customer = customerMapper.selectById(transaction.getCustomerId());
            SysAgent agent = agentMapper.selectById(transaction.getAgentId());

            String statusText = getStatusText(status);
            String contentTemplate = "";
            String agentName = agent != null ? agent.getName() : "中介";

            switch (status) {
                case 0:
                    contentTemplate = String.format("👋 %s 已为您创建了一笔交易，请及时确认。\n\n" +
                            "🏠 房源名称：%s\n" +
                            "💰 成交价格：%.2f 万元\n" +
                            "📋 交易单号：%s\n\n" +
                            "👉 请尽快登录系统查看详情并确认交易。\n" +
                            "💡 确认后可进入交易详情页了解更多信息。",
                        agentName,
                        house != null ? house.getTitle() : "未知",
                        transaction.getFinalPrice(),
                        transaction.getTransactionNo());
                    break;

                case 1:
                    boolean isPriceNegotiation = remark != null && remark.contains("价格协商");
                    boolean isCustomerConfirm = remark != null && remark.contains("客户确认");
                    boolean isAgentStart = remark != null && remark.contains("中介开始");

                    if (isPriceNegotiation) {
                        boolean isCustomerOffer = remark != null && remark.contains("客户备注");

                        if (isCustomerOffer) {
                            contentTemplate = String.format("💬 客户已提出还价，请及时处理！\n\n" +
                                    "🏠 房源名称：%s\n" +
                                    "💰 最新价格：%.2f 万元\n" +
                                    "📋 交易单号：%s\n" +
                                    "👤 客户名称：%s\n" +
                                    "%s\n\n" +
                                    "👉 您可以在交易详情中：\n" +
                                    "   1. 同意客户还价，进入签约阶段\n" +
                                    "   2. 继续协商，提出新的报价\n" +
                                    "   3. 取消交易",
                                house != null ? house.getTitle() : "未知",
                                transaction.getFinalPrice(),
                                transaction.getTransactionNo(),
                                transaction.getCustomerName() != null ? transaction.getCustomerName() : "未知客户",
                                remark != null && !remark.isEmpty() ? "📝 客户备注：" + remark.substring(remark.indexOf("客户备注：") + 5) + "\n" : "");
                        } else {
                            contentTemplate = String.format("🤝 中介已调整报价，请注意查看！\n\n" +
                                    "🏠 房源名称：%s\n" +
                                    "💰 最新报价：%.2f 万元\n" +
                                    "📋 交易单号：%s\n" +
                                    "%s\n\n" +
                                    "👉 您可以在交易详情中：\n" +
                                    "   1. 同意当前价格，进入签约阶段\n" +
                                    "   2. 继续协商，提出您的还价\n" +
                                    "   3. 取消交易",
                                house != null ? house.getTitle() : "未知",
                                transaction.getFinalPrice(),
                                transaction.getTransactionNo(),
                                remark != null && !remark.isEmpty() ? "📝 中介备注：" + remark.substring(remark.indexOf("中介备注：") + 5) + "\n" : "");
                        }
                    } else if (isCustomerConfirm) {
                        contentTemplate = String.format("✅ 客户已确认交易，可以开始谈判了！\n\n" +
                                "🏠 房源名称：%s\n" +
                                "💰 当前价格：%.2f 万元\n" +
                                "📋 交易单号：%s\n" +
                                "👤 客户名称：%s\n\n" +
                                "👉 请在交易详情中开始谈判或协商价格。",
                            house != null ? house.getTitle() : "未知",
                            transaction.getFinalPrice(),
                            transaction.getTransactionNo(),
                            transaction.getCustomerName() != null ? transaction.getCustomerName() : "未知客户");
                    } else if (isAgentStart) {
                        contentTemplate = String.format("🤝 交易已进入谈判阶段，您可以与中介协商价格。\n\n" +
                                "🏠 房源名称：%s\n" +
                                "💰 当前价格：%.2f 万元\n" +
                                "📋 交易单号：%s\n" +
                                "%s\n\n" +
                                "👉 如需协商价格，请在交易详情中提交您的期望价格。",
                            house != null ? house.getTitle() : "未知",
                            transaction.getFinalPrice(),
                            transaction.getTransactionNo(),
                            remark != null && !remark.isEmpty() ? "📝 中介备注：" + remark + "\n" : "");
                    } else {
                        contentTemplate = String.format("🤝 交易已进入谈判阶段，您可以与中介协商价格。\n\n" +
                                "🏠 房源名称：%s\n" +
                                "💰 当前价格：%.2f 万元\n" +
                                "📋 交易单号：%s\n" +
                                "%s\n\n" +
                                "👉 如需协商价格，请在交易详情中提交您的期望价格。",
                            house != null ? house.getTitle() : "未知",
                            transaction.getFinalPrice(),
                            transaction.getTransactionNo(),
                            remark != null && !remark.isEmpty() ? "📝 备注：" + remark + "\n" : "");
                    }
                    break;

                case 2:
                    boolean isAgentAgreed = remark != null && remark.contains("中介同意");
                    boolean isCustomerAgreed = remark != null && remark.contains("客户同意");

                    if (isAgentAgreed) {
                        contentTemplate = String.format("✍️ 中介已同意您的还价，交易进入签约阶段！\n\n" +
                                "🏠 房源名称：%s\n" +
                                "💰 成交价格：%.2f 万元\n" +
                                "📋 交易单号：%s\n" +
                                "👤 中介名称：%s\n\n" +
                                "👉 中介将上传合同，请留意查看。\n" +
                                "💡 合同签署后，请按约定完成付款手续。",
                            house != null ? house.getTitle() : "未知",
                            transaction.getFinalPrice(),
                            transaction.getTransactionNo(),
                            agentName);
                    } else if (isCustomerAgreed) {
                        contentTemplate = String.format("✍️ 客户已同意当前价格，交易进入签约阶段！\n\n" +
                                "🏠 房源名称：%s\n" +
                                "💰 成交价格：%.2f 万元\n" +
                                "📋 交易单号：%s\n" +
                                "👤 客户名称：%s\n\n" +
                                "👉 请上传合同完成签约流程。",
                            house != null ? house.getTitle() : "未知",
                            transaction.getFinalPrice(),
                            transaction.getTransactionNo(),
                            transaction.getCustomerName() != null ? transaction.getCustomerName() : "未知客户");
                    } else {
                        contentTemplate = String.format("✍️ 交易合同已签署，请注意后续流程。\n\n" +
                                "🏠 房源名称：%s\n" +
                                "💰 成交价格：%.2f 万元\n" +
                                "📋 交易单号：%s\n" +
                                "%s\n\n" +
                                "👉 请按照合同约定完成后续付款和过户手续。",
                            house != null ? house.getTitle() : "未知",
                            transaction.getFinalPrice(),
                            transaction.getTransactionNo(),
                            remark != null && !remark.isEmpty() ? "📝 备注：" + remark + "\n" : "");
                    }
                    break;

                case 3:
                    contentTemplate = String.format("🎉 恭喜！交易已圆满完成！\n\n" +
                            "🏠 房源名称：%s\n" +
                            "💰 成交价格：%.2f 万元\n" +
                            "📋 交易单号：%s\n\n" +
                            "👉 感谢您使用我们的服务，祝您生活愉快！\n" +
                            "💡 如对本次服务满意，欢迎对我们的中介进行评价。",
                        house != null ? house.getTitle() : "未知",
                        transaction.getFinalPrice(),
                        transaction.getTransactionNo());
                    break;

                case 4:
                    contentTemplate = String.format("❌ 交易已取消。\n\n" +
                            "🏠 房源名称：%s\n" +
                            "📋 交易单号：%s\n" +
                            "%s\n\n" +
                            "👉 房源已恢复为可售状态，如需继续交易请重新发起。",
                        house != null ? house.getTitle() : "未知",
                        transaction.getTransactionNo(),
                        remark != null && !remark.isEmpty() ? "📝 取消原因：" + remark + "\n" : "");
                    break;

                default:
                    contentTemplate = String.format("📢 交易状态变更为：%s\n\n" +
                            "🏠 房源名称：%s\n" +
                            "📋 交易单号：%s\n\n" +
                            "👉 请登录系统查看详情。",
                        statusText,
                        house != null ? house.getTitle() : "未知",
                        transaction.getTransactionNo());
            }

            if (customer != null) {
                Message customerMsg = new Message();
                customerMsg.setSenderId(transaction.getAgentId());
                customerMsg.setSenderType(2);
                customerMsg.setReceiverId(transaction.getCustomerId());
                customerMsg.setReceiverType(3);
                customerMsg.setMessageType(1);
                customerMsg.setMessageScene(MessageScenes.TRANSACTION_STATUS_UPDATED);
                customerMsg.setHouseId(transaction.getHouseId());
                customerMsg.setViewingId(transaction.getViewingId());
                customerMsg.setTitle("交易状态通知 - " + statusText);
                customerMsg.setContent(contentTemplate);
                customerMsg.setIsRead(0);
                customerMsg.setCreateTime(new Date());
                customerMsg.setUpdateTime(new Date());

                messageService.sendMessage(customerMsg);
                System.out.println("✅ [交易消息] 已发送状态变更通知给客户 ID: " + transaction.getCustomerId());
            }
        } catch (Exception e) {
            System.err.println("⚠️ [交易消息] 发送状态变更通知失败: " + e.getMessage());
        }

        return Result.success("状态更新成功");
    }

    @Override
    @Transactional
    public Result<?> negotiatePrice(Long id, BigDecimal newPrice, String remark) {
        Transaction transaction = transactionMapper.selectById(id);
        if (transaction == null) {
            return Result.error("交易不存在");
        }

        if (transaction.getStatus() != 1) {
            return Result.error("只有谈判中的交易才能协商价格");
        }

        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("价格必须大于 0");
        }

        BigDecimal oldPrice = transaction.getFinalPrice();
        String negotiationRecord = String.format(
            "价格协商：%.2f万元 → %.2f万元 | %s",
            oldPrice, newPrice,
            remark != null && !remark.isEmpty() ? remark : ""
        );

        String newHistory = buildStatusHistoryRecord(
            transaction.getStatusHistory(),
            transaction.getStatus(),
            transaction.getStatus(),
            negotiationRecord
        );

        transactionMapper.updatePriceAndHistory(id, newPrice, newHistory);
        sendNegotiationNotification(transaction, oldPrice, newPrice, remark);

        return Result.success("价格更新成功");
    }

    /**
     * 发送价格协商通知
     * 客户提出还价时通知中介，中介调整报价时通知客户
     */
    private void sendNegotiationNotification(Transaction transaction, BigDecimal oldPrice, BigDecimal newPrice, String remark) {
        try {
            House house = houseMapper.selectById(transaction.getHouseId());
            String houseTitle = house != null ? house.getTitle() : "未知房源";

            String title = "💬 价格协商通知 - " + houseTitle;

            String customerContent = String.format(
                "📢 中介已调整交易报价，请注意查看！\n\n" +
                    "🏠 房源名称：%s\n" +
                    "💰 原价格：%.2f 万元\n" +
                    "💵 新报价：%.2f 万元\n" +
                    "📋 交易单号：%s\n" +
                    "%s\n\n" +
                    "👉 您可以继续还价，或点击\"同意当前价格\"进入签约阶段。\n" +
                    "💡 如需沟通，请通过系统消息联系中介。",
                houseTitle,
                oldPrice,
                newPrice,
                transaction.getTransactionNo(),
                remark != null && !remark.isEmpty() ? "📝 中介备注：" + remark + "\n" : ""
            );

            String agentContent = String.format(
                "📢 客户已提出还价，请注意查看！\n\n" +
                    "🏠 房源名称：%s\n" +
                    "💰 原价格：%.2f 万元\n" +
                    "💵 客户还价：%.2f 万元\n" +
                    "👤 客户名称：%s\n" +
                    "📋 交易单号：%s\n" +
                    "%s\n\n" +
                    "👉 您可以继续协商，或等待客户同意当前价格。\n" +
                    "💡 如需沟通，请通过系统消息联系客户。",
                houseTitle,
                oldPrice,
                newPrice,
                transaction.getCustomerName() != null ? transaction.getCustomerName() : "未知客户",
                transaction.getTransactionNo(),
                remark != null && !remark.isEmpty() ? "📝 客户备注：" + remark + "\n" : ""
            );

            Message customerMsg = new Message();
            customerMsg.setSenderId(transaction.getAgentId());
            customerMsg.setSenderType(2);
            customerMsg.setReceiverId(transaction.getCustomerId());
            customerMsg.setReceiverType(3);
            customerMsg.setMessageType(1);
            customerMsg.setMessageScene(MessageScenes.TRANSACTION_NEGOTIATION);
            customerMsg.setHouseId(transaction.getHouseId());
            customerMsg.setViewingId(transaction.getViewingId());
            customerMsg.setTitle(title);
            customerMsg.setContent(customerContent);
            customerMsg.setIsRead(0);
            customerMsg.setCreateTime(new Date());
            customerMsg.setUpdateTime(new Date());

            messageService.sendMessage(customerMsg);
            System.out.println("✅ [交易协商] 已发送报价调整通知给客户 ID: " + transaction.getCustomerId());

            Message agentMsg = new Message();
            agentMsg.setSenderId(transaction.getCustomerId());
            agentMsg.setSenderType(3);
            agentMsg.setReceiverId(transaction.getAgentId());
            agentMsg.setReceiverType(2);
            agentMsg.setMessageType(1);
            agentMsg.setMessageScene(MessageScenes.TRANSACTION_NEGOTIATION);
            agentMsg.setHouseId(transaction.getHouseId());
            agentMsg.setViewingId(transaction.getViewingId());
            agentMsg.setTitle(title);
            agentMsg.setContent(agentContent);
            agentMsg.setIsRead(0);
            agentMsg.setCreateTime(new Date());
            agentMsg.setUpdateTime(new Date());

            messageService.sendMessage(agentMsg);
            System.out.println("✅ [交易协商] 已发送还价通知给中介 ID: " + transaction.getAgentId());

        } catch (Exception e) {
            System.err.println("⚠️ [交易协商] 发送协商通知失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 验证状态流转合法性
     */
    private boolean validateStatusChange(Integer fromStatus, Integer toStatus) {
        if (fromStatus == 0 && toStatus == 1) return true;
        if (fromStatus == 1 && toStatus == 2) return true;
        if (fromStatus == 2 && toStatus == 3) return true;
        if (toStatus == 4) return true;
        return false;
    }

    /**
     * 构建状态历史记录 (JSON 格式)
     */
    private String buildStatusHistoryRecord(String existingHistory,
                                            Integer fromStatus,
                                            Integer toStatus,
                                            String remark) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            List<Map<String, Object>> historyList;
            if (existingHistory == null || existingHistory.trim().isEmpty()) {
                historyList = new ArrayList<>();
            } else {
                historyList = mapper.readValue(existingHistory, ArrayList.class);
            }

            Map<String, Object> newRecord = new HashMap<>();
            newRecord.put("from_status", fromStatus);
            newRecord.put("to_status", toStatus);
            newRecord.put("remark", remark != null ? remark : "");
            newRecord.put("time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            historyList.add(newRecord);
            return mapper.writeValueAsString(historyList);
        } catch (Exception e) {
            e.printStackTrace();
            return "[{\"from_status\":" + fromStatus + ",\"to_status\":" + toStatus +
                ",\"time\":\"" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\"}]";
        }
    }

    /**
     * 获取交易状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待确认";
            case 1: return "谈判中";
            case 2: return "已签约";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知状态";
        }
    }

    @Override
    public Result<?> getCustomerTransactions(Long customerId) {
        List<Transaction> list = transactionMapper.selectByCustomerId(customerId);
        return Result.success(list);
    }

    @Override
    public Result<?> getAgentTransactions(Long agentId) {
        List<Transaction> list = transactionMapper.selectByAgentId(agentId);
        return Result.success(list);
    }

    @Override
    public Result<?> getAgentTotalSales(Long agentId) {
        Map<String, Object> params = new HashMap<>();
        params.put("agentId", agentId);
        params.put("status", 2);

        BigDecimal total = transactionMapper.sumFinalPrice(params);

        Map<String, Object> data = new HashMap<>();
        data.put("totalSales", total != null ? total : BigDecimal.ZERO);

        return Result.success(data);
    }

    @Override
    @Transactional
    public Result<?> deleteTransaction(Long id) {
        Transaction transaction = transactionMapper.selectById(id);
        if (transaction == null) {
            return Result.error("交易不存在");
        }

        if (transaction.getStatus() != 4) {
            return Result.error("只能删除已取消的交易记录");
        }

        houseMapper.updateStatus(transaction.getHouseId(), HOUSE_STATUS_PUBLISHED);

        int result = transactionMapper.deleteById(id);
        if (result > 0) {
            System.out.println("✅ [交易删除] 已删除交易 ID: " + id + "，房源 ID: " + transaction.getHouseId() + " 已恢复为在售状态");
            return Result.success("交易记录已删除");
        } else {
            return Result.error("删除失败");
        }
    }
}
