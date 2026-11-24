package dev.boujemaaoui.chess.engine;

/** Résultat d'une partie, avec son écriture PGN. */
public enum GameResult {
    WHITE_WINS("1-0"),
    BLACK_WINS("0-1"),
    DRAW("1/2-1/2"),
    ONGOING("*");

    private final String notation;

    GameResult(String notation) {
        this.notation = notation;
    }

    public String notation() {
        return notation;
    }
}
