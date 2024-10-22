package com.bgauction.gameservice.service;

import com.bgauction.gameservice.model.entity.Game;

import java.util.List;
import java.util.Optional;

public interface GameService {
    Optional<Game> findGameById(Long id);
    List<Game> findGameListByUserId(Long userId);
    Game saveGame(Game game);
    void updateGame(Game game);
    void deleteGameById(Long id);
    void deleteAllGamesByUserId(Long user_id);
}
