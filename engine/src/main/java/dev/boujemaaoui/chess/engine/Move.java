package dev.boujemaaoui.chess.engine;

import java.util.Objects;
import java.util.Optional;

/** Coup d'une case à une autre, avec la pièce choisie en cas de promotion. */
public record Move(Square from, Square to, Optional<PieceType> promotion) {

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

    /** Notation UCI, par exemple « e2e4 » ou « e7e8q ». */
    @Override
    public String toString() {
        return from.toString() + to + promotion.map(type -> String.valueOf(type.symbol())).orElse("");
    }
}
