package com.bgauction.gameservice.model.mapper;

import com.bgauction.gameservice.model.dto.GameDto;
import com.bgauction.gameservice.model.entity.Game;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface GameMapper {
    Game gameDtoToGame(GameDto dto);
    GameDto gameToGameDto(Game entity);
}
