package com.phegon.foodapp.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response <T>{

    private int statusCode; //ej 200 400
    private String message; //info adicional
    private T data; //los datos
    private Map<String,String> meta;
}
