package dev.boujemaaoui.chess.engine;

import java.util.EnumMap;
import java.util.Map;

/** Lecture et écriture de la notation Forsyth-Edwards (FEN). */
public final class Fen {

    public static final String STARTING_POSITION = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    private Fen() {
    }

    public static Position parse(String fen) {
        if (fen == null) {
            throw new InvalidFenException("FEN must not be null");
        }
        String[] fields = fen.trim().split("\\s+");
        if (fields.length != 6) {
            throw new InvalidFenException("FEN must have 6 fields, found " + fields.length + ": \"" + fen + "\"");
        }
        try {
            Color sideToMove = parseSideToMove(fields[1]);
            return new Position(
                    parsePieces(fields[0]),
                    sideToMove,
                    parseCastlingRights(fields[2]),
                    parseEnPassantSquare(fields[3], sideToMove),
                    parseCounter(fields[4], "halfmove clock"),
                    parseCounter(fields[5], "fullmove number"));
        } catch (InvalidFenException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw new InvalidFenException("Invalid FEN \"" + fen + "\": " + e.getMessage(), e);
        }
    }

    public static String format(Position position) {
        StringBuilder fen = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            int emptySquares = 0;
            for (int file = 0; file < 8; file++) {
                var piece = position.pieceAt(Square.of(file, rank));
                if (piece.isEmpty()) {
                    emptySquares++;
                    continue;
                }
                if (emptySquares > 0) {
                    fen.append(emptySquares);
                    emptySquares = 0;
                }
                fen.append(piece.get().toFenChar());
            }
            if (emptySquares > 0) {
                fen.append(emptySquares);
            }
            if (rank > 0) {
                fen.append('/');
            }
        }
        return fen.append(' ').append(position.sideToMove() == Color.WHITE ? 'w' : 'b')
                .append(' ').append(formatCastlingRights(position.castlingRights()))
                .append(' ').append(position.enPassantSquare().map(Square::toString).orElse("-"))
                .append(' ').append(position.halfmoveClock())
                .append(' ').append(position.fullmoveNumber())
                .toString();
    }

    private static Map<Square, Piece> parsePieces(String field) {
        String[] ranks = field.split("/", -1);
        if (ranks.length != 8) {
            throw new InvalidFenException("Piece placement must have 8 ranks, found " + ranks.length);
        }
        Map<Square, Piece> pieces = new EnumMap<>(Square.class);
        for (int i = 0; i < 8; i++) {
            int rank = 7 - i;
            int file = 0;
            for (char c : ranks[i].toCharArray()) {
                boolean emptySquares = c >= '1' && c <= '8';
                int width = emptySquares ? c - '0' : 1;
                if (file + width > 8) {
                    throw new InvalidFenException("Rank " + (rank + 1) + " has more than 8 squares");
                }
                if (!emptySquares) {
                    pieces.put(Square.of(file, rank), Piece.fromFenChar(c));
                }
                file += width;
            }
            if (file != 8) {
                throw new InvalidFenException("Rank " + (rank + 1) + " has " + file + " squares instead of 8");
            }
        }
        return pieces;
    }

    private static Color parseSideToMove(String field) {
        return switch (field) {
            case "w" -> Color.WHITE;
            case "b" -> Color.BLACK;
            default -> throw new InvalidFenException("Side to move must be 'w' or 'b', found \"" + field + "\"");
        };
    }

    private static CastlingRights parseCastlingRights(String field) {
        if (field.equals("-")) {
            return CastlingRights.NONE;
        }
        // Chaque lettre doit apparaître au plus une fois, dans l'ordre KQkq.
        if (!field.matches("K?Q?k?q?") || field.isEmpty()) {
            throw new InvalidFenException("Invalid castling rights: \"" + field + "\"");
        }
        return new CastlingRights(
                field.contains("K"), field.contains("Q"), field.contains("k"), field.contains("q"));
    }

    private static String formatCastlingRights(CastlingRights rights) {
        String field = (rights.whiteKingSide() ? "K" : "")
                + (rights.whiteQueenSide() ? "Q" : "")
                + (rights.blackKingSide() ? "k" : "")
                + (rights.blackQueenSide() ? "q" : "");
        return field.isEmpty() ? "-" : field;
    }

    private static Square parseEnPassantSquare(String field, Color sideToMove) {
        if (field.equals("-")) {
            return null;
        }
        Square square = Square.fromName(field);
        // La case est derrière le pion adverse qui vient d'avancer de deux cases.
        int expectedRank = sideToMove == Color.WHITE ? 5 : 2;
        if (square.rank() != expectedRank) {
            throw new InvalidFenException(
                    "En passant square must be on rank " + (expectedRank + 1) + ", found " + field);
        }
        return square;
    }

    private static int parseCounter(String field, String name) {
        try {
            return Integer.parseInt(field);
        } catch (NumberFormatException e) {
            throw new InvalidFenException("Invalid " + name + ": \"" + field + "\"", e);
        }
    }
}
