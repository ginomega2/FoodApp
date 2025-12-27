package com.phegon.foodapp.review.services;

import com.phegon.foodapp.auth_users.entity.User;
import com.phegon.foodapp.auth_users.services.UserService;
import com.phegon.foodapp.enums.OrderStatus;
import com.phegon.foodapp.exceptions.BadRequestException;
import com.phegon.foodapp.exceptions.NotFoundException;
import com.phegon.foodapp.menu.entity.Menu;
import com.phegon.foodapp.menu.repository.MenuRepository;
import com.phegon.foodapp.order.entity.Order;
import com.phegon.foodapp.order.repository.OrderItemRepository;
import com.phegon.foodapp.order.repository.OrderRepository;
import com.phegon.foodapp.response.Response;
import com.phegon.foodapp.review.dtos.ReviewDTO;
import com.phegon.foodapp.review.entity.Review;
import com.phegon.foodapp.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final MenuRepository menuRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;

    @Override
    @Transactional
    public Response<ReviewDTO> createReview(ReviewDTO reviewDTO) {
        log.info("Creating review");
        User user =userService.getCurrentLoggedInUser();

        if(reviewDTO.getOrderId()==null || reviewDTO.getMenuId()==null) {
            throw new BadRequestException("order id y menu id se requieren");

        }

        Menu menu = menuRepository.findById(reviewDTO.getMenuId())
                .orElseThrow(() -> new NotFoundException("Menu item not found"));

        Order order = orderRepository.findById(reviewDTO.getOrderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if(!order.getUser().getId().equals(user.getId())) {
            throw new BadRequestException("la orden no es tuyas , order id y menu id not match");
        }

        if(order.getOrderStatus()!= OrderStatus.DELIVERED){
            throw new BadRequestException("solo se púeden poner reviws a lo items  en ordeesentregadas DELIVERED");

        }

        boolean itemInOrder = orderItemRepository.existsOrderIdAndMenuId(reviewDTO.getOrderId(), reviewDTO.getMenuId());

        if(!itemInOrder) {
            throw new BadRequestException("Estye menu item no es parte de la orden especificada");

        }

        if(reviewRepository.existsByUserIdAndMenuIdAndOrderId(
                user.getId(),
                reviewDTO.getMenuId(),
                reviewDTO.getOrderId())){
            throw new BadRequestException("ya haz hecho el review de este item para esta orden");

        }

        Review review = Review.builder()
                .user(user)
                .menu(menu)
                .orderId(reviewDTO.getOrderId())
                .rating(reviewDTO.getRating())
                .comment(reviewDTO.getComment())
                .createdAt(LocalDateTime.now())
                .build();

        Review savedReview = reviewRepository.save(review);

        ReviewDTO responseDto = modelMapper.map(savedReview, ReviewDTO.class);
        responseDto.setUserName(user.getName());
        responseDto.setMenuName(menu.getName());

        return Response.<ReviewDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Review se agrego con exito")
                .data(responseDto)
                .build();

    }

    @Override
    public Response<List<ReviewDTO>> getReviewsForMenu(Long menuId) {
        log.info("Retrieving reviews for menu {}", menuId);

        List<Review> reviews= reviewRepository.findByMenuIdOrderByIdDesc(menuId);

        List<ReviewDTO> reviewDTOS = reviews.stream()
                .map(review -> modelMapper.map(review,ReviewDTO.class))
                .toList();


        return Response.<List<ReviewDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Reviews recuperados con exito")
                .data(reviewDTOS)
                .build();
    }

    @Override
    public Response<Double> getAverageRating(Long menuId) {
        log.info("Retrieving average rating");

        Double averageRating = reviewRepository.calculateAverageRatingByMenuId(menuId);

        return Response.<Double>builder()
                .statusCode(HttpStatus.OK.value())
                .message("average rating recuperado con exito")
                .data(averageRating!=null ? averageRating: 0.0)
                .build();
    }
}
