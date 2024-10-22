package com.bgauction.gameservice.controller;

import com.bgauction.gameservice.model.dto.GameDto;
import com.bgauction.gameservice.model.entity.Game;
import com.bgauction.gameservice.model.mapper.GameMapper;
import com.bgauction.gameservice.service.GameService;
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
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/game")
public class GameController {

    private final GameService gameService;
    private final GameMapper gameMapper;

    @GetMapping("/{id}")
    public ResponseEntity<?> getGameById(@PathVariable Long id) {
        Optional<GameDto> gameDto = gameService.findGameById(id).map(gameMapper::gameToGameDto);
        return gameDto.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GameDto>> getGamesByUserId(@PathVariable Long userId) {
        List<GameDto> games = gameService.findGameListByUserId(userId).stream().map(gameMapper::gameToGameDto).toList();
        return ResponseEntity.ok(games);
    }

    @PostMapping
    public ResponseEntity<?> createGame(@RequestBody GameDto gameDto) {
        if (gameDto.getId() != null) {
            return new ResponseEntity<>("Id must be null", HttpStatus.BAD_REQUEST);
        }
        if (gameDto.getImages() != null && !gameDto.getImages().stream().allMatch(image -> image.getId() == null)) {
            return new ResponseEntity<>("Image ids must be null", HttpStatus.BAD_REQUEST);
        }
        GameDto savedGame = gameMapper.gameToGameDto(gameService.saveGame(gameMapper.gameDtoToGame(gameDto)));
        return ResponseEntity.status(HttpStatus.CREATED).body(savedGame);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateGame(@PathVariable Long id, @RequestBody GameDto game) {
        if (id == null || id < 1 || !id.equals(game.getId())) {
            return new ResponseEntity<>("Id must be null", HttpStatus.BAD_REQUEST);
        }
        gameService.updateGame(gameMapper.gameDtoToGame(game));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGame(@PathVariable Long id) {
        gameService.deleteGameById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/user/{userId}")
    public ResponseEntity<?> deleteAllGamesByUserId(@PathVariable Long userId) {
        gameService.deleteAllGamesByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
