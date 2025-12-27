package com.phegon.foodapp.menu.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegon.foodapp.category.entity.Category;
import com.phegon.foodapp.review.dtos.ReviewDTO;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MenuDTO {

    private Long id;
    private String name;
    private String description;

    @NotNull(message = "precio requerido")
    @Positive(message = "precio debe ser posit5ivo")
    private BigDecimal price;

    private String imageUrl;

    private Long categoryId;

    private MultipartFile imageFile;

    private List<ReviewDTO> reviews;


}
