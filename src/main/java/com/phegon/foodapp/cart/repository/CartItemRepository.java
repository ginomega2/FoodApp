package com.phegon.foodapp.cart.repository;

import com.phegon.foodapp.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}
