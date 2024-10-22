package com.bgauction.gameservice.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class GameImageDto {

    private Long id;

    @NotNull
    @URL
    private String url;
}
