package com.bgauction.gameservice.service;

import com.bgauction.gameservice.model.entity.Game;

import java.util.List;

public interface GameService {
    Game findGameById(Long id);
    List<Game> findGameListByUserId(Long userId);
    Game saveGame(Game game);
    void setStatusToInAuctionForGameWithId(Long id);
    void setStatusToSoldForGameWithId(Long id);
    void setStatusToPublishedForGameWithId(Long id);
    void updateGame(Game game);
    void deleteGameById(Long id);
}
