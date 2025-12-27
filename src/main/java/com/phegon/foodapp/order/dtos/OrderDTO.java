package com.phegon.foodapp.order.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegon.foodapp.auth_users.dtos.UserDTO;
import com.phegon.foodapp.auth_users.entity.User;
import com.phegon.foodapp.enums.OrderStatus;
import com.phegon.foodapp.enums.PaymentStatus;
import com.phegon.foodapp.order.entity.OrderItem;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderDTO {

    private Long id;

    private LocalDateTime orderDate;

    private BigDecimal totalAmount;

    private OrderStatus orderStatus;

    private PaymentStatus paymentStatus;
    private UserDTO user;


    private List<OrderItemDTO> orderItems;


}
