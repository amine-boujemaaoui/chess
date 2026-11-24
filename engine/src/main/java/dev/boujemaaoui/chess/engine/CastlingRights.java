package dev.boujemaaoui.chess.engine;

/** Droits de roque encore disponibles pour chaque camp. */
public record CastlingRights(
        boolean whiteKingSide,
        boolean whiteQueenSide,
        boolean blackKingSide,
        boolean blackQueenSide) {

    public static final CastlingRights NONE = new CastlingRights(false, false, false, false);
    public static final CastlingRights ALL = new CastlingRights(true, true, true, true);
}
