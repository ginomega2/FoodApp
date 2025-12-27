package com.phegon.foodapp.category.services;

import com.phegon.foodapp.category.dtos.CategoryDTO;
import com.phegon.foodapp.response.Response;

import java.util.List;

public interface CategoryService {
    Response <CategoryDTO> addCategory(CategoryDTO category);
    Response <CategoryDTO> updateCategory(CategoryDTO category);
    Response <CategoryDTO> getCategoryById(Long id);

    Response <List<CategoryDTO>> getAllCategories();

    Response <?> deleteCategory(Long id);

}
