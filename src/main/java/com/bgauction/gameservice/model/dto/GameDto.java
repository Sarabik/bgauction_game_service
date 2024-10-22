package com.bgauction.gameservice.model.dto;

import com.bgauction.gameservice.model.entity.GameImage;
import com.bgauction.gameservice.model.entity.GameLanguage;
import com.bgauction.gameservice.model.entity.GameStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class GameDto {

    private Long id;

    @NotNull
    @Positive
    private Long userId;

    @NotBlank
    @Size(min = 2)
    private String title;

    @NotBlank
    @Size(min = 5)
    private String description;

    @NotBlank
    @Size(min = 5)
    private String condition;

    @NotNull
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private GameLanguage language = GameLanguage.EN;

    @NotNull
    @Positive
    private Integer minPlayers;

    @NotNull
    @Positive
    private Integer maxPlayers;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private GameStatus status = GameStatus.PUBLISHED;

    @Builder.Default
    private List<GameImageDto> images = new ArrayList<>();

}
