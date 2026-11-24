package dev.boujemaaoui.chess.engine;

import java.util.Optional;

/** Case de l'échiquier. Colonnes et rangées sont numérotées de 0 à 7 depuis a1. */
public enum Square {
    A1, B1, C1, D1, E1, F1, G1, H1,
    A2, B2, C2, D2, E2, F2, G2, H2,
    A3, B3, C3, D3, E3, F3, G3, H3,
    A4, B4, C4, D4, E4, F4, G4, H4,
    A5, B5, C5, D5, E5, F5, G5, H5,
    A6, B6, C6, D6, E6, F6, G6, H6,
    A7, B7, C7, D7, E7, F7, G7, H7,
    A8, B8, C8, D8, E8, F8, G8, H8;

    private static final Square[] VALUES = values();

    public int file() {
        return ordinal() % 8;
    }

    public int rank() {
        return ordinal() / 8;
    }

    public static Square of(int file, int rank) {
        if (!isOnBoard(file, rank)) {
            throw new IllegalArgumentException("Square out of board: file " + file + ", rank " + rank);
        }
        return VALUES[rank * 8 + file];
    }

    /** Lit un nom algébrique en minuscules, par exemple « e4 ». */
    public static Square fromName(String name) {
        if (name.length() != 2) {
            throw new IllegalArgumentException("Invalid square name: \"" + name + "\"");
        }
        int file = name.charAt(0) - 'a';
        int rank = name.charAt(1) - '1';
        if (!isOnBoard(file, rank)) {
            throw new IllegalArgumentException("Invalid square name: \"" + name + "\"");
        }
        return of(file, rank);
    }

    /** Case décalée du nombre de colonnes et de rangées donné, vide si elle sort de l'échiquier. */
    public Optional<Square> offset(int fileDelta, int rankDelta) {
        int file = file() + fileDelta;
        int rank = rank() + rankDelta;
        return isOnBoard(file, rank) ? Optional.of(of(file, rank)) : Optional.empty();
    }

    @Override
    public String toString() {
        return "" + (char) ('a' + file()) + (char) ('1' + rank());
    }

    private static boolean isOnBoard(int file, int rank) {
        return file >= 0 && file < 8 && rank >= 0 && rank < 8;
    }
}
