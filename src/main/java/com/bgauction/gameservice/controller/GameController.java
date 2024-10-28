package com.bgauction.gameservice.controller;

import com.bgauction.gameservice.model.dto.GameDto;
import com.bgauction.gameservice.model.mapper.GameMapper;
import com.bgauction.gameservice.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;
    private final GameMapper gameMapper;
    private static final String GAME_ID_GREATER_THEN_0 = "Game id: %d must be greater then 0";
    private static final String GAME_ID_MUST_BE_NULL = "Game id: %d must be null or 0";
    private static final String USER_ID_GREATER_THEN_0 = "User id: %d must be greater then 0";
    private static final String IMAGE_ID_MUST_BE_NULL = "Image ids for new game must be null or 0";

    @GetMapping("/{id}")
    public ResponseEntity<?> getGameById(@PathVariable Long id) {
        if (id < 1) {
            return new ResponseEntity<>(String.format(GAME_ID_GREATER_THEN_0, id), HttpStatus.BAD_REQUEST);
        }
        GameDto gameDto = gameMapper.gameToGameDto(gameService.findGameById(id));
        return new ResponseEntity<>(gameDto, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getGamesByUserId(@PathVariable Long userId) {
        if (userId < 1) {
            return new ResponseEntity<>(String.format(USER_ID_GREATER_THEN_0, userId), HttpStatus.BAD_REQUEST);
        }
        List<GameDto> games = gameService.findGameListByUserId(userId).stream().map(gameMapper::gameToGameDto).toList();
        return ResponseEntity.ok(games);
    }

    @PostMapping
    public ResponseEntity<?> createGame(@Valid @RequestBody GameDto gameDto) {
        if (gameDto.getId() != null) {
            return new ResponseEntity<>(String.format(GAME_ID_MUST_BE_NULL, gameDto.getId()), HttpStatus.BAD_REQUEST);
        }
        if (gameDto.getImages() != null && !gameDto.getImages().stream().allMatch(image -> image.getId() == null)) {
            return new ResponseEntity<>(IMAGE_ID_MUST_BE_NULL, HttpStatus.BAD_REQUEST);
        }
        GameDto savedGame = gameMapper.gameToGameDto(gameService.saveGame(gameMapper.gameDtoToGame(gameDto)));
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGame);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGame(@PathVariable Long id, @Valid @RequestBody GameDto game) {
        if (id == null || id < 1 || !id.equals(game.getId())) {
            return new ResponseEntity<>(String.format(GAME_ID_MUST_BE_NULL, id), HttpStatus.BAD_REQUEST);
        }
        gameService.updateGame(gameMapper.gameDtoToGame(game));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/in_auction")
    public ResponseEntity<?> setStatusToInAuctionForGameWithId(@PathVariable Long id) {
        if (id < 1) {
            return new ResponseEntity<>(String.format(GAME_ID_GREATER_THEN_0, id), HttpStatus.BAD_REQUEST);
        }
        gameService.setStatusToInAuctionForGameWithId(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/sold")
    public ResponseEntity<?> setStatusToSoldForGameWithId(@PathVariable Long id) {
        if (id < 1) {
            return new ResponseEntity<>(String.format(GAME_ID_GREATER_THEN_0, id), HttpStatus.BAD_REQUEST);
        }
        gameService.setStatusToSoldForGameWithId(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/published")
    public ResponseEntity<?> setStatusToPublishedForGameWithId(@PathVariable Long id) {
        if (id < 1) {
            return new ResponseEntity<>(String.format(GAME_ID_GREATER_THEN_0, id), HttpStatus.BAD_REQUEST);
        }
        gameService.setStatusToPublishedForGameWithId(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGame(@PathVariable Long id) {
        gameService.deleteGameById(id);
        return ResponseEntity.noContent().build();
    }
}
