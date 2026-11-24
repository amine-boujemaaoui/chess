package dev.boujemaaoui.chess.engine;

/** Droits de roque encore disponibles pour chaque camp. */
public record CastlingRights(
        boolean whiteKingSide,
        boolean whiteQueenSide,
        boolean blackKingSide,
        boolean blackQueenSide) {

    public static final CastlingRights NONE = new CastlingRights(false, false, false, false);
    public static final CastlingRights ALL = new CastlingRights(true, true, true, true);

    /**
     * Droits restants après un coup qui part de cette case ou y arrive : un roi ou une tour qui
     * quitte sa case d'origine, ou une tour prise sur sa case d'origine, retire le roque concerné.
     */
    public CastlingRights revokedBy(Square square) {
        return switch (square) {
            case E1 -> new CastlingRights(false, false, blackKingSide, blackQueenSide);
            case H1 -> new CastlingRights(false, whiteQueenSide, blackKingSide, blackQueenSide);
            case A1 -> new CastlingRights(whiteKingSide, false, blackKingSide, blackQueenSide);
            case E8 -> new CastlingRights(whiteKingSide, whiteQueenSide, false, false);
            case H8 -> new CastlingRights(whiteKingSide, whiteQueenSide, false, blackQueenSide);
            case A8 -> new CastlingRights(whiteKingSide, whiteQueenSide, blackKingSide, false);
            default -> this;
        };
    }
}
