package dev.boujemaaoui.chess.engine;

/** Couleur d'un joueur ou d'une pièce. */
public enum Color {
    WHITE,
    BLACK;

    public Color opposite() {
        return this == WHITE ? BLACK : WHITE;
    }
}
