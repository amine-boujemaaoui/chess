package dev.boujemaaoui.chess.engine;

/** État d'une partie : en cours, ou terminée et pour quelle raison. */
public enum GameStatus {
    ONGOING,
    CHECKMATE,
    STALEMATE,
    INSUFFICIENT_MATERIAL,
    FIFTY_MOVE_RULE,
    THREEFOLD_REPETITION;

    public boolean isOver() {
        return this != ONGOING;
    }
}
