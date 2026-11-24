package dev.boujemaaoui.chess.engine;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Position immuable : placement des pièces et état nécessaire pour continuer la partie. */
public final class Position {

    private final Map<Square, Piece> pieces;
    private final Color sideToMove;
    private final CastlingRights castlingRights;
    private final Square enPassantSquare;
    private final int halfmoveClock;
    private final int fullmoveNumber;

    /**
     * @param enPassantSquare case de prise en passant, ou {@code null} s'il n'y en a pas
     */
    public Position(
            Map<Square, Piece> pieces,
            Color sideToMove,
            CastlingRights castlingRights,
            Square enPassantSquare,
            int halfmoveClock,
            int fullmoveNumber) {
        if (halfmoveClock < 0) {
            throw new IllegalArgumentException("Halfmove clock must not be negative: " + halfmoveClock);
        }
        if (fullmoveNumber < 1) {
            throw new IllegalArgumentException("Fullmove number must be at least 1: " + fullmoveNumber);
        }
        Map<Square, Piece> copy = new EnumMap<>(Square.class);
        copy.putAll(pieces);
        this.pieces = Collections.unmodifiableMap(copy);
        this.sideToMove = Objects.requireNonNull(sideToMove, "sideToMove");
        this.castlingRights = Objects.requireNonNull(castlingRights, "castlingRights");
        this.enPassantSquare = enPassantSquare;
        this.halfmoveClock = halfmoveClock;
        this.fullmoveNumber = fullmoveNumber;
    }

    public Optional<Piece> pieceAt(Square square) {
        return Optional.ofNullable(pieces.get(square));
    }

    /** Pièces présentes sur l'échiquier, indexées par case. */
    public Map<Square, Piece> pieces() {
        return pieces;
    }

    public Color sideToMove() {
        return sideToMove;
    }

    public CastlingRights castlingRights() {
        return castlingRights;
    }

    public Optional<Square> enPassantSquare() {
        return Optional.ofNullable(enPassantSquare);
    }

    /** Nombre de demi-coups depuis la dernière prise ou le dernier coup de pion. */
    public int halfmoveClock() {
        return halfmoveClock;
    }

    public int fullmoveNumber() {
        return fullmoveNumber;
    }
}
