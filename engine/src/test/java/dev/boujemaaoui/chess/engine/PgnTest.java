package dev.boujemaaoui.chess.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PgnTest {

    /** Partie de l'Opéra, Morphy contre le duc de Brunswick et le comte Isouard, Paris, 1858. */
    private static final String OPERA_GAME = "1. e4 e5 2. Nf3 d6 3. d4 Bg4 4. dxe5 Bxf3 5. Qxf3 dxe5 6. Bc4 Nf6"
            + " 7. Qb3 Qe7 8. Nc3 c6 9. Bg5 b5 10. Nxb5 cxb5 11. Bxb5+ Nbd7 12. O-O-O Rd8"
            + " 13. Rxd7 Rxd7 14. Rd1 Qe6 15. Bxd7+ Nxd7 16. Qb8+ Nxb8 17. Rd8# 1-0";

    @Test
    void formatsEmptyGameWithSevenTagRoster() {
        assertThat(Pgn.format(new Game(), Map.of())).isEqualTo("""
            [Event "?"]
            [Site "?"]
            [Date "????.??.??"]
            [Round "?"]
            [White "?"]
            [Black "?"]
            [Result "*"]

            *
            """);
    }

    @Test
    void formatsFinishedGameWithTags() {
        Game game = new Game();
        for (String san : new String[] {"f3", "e5", "g4", "Qh4#"}) {
            game.play(San.parse(game.position(), san));
        }
        Map<String, String> tags = new LinkedHashMap<>();
        tags.put("White", "Alice");
        tags.put("Black", "Bob");
        tags.put("TimeControl", "300+0");

        assertThat(Pgn.format(game, tags)).isEqualTo("""
            [Event "?"]
            [Site "?"]
            [Date "????.??.??"]
            [Round "?"]
            [White "Alice"]
            [Black "Bob"]
            [Result "0-1"]
            [TimeControl "300+0"]

            1. f3 e5 2. g4 Qh4# 0-1
            """);
    }

    @Test
    void wrapsLongMovetextAtEightyCharacters() {
        Game game = new Game();
        for (String token : OPERA_GAME.split(" ")) {
            if (!token.matches("\\d+\\.|1-0")) {
                game.play(San.parse(game.position(), token));
            }
        }

        String movetext = Pgn.format(game, Map.of()).split("\n\n")[1].strip();

        assertThat(movetext.lines()).allSatisfy(line -> assertThat(line.length()).isLessThan(80));
        assertThat(String.join(" ", movetext.lines().toList())).isEqualTo(OPERA_GAME);
    }

    @Test
    void recordsCustomStartingPosition() {
        String fen = "4k3/8/8/8/8/8/4P3/4K3 b - - 0 12";
        Game game = new Game(Fen.parse(fen));
        game.play(Move.fromUci("e8d7"));
        game.play(Move.fromUci("e2e4"));

        String pgn = Pgn.format(game, Map.of());

        assertThat(pgn).contains("[SetUp \"1\"]\n[FEN \"" + fen + "\"]\n");
        assertThat(pgn).endsWith("\n12... Kd7 13. e4 *\n");
    }

    @Test
    void escapesQuotesAndBackslashesInTagValues() {
        String pgn = Pgn.format(new Game(), Map.of("White", "Le \"Roi\" \\ Fou"));

        assertThat(Arrays.asList(pgn.split("\n"))).contains("[White \"Le \\\"Roi\\\" \\\\ Fou\"]");
    }
}
