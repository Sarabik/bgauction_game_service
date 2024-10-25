package com.bgauction.gameservice.service.impl;

import com.bgauction.gameservice.exception.BadRequestException;
import com.bgauction.gameservice.exception.NotFoundException;
import com.bgauction.gameservice.model.entity.Game;
import com.bgauction.gameservice.model.entity.GameImage;
import com.bgauction.gameservice.model.entity.GameStatus;
import com.bgauction.gameservice.repository.GameRepository;
import com.bgauction.gameservice.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private static final String GAME_NOT_FOUND = "Game with id: %d is not found";
    private static final String GAME_CANT_BE_UPDATED = "Game with id: %d can't be updated because game status is not PUBLISHED";

    @Override
    public Game findGameById(Long id) {
        Optional<Game> optional = gameRepository.findById(id);
        if (optional.isEmpty()) {
            throw new NotFoundException(String.format(GAME_NOT_FOUND, id));
        }
        return optional.get();
    }

    @Override
    public List<Game> findGameListByUserId(Long userId) {
        return gameRepository.findAllByUserId(userId);
    }

    @Override
    public Game saveGame(Game game) {
        game.setStatus(GameStatus.PUBLISHED);
        if (!game.getImages().isEmpty()) {
            game.getImages().forEach(i -> i.setGame(game));
        }
        return gameRepository.save(game);
    }

    @Override
    public void updateGame(Game game) {
        Game existingGame = findGameById(game.getId());
        if (existingGame.getStatus() != GameStatus.PUBLISHED) {
            throw new BadRequestException(String.format(GAME_CANT_BE_UPDATED, game.getId()));
        }
        game.setStatus(existingGame.getStatus());
        updateGameImages(existingGame, game);
        gameRepository.save(game);
    }

    private void updateGameImages(Game existingGame, Game newGame) {
        List<GameImage> newImages = newGame.getImages();
        List<GameImage> oldImages = existingGame.getImages();

        oldImages.removeIf(existingImage ->
                newImages.stream().noneMatch(newImage -> newImage.getUrl() != null && newImage.getUrl().equals(existingImage.getUrl())));

        newImages.removeIf(newImage ->
                oldImages.stream().allMatch(oldImage -> oldImage.getUrl() != null && oldImage.getUrl().equals(newImage.getUrl())));

        newImages.addAll(oldImages);
        newImages.forEach(i -> i.setGame(newGame));
    }

    @Override
    public void setStatusToInAuctionForGameWithId(Long id) {
        changeGameStatus(id, GameStatus.IN_AUCTION);
    }

    @Override
    public void setStatusToSoldForGameWithId(Long id) {
        changeGameStatus(id, GameStatus.SOLD);
    }

    @Override
    public void setStatusToPublishedForGameWithId(Long id) {
        changeGameStatus(id, GameStatus.PUBLISHED);
    }

    private void changeGameStatus(Long id, GameStatus status) {
            Game game = findGameById(id);
            game.setStatus(status);
            gameRepository.save(game);
    }

    @Override
    public void deleteGameById(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new NotFoundException(String.format(GAME_NOT_FOUND, id));
        }
        gameRepository.deleteById(id);
    }

    @Override
    public void deleteAllGamesByUserId(Long userId) {
        gameRepository.deleteAllByUserId(userId);
    }
}
