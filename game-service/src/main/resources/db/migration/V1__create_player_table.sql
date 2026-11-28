CREATE TABLE player (
    id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username      VARCHAR(20)              NOT NULL,
    password_hash VARCHAR(100)             NOT NULL,
    role          VARCHAR(10)              NOT NULL CHECK (role IN ('PLAYER', 'ADMIN')),
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL
);

-- « Alice » et « alice » désignent le même joueur.
CREATE UNIQUE INDEX player_username_unique ON player (LOWER(username));
