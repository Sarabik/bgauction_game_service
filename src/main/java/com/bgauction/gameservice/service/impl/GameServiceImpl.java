package com.bgauction.gameservice.service.impl;

import com.bgauction.gameservice.model.entity.Game;
import com.bgauction.gameservice.model.entity.GameImage;
import com.bgauction.gameservice.model.entity.GameStatus;
import com.bgauction.gameservice.repository.GameRepository;
import com.bgauction.gameservice.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Log4j2
@Repository
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;

    @Override
    public Optional<Game> findGameById(Long id) {
        return gameRepository.findById(id);
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
        Long id = game.getId();
        if (id != null && gameRepository.existsById(id)) {
            Game existingGame = gameRepository.findById(id).orElseThrow(() ->
                    new RuntimeException("Game with ID " + id + " does not exist"));
            game.setStatus(existingGame.getStatus());
            updateGameImages(existingGame, game);

            gameRepository.save(game);
        } else {
            throw new RuntimeException("Game with ID " + game.getId() + " does not exist");
        }
    }

    private void updateGameImages(Game existingGame, Game newGame) {
        List<GameImage> newImages = newGame.getImages();
        log.info("newImages: {}", newImages.stream().map(i -> i.getUrl()).toList());
        List<GameImage> oldImages = existingGame.getImages();
        log.info("oldImages: {}", oldImages.stream().map(i -> i.getUrl()).toList());

        oldImages.removeIf(existingImage ->
                newImages.stream().noneMatch(newImage -> newImage.getUrl() != null && newImage.getUrl().equals(existingImage.getUrl())));

        newImages.removeIf(newImage ->
                oldImages.stream().allMatch(oldImage -> oldImage.getUrl() != null && oldImage.getUrl().equals(newImage.getUrl())));

        newImages.addAll(oldImages);
        newImages.forEach(i -> i.setGame(newGame));
        log.info("newImages updated: {}", newImages.stream().map(i -> i.getUrl()).toList());
    }

    @Override
    public void deleteGameById(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new RuntimeException("Game with ID " + id + " does not exist");
        }
        gameRepository.deleteById(id);
    }

    @Override
    public void deleteAllGamesByUserId(Long userId) {
        gameRepository.deleteAllByUserId(userId);
    }
}
