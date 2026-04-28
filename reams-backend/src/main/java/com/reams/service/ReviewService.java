package com.reams.service;

import com.reams.common.result.PageResult;
import com.reams.common.result.Result;
import com.reams.entity.Review;

import java.util.Map;

public interface ReviewService {

    PageResult<Review> getReviewPage(Map<String, Object> params);

    Result<?> getReviewDetail(Long id);

    Result<?> getReviewsByHouseId(Long houseId);

    Result<?> getReviewsByAgentId(Long agentId);

    Result<?> getMyReviews(Long customerId);

    Result<?> addReview(Review review);

    Result<?> replyReview(Long id, String replyContent);

    Result<?> updateReviewShowStatus(Long id, Integer isShow);

    Result<?> deleteReview(Long id);

    Result<?> getAgentAvgRating(Long agentId);
}
