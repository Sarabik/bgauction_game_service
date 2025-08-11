package com.bgauction.gameservice.integration;

import com.bgauction.gameservice.exception.NotFoundException;
import com.bgauction.gameservice.model.dto.GameDto;
import com.bgauction.gameservice.model.dto.GameImageDto;
import com.bgauction.gameservice.model.entity.Game;
import com.bgauction.gameservice.model.entity.GameImage;
import com.bgauction.gameservice.model.entity.GameStatus;
import com.bgauction.gameservice.model.mapper.GameMapper;
import com.bgauction.gameservice.repository.GameRepository;
import com.bgauction.gameservice.service.GameService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.bgauction.gameservice.util.TestUtil.generateGame;
import static com.bgauction.gameservice.util.TestUtil.generateGameDto;
import static com.bgauction.gameservice.util.TestUtil.generateNewImageListForNewGame;
import static com.bgauction.gameservice.util.TestUtil.generateNewImageListForNewGameDto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class GameControllerIntegrationTests {

    @Value("${service.internal-key}")
    private String serviceInternalKey;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private GameService gameService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameMapper gameMapper;

    @Test
    @DisplayName("Get game by id")
    void getGameById() throws Exception {
        GameDto savedGameDto = saveGameDto();

        MvcResult result = mockMvc.perform(get("/game/{id}", savedGameDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        GameDto game = objectMapper.readValue(jsonResponse, new TypeReference<GameDto>() {});
        assertThat(game).isEqualTo(savedGameDto);
    }

    @Test
    @DisplayName("Get game list by user id")
    void getGamesByUserId() throws Exception {
        GameDto savedGameDto = saveGameDto();

        MvcResult result = mockMvc.perform(get("/game/user/{userId}", savedGameDto.getUserId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", savedGameDto.getUserId())
                )
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        List<GameDto> games = objectMapper.readValue(jsonResponse, new TypeReference<List<GameDto>>() {});
        assertThat(games).hasSize(1);
        assertThat(games.get(0)).isEqualTo(savedGameDto);
    }

    @Test
    @DisplayName("Create new game")
    void createGame() throws Exception {
        Long userId = 10000L;
        GameDto gameDtoForSaving = generateGameDto(
                null, userId, null, generateNewImageListForNewGameDto());
        MvcResult result = mockMvc.perform(post("/game")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameDtoForSaving))
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", userId))
                .andExpect(status().isCreated())
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        GameDto game = objectMapper.readValue(jsonResponse, new TypeReference<GameDto>() {});
        assertThat(game.getUserId()).isEqualTo(userId);
        assertThat(game.getId()).isPositive();
        assertThat(game.getTitle()).isEqualTo(gameDtoForSaving.getTitle());
        assertThat(game.getStatus()).isEqualTo(GameStatus.PUBLISHED);
        assertThat(game.getImages()).hasSize(2);
        assertThat(game.getImages().get(0).getId()).isPositive();
    }

    @Test
    @DisplayName("Update existing game")
    void updateGame() throws Exception {
        Long userId = 10000L;
        Game savedGame = gameService.saveGame(generateGame(
                null, userId, GameStatus.PUBLISHED, generateNewImageListForNewGame()));
        Long gameId = savedGame.getId();

        GameDto gameDtoForUpdate = gameMapper.gameToGameDto(savedGame);
        String title = "new title";
        gameDtoForUpdate.setTitle(title);
        List<GameImageDto> list = gameDtoForUpdate.getImages();
        list.remove(0);
        GameImageDto remainingGameImage = list.get(0);
        String url = "https://new/url";
        list.add(new GameImageDto(null, url));

        mockMvc.perform(put("/game/{id}", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameDtoForUpdate))
                        .header("X-Service-Key", serviceInternalKey)
                        .header("X-User-Id", userId))
                .andExpect(status().isNoContent());
        Game updated = gameRepository.findById(gameId).orElse(null);
        assertThat(updated).isNotNull();
        assertThat(updated.getId()).isEqualTo(gameId);
        assertThat(updated.getTitle()).isEqualTo(title);
        assertThat(updated.getImages()).hasSize(2);
        assertThat(updated.getImages().stream().map(GameImage::getUrl).toList())
                .containsExactly(url, remainingGameImage.getUrl());
    }

    @Test
    @DisplayName("Set game status to IN_AUCTION")
    void setStatusToInAuction() throws Exception {
        GameDto savedGameDto = saveGameDto();
        assertThat(savedGameDto.getStatus()).isEqualTo(GameStatus.PUBLISHED);
        mockMvc.perform(put("/internal/game/{id}/in_auction", savedGameDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isNoContent());
        assertThat(gameService.findGameById(savedGameDto.getId()).getStatus()).isEqualTo(GameStatus.IN_AUCTION);
    }

    @Test
    @DisplayName("Set game status to SOLD")
    void setStatusToSold() throws Exception {
        GameDto savedGameDto = saveGameDto();
        assertThat(savedGameDto.getStatus()).isEqualTo(GameStatus.PUBLISHED);
        mockMvc.perform(put("/internal/game/{id}/sold", savedGameDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isNoContent());
        assertThat(gameService.findGameById(savedGameDto.getId()).getStatus()).isEqualTo(GameStatus.SOLD);
    }

    @Test
    @DisplayName("Set game status to PUBLISHED")
    void setStatusToPublished() throws Exception {
        GameDto savedGameDto = saveGameDto();
        gameService.setStatusToInAuctionForGameWithId(savedGameDto.getId());
        assertThat(gameService.findGameById(savedGameDto.getId()).getStatus()).isEqualTo(GameStatus.IN_AUCTION);

        mockMvc.perform(put("/internal/game/{id}/published", savedGameDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isNoContent());
        assertThat(gameService.findGameById(savedGameDto.getId()).getStatus()).isEqualTo(GameStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Delete game")
    void deleteGame() throws Exception {
        GameDto savedGameDto = saveGameDto();
        mockMvc.perform(delete("/internal/game/{id}", savedGameDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Service-Key", serviceInternalKey))
                .andExpect(status().isNoContent());
        Long id = savedGameDto.getId();
        assertThatThrownBy(() -> gameService.findGameById(id))
                .isInstanceOf(NotFoundException.class);
    }

    private GameDto saveGameDto() {
        Long userId = 10000L;
        Game savedGame = gameService.saveGame(generateGame(
                null, userId, GameStatus.PUBLISHED, generateNewImageListForNewGame()));
        return gameMapper.gameToGameDto(savedGame);
    }
}
