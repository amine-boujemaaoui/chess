package dev.boujemaaoui.chess.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Génère les coups d'une position et détecte les échecs. */
public final class MoveGenerator {

    private static final int[][] KNIGHT_OFFSETS = {{1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}};
    private static final int[][] KING_OFFSETS = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
    private static final int[][] ROOK_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] BISHOP_DIRECTIONS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final List<PieceType> PROMOTION_TYPES =
            List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT);

    private MoveGenerator() {
    }

    /** Coups légaux du camp qui a le trait : ceux qui ne laissent pas son roi en échec. */
    public static List<Move> legalMoves(Position position) {
        Color us = position.sideToMove();
        return pseudoLegalMoves(position).stream()
                .filter(move -> !isKingAttacked(position.play(move), us))
                .toList();
    }

    /**
     * Coups conformes au déplacement des pièces, sans vérifier que le roi reste hors d'échec. Seul
     * le roque tient déjà compte des attaques : il est exclu si le roi est en échec ou traverse une
     * case attaquée.
     */
    public static List<Move> pseudoLegalMoves(Position position) {
        List<Move> moves = new ArrayList<>();
        Color us = position.sideToMove();
        position.pieces().forEach((from, piece) -> {
            if (piece.color() != us) {
                return;
            }
            switch (piece.type()) {
                case PAWN -> addPawnMoves(position, from, moves);
                case KNIGHT -> addSteppingMoves(position, from, KNIGHT_OFFSETS, moves);
                case BISHOP -> addSlidingMoves(position, from, BISHOP_DIRECTIONS, moves);
                case ROOK -> addSlidingMoves(position, from, ROOK_DIRECTIONS, moves);
                case QUEEN -> {
                    addSlidingMoves(position, from, BISHOP_DIRECTIONS, moves);
                    addSlidingMoves(position, from, ROOK_DIRECTIONS, moves);
                }
                case KING -> {
                    addSteppingMoves(position, from, KING_OFFSETS, moves);
                    addCastlingMoves(position, from, moves);
                }
            }
        });
        return moves;
    }

    /** Indique si le roi du camp qui a le trait est en échec. */
    public static boolean isInCheck(Position position) {
        return isKingAttacked(position, position.sideToMove());
    }

    /** Indique si une pièce de la couleur {@code attacker} attaque la case donnée. */
    public static boolean isSquareAttacked(Position position, Square square, Color attacker) {
        // Un pion attaque en diagonale vers l'avant : on regarde donc une rangée en arrière de son point de vue.
        int pawnRank = attacker == Color.WHITE ? -1 : 1;
        Piece pawn = new Piece(attacker, PieceType.PAWN);
        if (hasPiece(position, square.offset(-1, pawnRank), pawn) || hasPiece(position, square.offset(1, pawnRank), pawn)) {
            return true;
        }
        for (int[] offset : KNIGHT_OFFSETS) {
            if (hasPiece(position, square.offset(offset[0], offset[1]), new Piece(attacker, PieceType.KNIGHT))) {
                return true;
            }
        }
        for (int[] offset : KING_OFFSETS) {
            if (hasPiece(position, square.offset(offset[0], offset[1]), new Piece(attacker, PieceType.KING))) {
                return true;
            }
        }
        return isAttackedBySlider(position, square, BISHOP_DIRECTIONS, attacker, PieceType.BISHOP)
                || isAttackedBySlider(position, square, ROOK_DIRECTIONS, attacker, PieceType.ROOK);
    }

    private static boolean isKingAttacked(Position position, Color color) {
        Piece king = new Piece(color, PieceType.KING);
        return position.pieces().entrySet().stream()
                .filter(entry -> entry.getValue().equals(king))
                .findFirst()
                .map(entry -> isSquareAttacked(position, entry.getKey(), color.opposite()))
                .orElse(false);
    }

    /** Cherche, dans chaque direction, la première pièce rencontrée : une pièce glissante ou une dame adverse. */
    private static boolean isAttackedBySlider(
            Position position, Square square, int[][] directions, Color attacker, PieceType slider) {
        for (int[] direction : directions) {
            Optional<Square> next = square.offset(direction[0], direction[1]);
            while (next.isPresent()) {
                Optional<Piece> occupant = position.pieceAt(next.get());
                if (occupant.isPresent()) {
                    Piece piece = occupant.get();
                    if (piece.color() == attacker && (piece.type() == slider || piece.type() == PieceType.QUEEN)) {
                        return true;
                    }
                    break;
                }
                next = next.get().offset(direction[0], direction[1]);
            }
        }
        return false;
    }

    private static boolean hasPiece(Position position, Optional<Square> square, Piece piece) {
        return square.flatMap(position::pieceAt).filter(piece::equals).isPresent();
    }

    private static void addSteppingMoves(Position position, Square from, int[][] offsets, List<Move> moves) {
        for (int[] offset : offsets) {
            from.offset(offset[0], offset[1])
                    .filter(to -> !isOccupiedBy(position, to, position.sideToMove()))
                    .ifPresent(to -> moves.add(new Move(from, to)));
        }
    }

    private static void addSlidingMoves(Position position, Square from, int[][] directions, List<Move> moves) {
        for (int[] direction : directions) {
            Optional<Square> next = from.offset(direction[0], direction[1]);
            while (next.isPresent()) {
                Square to = next.get();
                Optional<Piece> occupant = position.pieceAt(to);
                if (occupant.isPresent()) {
                    if (occupant.get().color() != position.sideToMove()) {
                        moves.add(new Move(from, to));
                    }
                    break;
                }
                moves.add(new Move(from, to));
                next = to.offset(direction[0], direction[1]);
            }
        }
    }

    private static void addPawnMoves(Position position, Square from, List<Move> moves) {
        Color us = position.sideToMove();
        int forward = us == Color.WHITE ? 1 : -1;
        int startingRank = us == Color.WHITE ? 1 : 6;

        from.offset(0, forward)
                .filter(to -> position.pieceAt(to).isEmpty())
                .ifPresent(to -> {
                    addPawnMove(from, to, moves);
                    if (from.rank() == startingRank) {
                        to.offset(0, forward)
                                .filter(twoAhead -> position.pieceAt(twoAhead).isEmpty())
                                .ifPresent(twoAhead -> moves.add(new Move(from, twoAhead)));
                    }
                });

        for (int side : new int[] {-1, 1}) {
            from.offset(side, forward)
                    .filter(to -> isOccupiedBy(position, to, us.opposite())
                            || position.enPassantSquare().filter(to::equals).isPresent())
                    .ifPresent(to -> addPawnMove(from, to, moves));
        }
    }

    private static void addPawnMove(Square from, Square to, List<Move> moves) {
        if (to.rank() == 0 || to.rank() == 7) {
            PROMOTION_TYPES.forEach(type -> moves.add(new Move(from, to, type)));
        } else {
            moves.add(new Move(from, to));
        }
    }

    private static void addCastlingMoves(Position position, Square kingSquare, List<Move> moves) {
        Color us = position.sideToMove();
        CastlingRights rights = position.castlingRights();
        boolean kingSide = us == Color.WHITE ? rights.whiteKingSide() : rights.blackKingSide();
        boolean queenSide = us == Color.WHITE ? rights.whiteQueenSide() : rights.blackQueenSide();
        Square origin = us == Color.WHITE ? Square.E1 : Square.E8;
        if (kingSquare != origin || (!kingSide && !queenSide) || isSquareAttacked(position, origin, us.opposite())) {
            return;
        }
        if (kingSide) {
            addCastlingMove(position, origin, 7, moves);
        }
        if (queenSide) {
            addCastlingMove(position, origin, 0, moves);
        }
    }

    /**
     * Ajoute le roque vers la tour de la colonne donnée si les cases entre le roi et la tour sont
     * vides et si la case traversée par le roi n'est pas attaquée. La case d'arrivée est vérifiée
     * par {@link #legalMoves}, comme pour tout coup du roi.
     */
    private static void addCastlingMove(Position position, Square origin, int rookFile, List<Move> moves) {
        Color us = position.sideToMove();
        int rank = origin.rank();
        if (!position.pieceAt(Square.of(rookFile, rank)).equals(Optional.of(new Piece(us, PieceType.ROOK)))) {
            return;
        }
        int step = rookFile > origin.file() ? 1 : -1;
        for (int file = origin.file() + step; file != rookFile; file += step) {
            if (position.pieceAt(Square.of(file, rank)).isPresent()) {
                return;
            }
        }
        if (isSquareAttacked(position, Square.of(origin.file() + step, rank), us.opposite())) {
            return;
        }
        moves.add(new Move(origin, Square.of(origin.file() + 2 * step, rank)));
    }

    private static boolean isOccupiedBy(Position position, Square square, Color color) {
        return position.pieceAt(square).filter(piece -> piece.color() == color).isPresent();
    }
}
