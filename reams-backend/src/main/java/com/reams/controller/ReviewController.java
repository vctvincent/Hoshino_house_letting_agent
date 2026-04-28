package com.reams.controller;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.Review;
import com.reams.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<PageResult<Review>> getReviewList(
            Authentication authentication,
            @RequestParam(required = false) Long houseId,
            @RequestParam(required = false) Long agentId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long viewingId,
            @RequestParam(required = false) Integer targetType,
            @RequestParam(required = false) Integer isShow,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        Map<String, Object> params = new HashMap<>();
        params.put("houseId", houseId);
        params.put("agentId", agentId);
        params.put("customerId", customerId);
        params.put("viewingId", viewingId);
        params.put("targetType", targetType);
        params.put("isShow", isShow);
        params.put("pageNum", pageNum);
        params.put("pageSize", pageSize);
        return Result.success(reviewService.getReviewPage(params));
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'CUSTOMER')")
    public Result<?> getReviewDetail(@PathVariable Long id) {
        return reviewService.getReviewDetail(id);
    }

    @GetMapping("/house/{houseId}")
    public Result<?> getReviewsByHouseId(@PathVariable Long houseId) {
        return reviewService.getReviewsByHouseId(houseId);
    }

    @GetMapping("/agent/{agentId}")
    public Result<?> getReviewsByAgentId(@PathVariable Long agentId) {
        return reviewService.getReviewsByAgentId(agentId);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<?> getMyReviews(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return reviewService.getMyReviews(userId);
    }

    @PostMapping({"/add", "/submit"})
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<?> addReview(Authentication authentication, @RequestBody Review review) {
        review.setCustomerId((Long) authentication.getPrincipal());
        return reviewService.addReview(review);
    }

    @PostMapping("/reply/{id}")
    @PreAuthorize("hasRole('AGENT')")
    public Result<?> replyReview(@PathVariable Long id, @RequestParam String replyContent) {
        return reviewService.replyReview(id, replyContent);
    }

    @PostMapping("/status/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> updateReviewShowStatus(@PathVariable Long id, @RequestParam Integer isShow) {
        return reviewService.updateReviewShowStatus(id, isShow);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<?> deleteReview(@PathVariable Long id) {
        return reviewService.deleteReview(id);
    }

    @GetMapping("/agent/{agentId}/rating")
    public Result<?> getAgentAvgRating(@PathVariable Long agentId) {
        return reviewService.getAgentAvgRating(agentId);
    }
}
