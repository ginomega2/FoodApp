package com.phegon.foodapp.category.repository;

import com.phegon.foodapp.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
