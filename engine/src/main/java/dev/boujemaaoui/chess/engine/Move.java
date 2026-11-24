package dev.boujemaaoui.chess.engine;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/** Coup d'une case à une autre, avec la pièce choisie en cas de promotion. */
public record Move(Square from, Square to, Optional<PieceType> promotion) {

    private static final Pattern UCI = Pattern.compile("[a-h][1-8][a-h][1-8][qrbn]?");

    public Move {
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        Objects.requireNonNull(promotion, "promotion");
    }

    public Move(Square from, Square to) {
        this(from, to, Optional.empty());
    }

    public Move(Square from, Square to, PieceType promotion) {
        this(from, to, Optional.of(promotion));
    }

    /**
     * Lit un coup en notation UCI, par exemple « e2e4 » ou « e7e8q ». Seule l'écriture est vérifiée,
     * pas la légalité du coup.
     */
    public static Move fromUci(String uci) {
        if (uci == null || !UCI.matcher(uci).matches()) {
            throw new IllegalMoveException("Invalid UCI move: \"" + uci + "\"");
        }
        Square from = Square.fromName(uci.substring(0, 2));
        Square to = Square.fromName(uci.substring(2, 4));
        return uci.length() == 5 ? new Move(from, to, PieceType.fromSymbol(uci.charAt(4))) : new Move(from, to);
    }

    /** Notation UCI, par exemple « e2e4 » ou « e7e8q ». */
    public String toUci() {
        return from.toString() + to + promotion.map(type -> String.valueOf(type.symbol())).orElse("");
    }

    @Override
    public String toString() {
        return toUci();
    }
}
