DROP TABLE IF EXISTS game_images;
DROP TABLE IF EXISTS games;

CREATE TABLE IF NOT EXISTS games (
    id BIGINT AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    game_condition TEXT NOT NULL,
    language VARCHAR(2) NOT NULL,
    min_players INT NOT NULL,
    max_players INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS game_images (
    id BIGINT AUTO_INCREMENT,
    url VARCHAR(2083) NOT NULL,
    game_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_game
    FOREIGN KEY (game_id) REFERENCES games(id)
);
