package dev.boujemaaoui.chess.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Export d'une partie au format PGN (Portable Game Notation). */
public final class Pgn {

    private static final int MAX_LINE_LENGTH = 79;

    private Pgn() {
    }

    /**
     * Écrit la partie en PGN. Les sept en-têtes obligatoires sont toujours présents, avec « ? » quand
     * ils ne sont pas fournis ; l'en-tête {@code Result} est toujours celui de la partie. Une position
     * de départ autre que la position initiale est indiquée par les en-têtes {@code SetUp} et {@code FEN}.
     *
     * @param tags en-têtes à écrire, dans l'ordre de la map pour ceux qui ne sont pas obligatoires
     */
    public static String format(Game game, Map<String, String> tags) {
        Map<String, String> allTags = new LinkedHashMap<>();
        allTags.put("Event", "?");
        allTags.put("Site", "?");
        allTags.put("Date", "????.??.??");
        allTags.put("Round", "?");
        allTags.put("White", "?");
        allTags.put("Black", "?");
        allTags.put("Result", "?");
        allTags.putAll(tags);
        allTags.put("Result", game.result().notation());
        String initialFen = Fen.format(game.initialPosition());
        if (!initialFen.equals(Fen.STARTING_POSITION)) {
            allTags.put("SetUp", "1");
            allTags.put("FEN", initialFen);
        }

        StringBuilder pgn = new StringBuilder();
        allTags.forEach((name, value) -> pgn.append('[').append(name).append(" \"").append(escape(value)).append("\"]\n"));
        return pgn.append('\n').append(movetext(game)).append('\n').toString();
    }

    private static String movetext(Game game) {
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < game.moves().size(); i++) {
            Position before = game.positions().get(i);
            if (before.sideToMove() == Color.WHITE) {
                tokens.add(before.fullmoveNumber() + ".");
            } else if (i == 0) {
                tokens.add(before.fullmoveNumber() + "...");
            }
            tokens.add(San.format(before, game.moves().get(i)));
        }
        tokens.add(game.result().notation());
        return wrap(tokens);
    }

    private static String wrap(List<String> tokens) {
        StringBuilder text = new StringBuilder();
        int lineLength = 0;
        for (String token : tokens) {
            if (lineLength > 0 && lineLength + 1 + token.length() > MAX_LINE_LENGTH) {
                text.append('\n');
                lineLength = 0;
            } else if (lineLength > 0) {
                text.append(' ');
                lineLength++;
            }
            text.append(token);
            lineLength += token.length();
        }
        return text.toString();
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
