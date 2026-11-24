package dev.boujemaaoui.chess.engine;

/** Levée quand un coup est mal écrit, illégal dans la position, ou joué après la fin de la partie. */
public class IllegalMoveException extends IllegalArgumentException {

    public IllegalMoveException(String message) {
        super(message);
    }
}
