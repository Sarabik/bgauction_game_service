package com.bgauction.gameservice.util;

import com.bgauction.gameservice.model.dto.GameDto;
import com.bgauction.gameservice.model.dto.GameImageDto;
import com.bgauction.gameservice.model.entity.Game;
import com.bgauction.gameservice.model.entity.GameImage;
import com.bgauction.gameservice.model.entity.GameLanguage;
import com.bgauction.gameservice.model.entity.GameStatus;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {
    public static Game generateGame(Long gameId, Long userId, GameStatus status, List<GameImage> imageList) {
        int count = 0;
        if (gameId != null) {
            count = Math.toIntExact(gameId);
        }
        return Game.builder()
                .id(gameId)
                .userId(userId)
                .title("title" + count)
                .description("description" + count)
                .condition("condition" + count)
                .language(GameLanguage.LV)
                .minPlayers(2)
                .maxPlayers(5)
                .status(status)
                .images(imageList)
                .build();
    }

    public static GameDto generateGameDto(Long gameId, Long userId, GameStatus status, List<GameImageDto> imageList) {
        int count = 0;
        if (gameId != null) {
            count = Math.toIntExact(gameId);
        }
        return GameDto.builder()
                .id(gameId)
                .userId(userId)
                .title("title" + count)
                .description("description" + count)
                .condition("condition" + count)
                .language(GameLanguage.LV)
                .minPlayers(2)
                .maxPlayers(5)
                .status(status)
                .images(imageList)
                .build();
    }

    public static List<GameImageDto> generateExistingImageListForExistingGameDto() {
        List<GameImageDto> imageList = new ArrayList<>();
        imageList.add(new GameImageDto(1L, "https://boardgamegeek.com/image/54043754"));
        imageList.add(new GameImageDto(2L, "https://boardgamegeek.com/image/53525566"));
        return imageList;
    }

    public static List<GameImage> generateNewImageListForNewGame() {
        List<GameImage> imageList = new ArrayList<>();
        imageList.add(new GameImage(null, "https://boardgamegeek.com/image/54043754", null));
        imageList.add(new GameImage(null, "https://boardgamegeek.com/image/53525566", null));
        return imageList;
    }

    public static List<GameImageDto> generateNewImageListForNewGameDto() {
        List<GameImageDto> imageList = new ArrayList<>();
        imageList.add(new GameImageDto(null, "https://boardgamegeek.com/image/54043754"));
        imageList.add(new GameImageDto(null, "https://boardgamegeek.com/image/53525566"));
        return imageList;
    }

    public static List<GameImage> generateExistingImageListForExistingGame() {
        List<GameImage> imageList = new ArrayList<>();
        imageList.add(new GameImage(1L, "https://boardgamegeek.com/image/54043754", null));
        imageList.add(new GameImage(2L, "https://boardgamegeek.com/image/53525566", null));
        return imageList;
    }

    public static List<GameImage> generateImageListForUpdatingGame() {
        List<GameImage> imageList = new ArrayList<>();
        imageList.add(new GameImage(null, "https://boardgamegeek.com/image/72234231", null));
        imageList.add(new GameImage(null, "https://boardgamegeek.com/image/53525566", null));
        return imageList;
    }

    public static List<GameImage> generateImageListForUpdatedGame() {
        List<GameImage> imageList = new ArrayList<>();
        imageList.add(new GameImage(3L, "https://boardgamegeek.com/image/72234231", null));
        imageList.add(new GameImage(2L, "https://boardgamegeek.com/image/53525566", null));
        return imageList;
    }
}
