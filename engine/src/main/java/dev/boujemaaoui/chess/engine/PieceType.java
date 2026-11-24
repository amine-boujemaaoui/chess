package dev.boujemaaoui.chess.engine;

/** Type d'une pièce, indépendamment de sa couleur. */
public enum PieceType {
    PAWN('p'),
    KNIGHT('n'),
    BISHOP('b'),
    ROOK('r'),
    QUEEN('q'),
    KING('k');

    private final char symbol;

    PieceType(char symbol) {
        this.symbol = symbol;
    }

    /** Lettre minuscule utilisée par les notations FEN et UCI. */
    public char symbol() {
        return symbol;
    }

    public static PieceType fromSymbol(char symbol) {
        for (PieceType type : values()) {
            if (type.symbol == symbol) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown piece symbol: '" + symbol + "'");
    }
}
