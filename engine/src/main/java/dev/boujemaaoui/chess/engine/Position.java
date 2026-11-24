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

    /**
     * Joue un coup et renvoie la nouvelle position ; celle-ci reste inchangée. Le coup doit être
     * pseudo-légal (voir {@link MoveGenerator}) : sa légalité n'est pas vérifiée ici.
     *
     * @throws IllegalArgumentException si la case de départ est vide
     */
    public Position play(Move move) {
        Square from = move.from();
        Square to = move.to();
        Piece piece = pieceAt(from)
                .orElseThrow(() -> new IllegalArgumentException("No piece on " + from + " for move " + move));
        boolean isPawn = piece.type() == PieceType.PAWN;

        Map<Square, Piece> board = new EnumMap<>(Square.class);
        board.putAll(pieces);
        board.remove(from);
        Piece captured = board.put(to, move.promotion().map(type -> new Piece(piece.color(), type)).orElse(piece));

        if (isPawn && to == enPassantSquare && from.file() != to.file()) {
            board.remove(Square.of(to.file(), from.rank()));
        }
        if (piece.type() == PieceType.KING && Math.abs(to.file() - from.file()) == 2) {
            boolean kingSide = to.file() > from.file();
            Square rookFrom = Square.of(kingSide ? 7 : 0, from.rank());
            Square rookTo = Square.of(kingSide ? 5 : 3, from.rank());
            board.put(rookTo, board.remove(rookFrom));
        }

        boolean doubleAdvance = isPawn && Math.abs(to.rank() - from.rank()) == 2;
        return new Position(
                board,
                sideToMove.opposite(),
                castlingRights.revokedBy(from).revokedBy(to),
                doubleAdvance ? Square.of(from.file(), (from.rank() + to.rank()) / 2) : null,
                isPawn || captured != null ? 0 : halfmoveClock + 1,
                sideToMove == Color.BLACK ? fullmoveNumber + 1 : fullmoveNumber);
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
