package dev.boujemaaoui.chess.engine;

import java.util.List;

/** Lecture et écriture de la notation algébrique standard (SAN), par exemple « Nf3 », « exd5 » ou « O-O ». */
public final class San {

    private San() {
    }

    /** Écrit un coup légal de la position, avec « + » s'il donne échec et « # » s'il mate. */
    public static String format(Position position, Move move) {
        String san = formatWithoutCheck(position, move, MoveGenerator.legalMoves(position));
        Position next = position.play(move);
        if (!MoveGenerator.isInCheck(next)) {
            return san;
        }
        return san + (MoveGenerator.legalMoves(next).isEmpty() ? "#" : "+");
    }

    /**
     * Lit un coup en SAN et renvoie le coup légal correspondant. Les marques d'échec et de mat
     * finales sont facultatives.
     *
     * @throws IllegalMoveException si l'écriture ne correspond à aucun coup légal, ou est ambiguë
     */
    public static Move parse(Position position, String san) {
        if (san == null) {
            throw new IllegalMoveException("SAN move must not be null");
        }
        String expected = san.replaceAll("[+#]+$", "");
        List<Move> legalMoves = MoveGenerator.legalMoves(position);
        return legalMoves.stream()
                .filter(move -> formatWithoutCheck(position, move, legalMoves).equals(expected))
                .findFirst()
                .orElseThrow(() -> new IllegalMoveException(
                        "No legal move \"" + san + "\" in position " + Fen.format(position)));
    }

    private static String formatWithoutCheck(Position position, Move move, List<Move> legalMoves) {
        Square from = move.from();
        Square to = move.to();
        Piece piece = position.pieceAt(from)
                .orElseThrow(() -> new IllegalMoveException("No piece on " + from + " for move " + move));
        if (piece.type() == PieceType.KING && Math.abs(to.file() - from.file()) == 2) {
            return to.file() > from.file() ? "O-O" : "O-O-O";
        }

        boolean isPawn = piece.type() == PieceType.PAWN;
        boolean capture = position.pieceAt(to).isPresent() || (isPawn && from.file() != to.file());
        StringBuilder san = new StringBuilder();
        if (isPawn) {
            if (capture) {
                san.append(fileName(from));
            }
        } else {
            san.append(Character.toUpperCase(piece.type().symbol()))
                    .append(disambiguation(position, move, piece, legalMoves));
        }
        if (capture) {
            san.append('x');
        }
        san.append(to);
        move.promotion().ifPresent(type -> san.append('=').append(Character.toUpperCase(type.symbol())));
        return san.toString();
    }

    /**
     * Précise la case de départ quand une autre pièce identique peut aller sur la même case : la
     * colonne si elle suffit, sinon la rangée, sinon les deux.
     */
    private static String disambiguation(Position position, Move move, Piece piece, List<Move> legalMoves) {
        List<Square> rivals = legalMoves.stream()
                .filter(other -> other.to() == move.to() && other.from() != move.from())
                .map(Move::from)
                .filter(square -> position.pieceAt(square).orElseThrow().equals(piece))
                .toList();
        if (rivals.isEmpty()) {
            return "";
        }
        Square from = move.from();
        if (rivals.stream().noneMatch(square -> square.file() == from.file())) {
            return fileName(from);
        }
        if (rivals.stream().noneMatch(square -> square.rank() == from.rank())) {
            return String.valueOf(from.rank() + 1);
        }
        return from.toString();
    }

    private static String fileName(Square square) {
        return String.valueOf((char) ('a' + square.file()));
    }
}
