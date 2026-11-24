package dev.boujemaaoui.chess.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Compte les positions atteignables à une profondeur donnée et les compare aux valeurs de référence
 * (https://www.chessprogramming.org/Perft_Results). Une seule erreur du générateur fausse le total.
 *
 * <p>Positions : initiale, « Kiwipete » (roques, prises en passant, promotions, clouages) et
 * « position 3 » (prises en passant qui découvrent le roi).
 */
class PerftTest {

    @ParameterizedTest(name = "{0} à la profondeur {1}")
    @CsvSource(delimiter = ';', textBlock = """
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1;          1; 20
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1;          2; 400
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1;          3; 8902
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1;          4; 197281
        r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1; 1; 48
        r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1; 2; 2039
        r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1; 3; 97862
        8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1;                         1; 14
        8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1;                         2; 191
        8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1;                         3; 2812
        8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1;                         4; 43238
        """)
    void matchesReferenceCount(String fen, int depth, long expected) {
        assertThat(perft(Fen.parse(fen), depth)).isEqualTo(expected);
    }

    @Tag("slow")
    @ParameterizedTest(name = "{0} à la profondeur {1}")
    @CsvSource(delimiter = ';', textBlock = """
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1;          5; 4865609
        r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1; 4; 4085603
        8/2p5/3p4/KP5r/1R3p1k/8/4P1P1/8 w - - 0 1;                         5; 674624
        """)
    void matchesReferenceCountAtHighDepth(String fen, int depth, long expected) {
        assertThat(perft(Fen.parse(fen), depth)).isEqualTo(expected);
    }

    private static long perft(Position position, int depth) {
        var moves = MoveGenerator.legalMoves(position);
        if (depth == 1) {
            return moves.size();
        }
        long count = 0;
        for (Move move : moves) {
            count += perft(position.play(move), depth - 1);
        }
        return count;
    }
}
