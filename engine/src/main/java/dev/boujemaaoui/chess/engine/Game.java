package dev.boujemaaoui.chess.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Partie d'échecs : suite des positions et des coups joués depuis une position de départ. Seuls les
 * coups légaux sont acceptés. La fin de partie (mat, pat, matériel insuffisant, règle des 50 coups,
 * triple répétition) est détectée après chaque coup et appliquée automatiquement.
 */
public final class Game {

    private final List<Position> positions = new ArrayList<>();
    private final List<Move> moves = new ArrayList<>();
    private final List<String> repetitionKeys = new ArrayList<>();
    private List<Move> legalMoves;
    private GameStatus status;

    public Game() {
        this(Fen.parse(Fen.STARTING_POSITION));
    }

    public Game(Position initialPosition) {
        record(Objects.requireNonNull(initialPosition, "initialPosition"));
    }

    /**
     * Joue un coup.
     *
     * @throws IllegalMoveException si le coup est illégal ou si la partie est terminée
     */
    public void play(Move move) {
        if (status.isOver()) {
            throw new IllegalMoveException("Game is over (" + status + "): " + move + " cannot be played");
        }
        if (!legalMoves.contains(move)) {
            throw new IllegalMoveException("Illegal move " + move + " in position " + Fen.format(position()));
        }
        moves.add(move);
        record(position().play(move));
    }

    public Position initialPosition() {
        return positions.getFirst();
    }

    public Position position() {
        return positions.getLast();
    }

    /** Positions successives : la position de départ, puis celle obtenue après chaque coup. */
    public List<Position> positions() {
        return Collections.unmodifiableList(positions);
    }

    public List<Move> moves() {
        return Collections.unmodifiableList(moves);
    }

    public GameStatus status() {
        return status;
    }

    public GameResult result() {
        return switch (status) {
            case ONGOING -> GameResult.ONGOING;
            case CHECKMATE -> position().sideToMove() == Color.WHITE ? GameResult.BLACK_WINS : GameResult.WHITE_WINS;
            case STALEMATE, INSUFFICIENT_MATERIAL, FIFTY_MOVE_RULE, THREEFOLD_REPETITION -> GameResult.DRAW;
        };
    }

    private void record(Position position) {
        legalMoves = MoveGenerator.legalMoves(position);
        positions.add(position);
        repetitionKeys.add(repetitionKey(position));
        status = computeStatus(position);
    }

    private GameStatus computeStatus(Position position) {
        if (legalMoves.isEmpty()) {
            return MoveGenerator.isInCheck(position) ? GameStatus.CHECKMATE : GameStatus.STALEMATE;
        }
        if (hasInsufficientMaterial(position)) {
            return GameStatus.INSUFFICIENT_MATERIAL;
        }
        if (position.halfmoveClock() >= 100) {
            return GameStatus.FIFTY_MOVE_RULE;
        }
        if (Collections.frequency(repetitionKeys, repetitionKeys.getLast()) >= 3) {
            return GameStatus.THREEFOLD_REPETITION;
        }
        return GameStatus.ONGOING;
    }

    /**
     * Deux positions sont identiques pour la répétition si elles ont les mêmes pièces, le même trait,
     * les mêmes droits de roque et les mêmes prises en passant possibles. Une case de prise en passant
     * sans pion pour l'exploiter ne compte donc pas.
     */
    private String repetitionKey(Position position) {
        String[] fields = Fen.format(position).split(" ");
        boolean enPassantPossible = position.enPassantSquare()
                .filter(square -> legalMoves.stream().anyMatch(move -> move.to() == square
                        && position.pieceAt(move.from()).orElseThrow().type() == PieceType.PAWN))
                .isPresent();
        return fields[0] + " " + fields[1] + " " + fields[2] + " " + (enPassantPossible ? fields[3] : "-");
    }

    /**
     * Matériel insuffisant pour mater, quel que soit le jeu : rois seuls, une seule pièce mineure, ou
     * uniquement des fous qui sont tous sur des cases de la même couleur.
     */
    private static boolean hasInsufficientMaterial(Position position) {
        List<Map.Entry<Square, Piece>> minorPieces = new ArrayList<>();
        for (Map.Entry<Square, Piece> entry : position.pieces().entrySet()) {
            switch (entry.getValue().type()) {
                case KING -> {
                }
                case KNIGHT, BISHOP -> minorPieces.add(entry);
                case PAWN, ROOK, QUEEN -> {
                    return false;
                }
            }
        }
        if (minorPieces.size() <= 1) {
            return true;
        }
        int squareColor = squareColor(minorPieces.getFirst().getKey());
        return minorPieces.stream().allMatch(entry -> entry.getValue().type() == PieceType.BISHOP
                && squareColor(entry.getKey()) == squareColor);
    }

    private static int squareColor(Square square) {
        return (square.file() + square.rank()) % 2;
    }
}
