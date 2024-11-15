package com.bgauction.gameservice.service.impl;

import com.bgauction.gameservice.exception.BadRequestException;
import com.bgauction.gameservice.exception.NotFoundException;
import com.bgauction.gameservice.model.entity.Game;
import com.bgauction.gameservice.model.entity.GameImage;
import com.bgauction.gameservice.model.entity.GameStatus;
import com.bgauction.gameservice.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.bgauction.gameservice.util.TestUtil.generateExistingImageListForExistingGame;
import static com.bgauction.gameservice.util.TestUtil.generateGame;
import static com.bgauction.gameservice.util.TestUtil.generateImageListForUpdatedGame;
import static com.bgauction.gameservice.util.TestUtil.generateImageListForUpdatingGame;
import static com.bgauction.gameservice.util.TestUtil.generateNewImageListForNewGame;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameServiceImplTest {
    @Mock
    private GameRepository gameRepository;

    @InjectMocks
    private GameServiceImpl gameService;

    private Game existingGame;
    private Game gameForSaving;
    private Game gameForUpdate;
    private Game updatedGame;
    private final Long gameId1 = 1L;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        existingGame = generateGame(gameId1, userId,GameStatus.PUBLISHED, generateExistingImageListForExistingGame());
        gameForSaving = generateGame(null, userId,null, generateNewImageListForNewGame());
        gameForUpdate = generateGame(gameId1, userId,null, generateImageListForUpdatingGame());
        updatedGame = generateGame(gameId1, userId,GameStatus.PUBLISHED, generateImageListForUpdatedGame());
    }

    @Test
    @DisplayName("Find game by id when exists")
    void findGameByIdReturnsGameWhenGameExists() {
        when(gameRepository.findById(gameId1)).thenReturn(Optional.of(existingGame));
        Game foundGame = gameService.findGameById(gameId1);
        assertThat(existingGame).isEqualTo(foundGame);
        verify(gameRepository, times(1)).findById(gameId1);
    }

    @Test
    @DisplayName("Find game by id when doesn't exist")
    void findGameByIdReturnsGameWhenGameDoesNotExist() {
        when(gameRepository.findById(gameId1)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> gameService.findGameById(gameId1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Game with id: 1 is not found");
    }

    @Test
    @DisplayName("Find game list by user id")
    void findGameListByUserId() {
        List<Game> expectedList = new ArrayList<>(Arrays.asList(existingGame));
        when(gameRepository.findAllByUserId(userId)).thenReturn(expectedList);
        List<Game> resultList =  gameService.findGameListByUserId(userId);
        assertThat(resultList).hasSize(1).containsExactlyInAnyOrderElementsOf(expectedList);
        verify(gameRepository, times(1)).findAllByUserId(userId);
    }

    @Test
    @DisplayName("Save new game")
    void saveNewGame() {
        when(gameRepository.save(any(Game.class))).thenReturn(existingGame);

        Game result = gameService.saveGame(gameForSaving);
        assertThat(existingGame).isEqualTo(result);

        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(1)).save(gameCaptor.capture());
        Game savedGame = gameCaptor.getValue();
        assertThat(savedGame.getStatus()).isEqualTo(GameStatus.PUBLISHED);
        assertThat(savedGame.getImages()).hasSize(2);
        assertThat(savedGame.getImages().get(0).getGame()).isEqualTo(savedGame);
        assertThat(savedGame.getImages().get(1).getGame()).isEqualTo(savedGame);
    }

    @Test
    @DisplayName("Update existing game when status is not PUBLISHED")
    void updateExistingGameWithInAuctionStatus() {
        existingGame.setStatus(GameStatus.IN_AUCTION);
        when(gameRepository.findById(gameId1)).thenReturn(Optional.of(existingGame));
        assertThatThrownBy(() -> gameService.updateGame(gameForUpdate))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Game with id: 1 can't be updated because game status is not PUBLISHED");
    }

    @Test
    @DisplayName("Update existing game when status is PUBLISHED")
    void updateExistingGameWithPublishedStatus() {
        when(gameRepository.findById(gameId1)).thenReturn(Optional.of(existingGame));
        when(gameRepository.save(any(Game.class))).thenReturn(updatedGame);

        gameService.updateGame(gameForUpdate);

        verify(gameRepository, times(1)).findById(gameId1);
        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(1)).save(gameCaptor.capture());
        Game savedGame = gameCaptor.getValue();
        assertThat(savedGame.getImages().stream().map(GameImage::getUrl).toList())
                .containsExactlyInAnyOrderElementsOf(updatedGame.getImages().stream().map(GameImage::getUrl).toList());
        assertThat(savedGame.getStatus()).isEqualTo(GameStatus.PUBLISHED);
        assertThat(savedGame.getImages()).hasSize(2);
    }

    @Test
    @DisplayName("Set game status to IN-AUCTION")
    void setStatusToInAuctionForGameWithIdTest() {
        when(gameRepository.findById(gameId1)).thenReturn(Optional.of(existingGame));
        assertThat(existingGame.getStatus()).isEqualTo(GameStatus.PUBLISHED);

        gameService.setStatusToInAuctionForGameWithId(gameId1);

        verify(gameRepository, times(1)).findById(gameId1);
        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(1)).save(gameCaptor.capture());
        Game savedGame = gameCaptor.getValue();
        assertThat(savedGame.getStatus()).isEqualTo(GameStatus.IN_AUCTION);
    }

    @Test
    @DisplayName("Set game status to SOLD")
    void setStatusToSoldForGameWithIdTest() {
        when(gameRepository.findById(gameId1)).thenReturn(Optional.of(existingGame));
        assertThat(existingGame.getStatus()).isEqualTo(GameStatus.PUBLISHED);

        gameService.setStatusToSoldForGameWithId(gameId1);

        verify(gameRepository, times(1)).findById(gameId1);
        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(1)).save(gameCaptor.capture());
        Game savedGame = gameCaptor.getValue();
        assertThat(savedGame.getStatus()).isEqualTo(GameStatus.SOLD);
    }

    @Test
    @DisplayName("Set game status to PUBLISHED")
    void setStatusToPublishedForGameWithIdTest() {
        existingGame.setStatus(GameStatus.IN_AUCTION);
        when(gameRepository.findById(gameId1)).thenReturn(Optional.of(existingGame));
        assertThat(existingGame.getStatus()).isEqualTo(GameStatus.IN_AUCTION);

        gameService.setStatusToPublishedForGameWithId(gameId1);

        verify(gameRepository, times(1)).findById(gameId1);
        ArgumentCaptor<Game> gameCaptor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository, times(1)).save(gameCaptor.capture());
        Game savedGame = gameCaptor.getValue();
        assertThat(savedGame.getStatus()).isEqualTo(GameStatus.PUBLISHED);
    }
}
