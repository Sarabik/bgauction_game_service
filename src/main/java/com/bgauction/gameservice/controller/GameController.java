package com.bgauction.gameservice.controller;

import com.bgauction.gameservice.model.dto.GameDto;
import com.bgauction.gameservice.model.mapper.GameMapper;
import com.bgauction.gameservice.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GameMapper gameMapper;

    private static final String GAME_ID_GREATER_THEN_0 = "Game id: %d must be greater then 0";
    private static final String GAME_ID_MUST_BE_NULL = "Game id: %d must be null or 0";
    private static final String GAME_ID_MUST_EQUAL_TO_PATH_VARIABLE = "Game id: %d must be equal to path variable: %d";
    private static final String USER_ID_GREATER_THEN_0 = "User id: %d must be greater then 0";
    private static final String IMAGE_ID_MUST_BE_NULL = "Image ids for new game must be null or 0";

    @GetMapping({"/game/{id}", "/internal/game/{id}"})
    public ResponseEntity<?> getGameById(@PathVariable Long id) {
        if (id < 1) {
            return new ResponseEntity<>(String.format(GAME_ID_GREATER_THEN_0, id), HttpStatus.BAD_REQUEST);
        }
        GameDto gameDto = gameMapper.gameToGameDto(gameService.findGameById(id));
        return new ResponseEntity<>(gameDto, HttpStatus.OK);
    }

    @GetMapping("/game/user/{userId}")
    public ResponseEntity<?> getGamesByUserId(@PathVariable Long userId,
                                              @RequestHeader(value = "X-User-Id") Long id) {
        if (userId < 1) {
            return new ResponseEntity<>(String.format(USER_ID_GREATER_THEN_0, userId), HttpStatus.BAD_REQUEST);
        }
        if (!id.equals(userId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        List<GameDto> games = gameService.findGameListByUserId(userId).stream().map(gameMapper::gameToGameDto).toList();
        return ResponseEntity.ok(games);
    }

    @PostMapping("/game")
    public ResponseEntity<?> createGame(@Valid @RequestBody GameDto gameDto,
                                        BindingResult bindingResult,
                                        @RequestHeader(value = "X-User-Id") Long id) {
        if (bindingResult.hasErrors()) {
            return getValidationErrors(bindingResult);
        }
        if (gameDto.getId() != null && gameDto.getId() != 0) {
            return new ResponseEntity<>(String.format(GAME_ID_MUST_BE_NULL, gameDto.getId()), HttpStatus.BAD_REQUEST);
        }
        if (gameDto.getImages() != null && !gameDto.getImages().stream().allMatch(image -> image.getId() == null)) {
            return new ResponseEntity<>(IMAGE_ID_MUST_BE_NULL, HttpStatus.BAD_REQUEST);
        }
        if (!id.equals(gameDto.getUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        GameDto savedGame = gameMapper.gameToGameDto(gameService.saveGame(gameMapper.gameDtoToGame(gameDto)));
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGame);
    }

    @PutMapping("/game/{id}")
    public ResponseEntity<?> updateGame(@PathVariable Long id,
                                        @Valid @RequestBody GameDto game,
                                        BindingResult bindingResult,
                                        @RequestHeader(value = "X-User-Id") Long userId) {
        if (bindingResult.hasErrors()) {
            return getValidationErrors(bindingResult);
        }
        if (!id.equals(game.getId())) {
            return new ResponseEntity<>(String.format(GAME_ID_MUST_EQUAL_TO_PATH_VARIABLE, game.getId(), id), HttpStatus.BAD_REQUEST);
        }
        if (id < 1) {
            return new ResponseEntity<>(String.format(GAME_ID_GREATER_THEN_0, id), HttpStatus.BAD_REQUEST);
        }
        if (!userId.equals(game.getUserId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        gameService.updateGame(gameMapper.gameDtoToGame(game));
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/game/{id}/in_auction")
    public ResponseEntity<?> setStatusToInAuctionForGameWithId(@PathVariable Long id) {
        if (id < 1) {
            return new ResponseEntity<>(String.format(GAME_ID_GREATER_THEN_0, id), HttpStatus.BAD_REQUEST);
        }
        gameService.setStatusToInAuctionForGameWithId(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/game/{id}/sold")
    public ResponseEntity<?> setStatusToSoldForGameWithId(@PathVariable Long id) {
        if (id < 1) {
            return new ResponseEntity<>(String.format(GAME_ID_GREATER_THEN_0, id), HttpStatus.BAD_REQUEST);
        }
        gameService.setStatusToSoldForGameWithId(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/internal/game/{id}/published")
    public ResponseEntity<?> setStatusToPublishedForGameWithId(@PathVariable Long id) {
        if (id < 1) {
            return new ResponseEntity<>(String.format(GAME_ID_GREATER_THEN_0, id), HttpStatus.BAD_REQUEST);
        }
        gameService.setStatusToPublishedForGameWithId(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/internal/game/{id}")
    public ResponseEntity<?> deleteGame(@PathVariable Long id) {
        gameService.deleteGameById(id);
        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<?> getValidationErrors(BindingResult bindingResult) {
            List<String> errors = bindingResult.getFieldErrors().stream()
                    .map(error -> error.getField() + ": " + error.getDefaultMessage())
                    .toList();
            return ResponseEntity.badRequest().body(errors);
    }
}
