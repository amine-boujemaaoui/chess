package dev.boujemaaoui.chess.engine;

/** Levée quand une chaîne FEN ne décrit pas une position valide. */
public class InvalidFenException extends IllegalArgumentException {

    public InvalidFenException(String message) {
        super(message);
    }

    public InvalidFenException(String message, Throwable cause) {
        super(message, cause);
    }
}
