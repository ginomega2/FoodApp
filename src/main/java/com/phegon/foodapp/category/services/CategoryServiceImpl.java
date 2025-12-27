package com.phegon.foodapp.category.services;

import com.phegon.foodapp.category.dtos.CategoryDTO;
import com.phegon.foodapp.category.entity.Category;
import com.phegon.foodapp.category.repository.CategoryRepository;
import com.phegon.foodapp.exceptions.NotFoundException;
import com.phegon.foodapp.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;

    @Override
    public Response<CategoryDTO> addCategory(CategoryDTO categoryDTO) {
        log.info("dentro de addCategory ");
        Category category = modelMapper.map(categoryDTO, Category.class);
        category = categoryRepository.save(category);

        return Response.<CategoryDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("categoria agregada cn exito")
                .build();

    }

    @Override
    public Response<CategoryDTO> updateCategory(CategoryDTO categoryDTO) {
        log.info("dentro de  updateCategory");
        Category category = categoryRepository.findById(categoryDTO.getId())
                .orElseThrow(() -> new NotFoundException("categoria no encontreada"));

        if(categoryDTO.getName()!=null  && !categoryDTO.getName().isEmpty())
            category.setName(categoryDTO.getName());
        if(categoryDTO.getDescription()!=null ) category.setDescription(categoryDTO.getDescription());
        categoryRepository.save(category);
        return Response.<CategoryDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Categoria actualizada con exito")
                .build();



    }

    @Override
    public Response<CategoryDTO> getCategoryById(Long id) {
        log.info("dentro de getCategoryById ");
        Category category =categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("categoria no encontreada"));
        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);

        return Response.<CategoryDTO>builder()
                .statusCode(HttpStatus.OK.value())
                .message("Categoria encontrada")
                .data(categoryDTO)
                .build();
    }

    @Override
    public Response<List<CategoryDTO>> getAllCategories() {
        log.info("dentro de  getAllCategories");

        List<Category> categories = categoryRepository.findAll();
        List<CategoryDTO> categoryDTOs = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();

        return Response.<List<CategoryDTO>>builder()
                .statusCode(HttpStatus.OK.value())
                .message("todas las categorias leidas")
                .data(categoryDTOs)
                .build();

    }

    @Override
    public Response<?> deleteCategory(Long id) {
        log.info("dentro de deleteCategory ");
        if(!categoryRepository.existsById(id)){
            throw new NotFoundException("categoria no encontreada");
        }
        categoryRepository.deleteById(id);
        return Response.builder()
                .statusCode(HttpStatus.OK.value())
                .message("Categoria eliminada con exito")
                .build();

    }
}
