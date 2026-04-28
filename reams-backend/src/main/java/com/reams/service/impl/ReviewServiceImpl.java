package com.reams.service.impl;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.Review;
import com.reams.mapper.ReviewMapper;
import com.reams.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 评价服务实现类
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;

    @Override
    public PageResult<Review> getReviewPage(Map<String, Object> params) {
        Integer pageNum = (Integer) params.get("pageNum");
        Integer pageSize = (Integer) params.get("pageSize");
        if (pageNum == null) pageNum = 1;
        if (pageSize == null) pageSize = 10;

        params.put("offset", (pageNum - 1) * pageSize);
        params.put("limit", pageSize);

        List<Review> list = reviewMapper.selectPage(params);
        long total = reviewMapper.count(params);

        return PageResult.of(total, pageNum, pageSize, list);
    }

    @Override
    public Result<?> getReviewDetail(Long id) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            return Result.error("评价不存在");
        }
        return Result.success(review);
    }

    @Override
    public Result<?> getReviewsByHouseId(Long houseId) {
        if (houseId == null) {
            return Result.error("房源 ID 不能为空");
        }
        List<Review> list = reviewMapper.selectByHouseId(houseId);
        return Result.success(list);
    }

    @Override
    public Result<?> getReviewsByAgentId(Long agentId) {
        if (agentId == null) {
            return Result.error("中介 ID 不能为空");
        }
        List<Review> list = reviewMapper.selectByAgentId(agentId);
        return Result.success(list);
    }

    @Override
    public Result<?> getMyReviews(Long customerId) {
        if (customerId == null) {
            return Result.error("客户 ID 不能为空");
        }
        List<Review> list = reviewMapper.selectByCustomerId(customerId);
        return Result.success(list);
    }

    @Override
    @Transactional
    public Result<?> addReview(Review review) {
        // 验证必填字段
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            return Result.error("评分必须在 1-5 之间");
        }
        if (review.getContent() == null || review.getContent().trim().isEmpty()) {
            return Result.error("评价内容不能为空");
        }
        if (review.getHouseId() == null && review.getAgentId() == null) {
            return Result.error("至少需要指定房源或中介");
        }

        System.out.println("=== 开始插入评价 ===");
        System.out.println("=== 前端提交数据：" + review);
        System.out.println("=== houseId: " + review.getHouseId());
        System.out.println("=== agentId: " + review.getAgentId());
        System.out.println("=== customerId: " + review.getCustomerId());
        System.out.println("=== rating: " + review.getRating());
        System.out.println("=== content: " + review.getContent());
        System.out.println("=== targetType: " + review.getTargetType());

        // 处理 images 字段：如果是数组则转为 JSON 字符串
        if (review.getImages() != null && !review.getImages().startsWith("[") && !review.getImages().startsWith("{")) {
            // 如果不是 JSON 格式，可能是单个 URL，转为数组
            review.setImages("[\"" + review.getImages() + "\"]");
        } else if (review.getImages() == null) {
            review.setImages("[]"); // 默认空数组
        }

        // 设置默认值
        review.setIsShow(1); // 默认显示
        review.setCreateTime(new Date());
        
        System.out.println("=== 准备插入，最终数据：" + review);
        
        int rows = reviewMapper.insert(review);
        System.out.println("=== 插入结果，影响行数：" + rows);
        System.out.println("=== 生成的评价 ID: " + review.getId());
        
        if (rows > 0) {
            return Result.success("评价提交成功");
        } else {
            return Result.error("评价提交失败");
        }
    }

    @Override
    @Transactional
    public Result<?> replyReview(Long id, String replyContent) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            return Result.error("评价不存在");
        }

        review.setReplyContent(replyContent);
        review.setReplyTime(new Date());
        reviewMapper.update(review);

        return Result.success("回复成功");
    }

    @Override
    @Transactional
    public Result<?> updateReviewShowStatus(Long id, Integer isShow) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            return Result.error("评价不存在");
        }

        review.setIsShow(isShow);
        reviewMapper.update(review);

        return Result.success(isShow == 1 ? "已设置为显示" : "已设置为隐藏");
    }

    @Override
    @Transactional
    public Result<?> deleteReview(Long id) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            return Result.error("评价不存在");
        }

        reviewMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result<?> getAgentAvgRating(Long agentId) {
        if (agentId == null) {
            return Result.error("中介 ID 不能为空");
        }
        
        Double avgRating = reviewMapper.selectAvgRatingByAgentId(agentId);
        if (avgRating == null) {
            return Result.success(0.0);
        }
        
        // 保留一位小数
        avgRating = Math.round(avgRating * 10.0) / 10.0;
        return Result.success(avgRating);
    }
}
