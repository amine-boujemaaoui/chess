package dev.boujemaaoui.chess.engine;

import java.util.Objects;

/** Pièce de l'échiquier : un type et une couleur. */
public record Piece(Color color, PieceType type) {

    public Piece {
        Objects.requireNonNull(color, "color");
        Objects.requireNonNull(type, "type");
    }

    /** Lit une lettre FEN : majuscule pour les blancs, minuscule pour les noirs. */
    public static Piece fromFenChar(char fenChar) {
        Color color = Character.isUpperCase(fenChar) ? Color.WHITE : Color.BLACK;
        return new Piece(color, PieceType.fromSymbol(Character.toLowerCase(fenChar)));
    }

    public char toFenChar() {
        return color == Color.WHITE ? Character.toUpperCase(type.symbol()) : type.symbol();
    }
}
