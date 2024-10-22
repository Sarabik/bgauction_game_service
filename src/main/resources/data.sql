INSERT INTO games (user_id, title, description, game_condition, language, min_players, max_players, status, created)
VALUES
(1, 'Catan', 'A strategy game for resource management and trading.', 'good', 'EN', 3, 4, 'PUBLISHED', CURRENT_TIMESTAMP),
(1, 'Ticket to Ride', 'A railway adventure game', 'excellent', 'RU', 2, 5, 'PUBLISHED', CURRENT_TIMESTAMP),
(2, 'Pandemic', 'A cooperative board game to stop a global outbreak', 'new', 'EN', 2, 4, 'PUBLISHED', CURRENT_TIMESTAMP),
(2, 'Carcassonne', 'A tile-based strategy game about building cities', 'like new', 'LV', 2, 5, 'PUBLISHED', CURRENT_TIMESTAMP);

INSERT INTO game_images (url, game_id)
VALUES
('https://boardgamegeek.com/image/540437/catan', 1),
('https://boardgamegeek.com/image/548861/catan', 1),
('https://boardgamegeek.com/image/103435/ticket-to-ride', 2),
('https://boardgamegeek.com/image/347643/pandemic', 3),
('https://boardgamegeek.com/image/166187/carcassonne', 4),
('https://boardgamegeek.com/image/170068/carcassonne', 4);
