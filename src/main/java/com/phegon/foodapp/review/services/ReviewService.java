package com.phegon.foodapp.review.services;

import com.phegon.foodapp.response.Response;
import com.phegon.foodapp.review.dtos.ReviewDTO;

import java.util.List;

public interface ReviewService {

    Response<ReviewDTO> createReview(ReviewDTO reviewDTO);
    Response<List<ReviewDTO>> getReviewsForMenu(Long menuId);
    Response<Double> getAverageRating(Long menuId);

}
