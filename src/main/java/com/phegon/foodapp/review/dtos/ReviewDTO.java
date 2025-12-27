package com.phegon.foodapp.review.dtos;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegon.foodapp.auth_users.entity.User;
import com.phegon.foodapp.menu.entity.Menu;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true  )
public class ReviewDTO {

    private Long id;
    private Long menuId;
    private Long orderId;

    private String userName;

    @NotNull(message = "rating requerido")
    @Min(1)
    @Max(10)
    private Integer rating; // de 1 a 10

    @Size(max = 100,message = "excerde 500 caracteres")
    private String comment;

    private String menuName;

    private LocalDateTime createdAt;

}
