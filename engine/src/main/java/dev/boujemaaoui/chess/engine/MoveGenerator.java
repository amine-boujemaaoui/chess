package dev.boujemaaoui.chess.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Génère les coups pseudo-légaux : coups conformes au déplacement des pièces, sans vérifier
 * que le roi du camp qui joue reste hors d'échec. Le roque et la prise en passant n'en font pas
 * encore partie.
 */
public final class MoveGenerator {

    private static final int[][] KNIGHT_OFFSETS = {{1, 2}, {2, 1}, {2, -1}, {1, -2}, {-1, -2}, {-2, -1}, {-2, 1}, {-1, 2}};
    private static final int[][] KING_OFFSETS = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};
    private static final int[][] ROOK_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] BISHOP_DIRECTIONS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final List<PieceType> PROMOTION_TYPES =
            List.of(PieceType.QUEEN, PieceType.ROOK, PieceType.BISHOP, PieceType.KNIGHT);

    private MoveGenerator() {
    }

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
                case KING -> addSteppingMoves(position, from, KING_OFFSETS, moves);
            }
        });
        return moves;
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
                    .filter(to -> isOccupiedBy(position, to, us.opposite()))
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

    private static boolean isOccupiedBy(Position position, Square square, Color color) {
        return position.pieceAt(square).filter(piece -> piece.color() == color).isPresent();
    }
}
