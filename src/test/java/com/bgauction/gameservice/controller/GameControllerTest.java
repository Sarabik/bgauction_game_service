package com.bgauction.gameservice.controller;

import com.bgauction.gameservice.exception.BadRequestException;
import com.bgauction.gameservice.exception.NotFoundException;
import com.bgauction.gameservice.model.dto.GameDto;
import com.bgauction.gameservice.model.entity.Game;
import com.bgauction.gameservice.model.entity.GameStatus;
import com.bgauction.gameservice.model.mapper.GameMapper;
import com.bgauction.gameservice.model.mapper.GameMapperImpl;
import com.bgauction.gameservice.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.bgauction.gameservice.util.TestUtil.generateExistingImageListForExistingGame;
import static com.bgauction.gameservice.util.TestUtil.generateExistingImageListForExistingGameDto;
import static com.bgauction.gameservice.util.TestUtil.generateGame;
import static com.bgauction.gameservice.util.TestUtil.generateGameDto;
import static com.bgauction.gameservice.util.TestUtil.generateNewImageListForNewGameDto;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@Import(GameMapperImpl.class)
class GameControllerTest {

    @Value("${service.internal-key}")
    private String serviceInternalKey;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @Autowired
    private GameMapper gameMapper;

    @Autowired
    ObjectMapper objectMapper;

    private GameDto existingGameDto;
    private GameDto gameDtoForSaving;
    private Game existingGame;
    private final Long gameId = 1L;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        existingGameDto = generateGameDto(
                gameId, userId, GameStatus.PUBLISHED, generateExistingImageListForExistingGameDto());
        gameDtoForSaving = generateGameDto(
                null, userId, null, generateNewImageListForNewGameDto());
        existingGame = generateGame(
                gameId, userId,GameStatus.PUBLISHED, generateExistingImageListForExistingGame());
    }

    @Test
    @DisplayName("Get game by id - successfully")
    void getGameByIdShouldReturnGame() throws Exception {
        when(gameService.findGameById(gameId)).thenReturn(existingGame);

        mockMvc.perform(get("/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId))
                .andExpect(jsonPath("$.userId").value(userId));
        mockMvc.perform(get("/internal/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(gameId))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    @DisplayName("Get game by id - when service throws NotFoundException")
    void getGameByIdWhenServiceThrowsNotFoundException() throws Exception {
        when(gameService.findGameById(gameId))
                .thenThrow(new NotFoundException("Game with id: 1 is not found"));
        mockMvc.perform(get("/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Game with id: 1 is not found"));
        verify(gameService, times(1)).findGameById(gameId);
    }

    @Test
    @DisplayName("Get game by id - without X-Service-Key header")
    void getGameByIdWithoutValidServiceKey() throws Exception {
        mockMvc.perform(get("/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Missing or invalid required header: X-User-Id"));
        verify(gameService, times(0)).findGameById(any(Long.class));
    }

    @Test
    @DisplayName("Get game by id - with invalid X-Service-Key header")
    void getGameByIdWithInvalidServiceKey() throws Exception {
        mockMvc.perform(get("/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", "invalid-key"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Missing or invalid required header: X-User-Id"));
        verify(gameService, times(0)).findGameById(any(Long.class));
    }

    @Test
    @DisplayName("Get game by id - when invalid id")
    void getGameByInvalidId() throws Exception {
        mockMvc.perform(get("/game/{id}", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Game id: 0 must be greater then 0"));
        mockMvc.perform(get("/internal/game/{id}", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Game id: 0 must be greater then 0"));
        verify(gameService, times(0)).findGameById(any(Long.class));
    }

    @Test
    @DisplayName("Get game list by user id - successfully")
    void getGamesByValidUserId() throws Exception {
        when(gameService.findGameListByUserId(userId)).thenReturn(List.of(existingGame));

        mockMvc.perform(get("/game/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", userId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0].title").value(existingGame.getTitle()));
        verify(gameService, times(1)).findGameListByUserId(userId);
    }

    @Test
    @DisplayName("Get game list by user id - invalid user id")
    void getGamesByInvalidUserId() throws Exception {

        mockMvc.perform(get("/game/user/{userId}", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", 0)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("User id: 0 must be greater then 0"));
        verify(gameService, times(0)).findGameListByUserId(any(Long.class));
    }

    @Test
    @DisplayName("Get game list by user id - when user id is not equal to path variable")
    void getGamesByUserIdWhenItIsNotEqualToPathVariable() throws Exception {

        mockMvc.perform(get("/game/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", 13)
                )
                .andExpect(status().isForbidden());
        verify(gameService, times(0)).findGameListByUserId(any(Long.class));
    }

    @Test
    @DisplayName("Get game list by user id - without X-User-Id header")
    void getGamesByUserIdWithoutUserIdHeader() throws Exception {
        mockMvc.perform(get("/game/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Missing required header: X-User-Id"));
        verify(gameService, times(0)).findGameListByUserId(any(Long.class));
    }

    @Test
    @DisplayName("Get game list by user id - without X-Service-Key header")
    void getGamesByUserIdWithoutServiceKeyHeader() throws Exception {
        mockMvc.perform(get("/game/user/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-User-Id", userId)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Missing or invalid required header: X-User-Id"));
        verify(gameService, times(0)).findGameListByUserId(any(Long.class));
    }

    @Test
    @DisplayName("Create new game - successfully")
    void createGameSuccessfully() throws Exception {
        when(gameService.saveGame(any(Game.class))).thenReturn(existingGame);
        mockMvc.perform(post("/game")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameDtoForSaving))
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", userId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(gameId))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    @DisplayName("Create new game - with invalid dto fields")
    void createGameWithInvalidDtoFields() throws Exception {
        gameDtoForSaving.setCondition("-");
        mockMvc.perform(post("/game")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameDtoForSaving))
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0]").value(containsString("condition: size must be")));
        verify(gameService, times(0)).saveGame(any(Game.class));
    }

    @Test
    @DisplayName("Create new game - with not null id")
    void createGameWithNotNullId() throws Exception {
        gameDtoForSaving.setId(1L);
        mockMvc.perform(post("/game")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameDtoForSaving))
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Game id: 1 must be null or 0"));
        verify(gameService, times(0)).saveGame(any(Game.class));
    }

    @Test
    @DisplayName("Create new game - with images with not null ids")
    void createGameWithImagesWithNotNullIds() throws Exception {
        gameDtoForSaving.setImages(generateExistingImageListForExistingGameDto());
        mockMvc.perform(post("/game")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameDtoForSaving))
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Image ids for new game must be null or 0"));
        verify(gameService, times(0)).saveGame(any(Game.class));
    }

    @Test
    @DisplayName("Create new game - when user id in header X-User-Id is not equal to game user id")
    void createGameWhenUserIdIsNotEqualToGameUserId() throws Exception {
        mockMvc.perform(post("/game")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameDtoForSaving))
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", 13))
                .andExpect(status().isForbidden());
        verify(gameService, times(0)).saveGame(any(Game.class));
    }

    @Test
    @DisplayName("Create new game - without X-Service-Key header")
    void createGameWithoutServiceKeyHeader() throws Exception {
        mockMvc.perform(post("/game")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameDtoForSaving))
                        .header("X-User-Id", userId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Missing or invalid required header: X-User-Id"));
        verify(gameService, times(0)).saveGame(any(Game.class));
    }

    @Test
    @DisplayName("Create new game - without X-User-Id header")
    void createGameWithoutUserIdHeader() throws Exception {
        mockMvc.perform(post("/game")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameDtoForSaving))
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Missing required header: X-User-Id"));
        verify(gameService, times(0)).saveGame(any(Game.class));
    }

    @Test
    @DisplayName("Update existing game - successfully")
    void updateGameSuccessfully() throws Exception {
        mockMvc.perform(put("/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingGameDto))
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", userId))
                .andExpect(status().isNoContent());
        verify(gameService, times(1)).updateGame(any(Game.class));
    }

    @Test
    @DisplayName("Update existing game - when service throws BadRequestException")
    void updateGameWhenServiceThrowsBadRequestException() throws Exception {
        doThrow(new BadRequestException("Game with id: 1 can't be updated because game status is not PUBLISHED"))
                .when(gameService).updateGame(any(Game.class));
        mockMvc.perform(put("/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingGameDto))
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$")
                        .value("Game with id: 1 can't be updated because game status is not PUBLISHED"));
        verify(gameService, times(1)).updateGame(any(Game.class));
    }

    @Test
    @DisplayName("Update existing game - with invalid dto fields")
    void updateGameWithInvalidDtoFields() throws Exception {
        existingGameDto.setCondition("-");
        mockMvc.perform(put("/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingGameDto))
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$.[0]").value(containsString("condition: size must be")));
        verify(gameService, times(0)).updateGame(any(Game.class));
    }

    @Test
    @DisplayName("Update existing game - when game id is not equal to path variable")
    void updateGameWhenGameIdIsNotEqualToPathVariable() throws Exception {
        mockMvc.perform(put("/game/{id}", 13)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingGameDto))
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest());
        verify(gameService, times(0)).updateGame(any(Game.class));
    }

    @Test
    @DisplayName("Update existing game - when game id path variable < 1")
    void updateGameWhenGameIdPathVariableIsInvalid() throws Exception {
        mockMvc.perform(put("/game/{id}", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingGameDto))
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", userId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Game id: 1 must be equal to path variable: 0"));
        verify(gameService, times(0)).updateGame(any(Game.class));
    }

    @Test
    @DisplayName("Update existing game - when user id is not equal to game user id")
    void updateGameWhenUserIdIsNotEqualToGameUserId() throws Exception {
        mockMvc.perform(put("/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingGameDto))
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", 13))
                .andExpect(status().isForbidden());
        verify(gameService, times(0)).updateGame(any(Game.class));
    }

    @Test
    @DisplayName("Update existing game - without X-Service-Key header")
    void updateGameWithoutServiceKeyHeader() throws Exception {
        mockMvc.perform(put("/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingGameDto))
                        .header("X-User-Id", userId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Missing or invalid required header: X-User-Id"));
        verify(gameService, times(0)).updateGame(any(Game.class));
    }

    @Test
    @DisplayName("Update existing game - without X-User-Id header")
    void updateGameWithoutUserIdHeader() throws Exception {
        mockMvc.perform(put("/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(existingGameDto))
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$").value("Missing required header: X-User-Id"));
        verify(gameService, times(0)).updateGame(any(Game.class));
    }

    @Test
    @DisplayName("Set game status to IN_AUCTION - successfully")
    void setStatusToInAuctionForGameWithValidId() throws Exception {
        mockMvc.perform(put("/internal/game/{id}/in_auction", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isNoContent());
        verify(gameService, times(1)).setStatusToInAuctionForGameWithId(gameId);
    }

    @Test
    @DisplayName("Set game status to IN_AUCTION - with invalid game id")
    void setStatusToInAuctionForGameWithInvalidId() throws Exception {
        mockMvc.perform(put("/internal/game/{id}/in_auction", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Game id: 0 must be greater then 0"));
        verify(gameService, times(0)).setStatusToInAuctionForGameWithId(any(Long.class));
    }

    @Test
    @DisplayName("Set game status to SOLD - successfully")
    void setStatusToSoldForGameWithValidId() throws Exception {
        mockMvc.perform(put("/internal/game/{id}/sold", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isNoContent());
        verify(gameService, times(1)).setStatusToSoldForGameWithId(gameId);
    }

    @Test
    @DisplayName("Set game status to SOLD - with invalid game id")
    void setStatusToSoldForGameWithInvalidId() throws Exception {
        mockMvc.perform(put("/internal/game/{id}/sold", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Game id: 0 must be greater then 0"));
        verify(gameService, times(0)).setStatusToSoldForGameWithId(any(Long.class));
    }

    @Test
    @DisplayName("Set game status to PUBLISHED - successfully")
    void setStatusToPublishedForGameWithValidId() throws Exception {
        mockMvc.perform(put("/internal/game/{id}/published", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isNoContent());
        verify(gameService, times(1)).setStatusToPublishedForGameWithId(gameId);
    }

    @Test
    @DisplayName("Set game status to PUBLISHED - with invalid game id")
    void setStatusToPublishedForGameWithInvalidId() throws Exception {
        mockMvc.perform(put("/internal/game/{id}/published", 0)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Game id: 0 must be greater then 0"));
        verify(gameService, times(0)).setStatusToPublishedForGameWithId(any(Long.class));
    }

    @Test
    @DisplayName("Delete game - successfully")
    void deleteGame() throws Exception {
        mockMvc.perform(delete("/internal/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isNoContent());
        verify(gameService, times(1)).deleteGameById(gameId);
    }
}
