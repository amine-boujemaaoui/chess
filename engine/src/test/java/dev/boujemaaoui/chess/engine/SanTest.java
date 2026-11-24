package dev.boujemaaoui.chess.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SanTest {

    @ParameterizedTest(name = "{2}")
    @CsvSource(delimiter = ';', textBlock = """
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1;             e2e4;  e4
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1;             g1f3;  Nf3
        rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 2;        e4d5;  exd5
        4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1;                                    e5d6;  exd6
        4k3/8/8/4p3/8/5N2/8/4K3 w - - 0 1;                                    f3e5;  Nxe5
        r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1;                                 e1g1;  O-O
        r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1;                                 e1c1;  O-O-O
        4k3/P7/8/8/8/8/8/4K3 w - - 0 1;                                       a7a8q; a8=Q+
        4k3/P7/8/8/8/8/8/4K3 w - - 0 1;                                       a7a8n; a8=N
        3rk3/4P3/8/8/8/8/8/4K3 w - - 0 1;                                     e7d8q; exd8=Q+
        4k3/8/8/8/8/8/8/1N2KN2 w - - 0 1;                                     b1d2;  Nbd2
        4k3/8/8/R7/8/8/8/R3K3 w - - 0 1;                                      a1a3;  R1a3
        8/8/1k6/8/4Q2Q/8/8/K6Q w - - 0 1;                                     h4e1;  Qh4e1
        4k3/8/8/8/8/8/8/R3K3 w - - 0 1;                                       a1a8;  Ra8+
        rnbqkbnr/pppp1ppp/8/4p3/6P1/5P2/PPPPP2P/RNBQKBNR b KQkq g3 0 2;       d8h4;  Qh4#
        """)
    void formatsMove(String fen, String uci, String san) {
        assertThat(San.format(Fen.parse(fen), Move.fromUci(uci))).isEqualTo(san);
    }

    @ParameterizedTest(name = "{2}")
    @CsvSource(delimiter = ';', textBlock = """
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1;             e2e4;  e4
        4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1;                                    e5d6;  exd6
        r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1;                                 e1c1;  O-O-O
        4k3/P7/8/8/8/8/8/4K3 w - - 0 1;                                       a7a8n; a8=N
        4k3/8/8/8/8/8/8/1N2KN2 w - - 0 1;                                     f1d2;  Nfd2
        8/8/1k6/8/4Q2Q/8/8/K6Q w - - 0 1;                                     h4e1;  Qh4e1
        4k3/8/8/8/8/8/8/R3K3 w - - 0 1;                                       a1a8;  Ra8+
        4k3/8/8/8/8/8/8/R3K3 w - - 0 1;                                       a1a8;  Ra8
        """)
    void parsesMove(String fen, String uci, String san) {
        assertThat(San.parse(Fen.parse(fen), san)).isEqualTo(Move.fromUci(uci));
    }

    @ParameterizedTest(name = "{1}")
    @CsvSource(delimiter = ';', nullValues = "null", textBlock = """
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; null
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; e5
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; Nd2
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; O-O
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; e2e4
        4k3/8/8/8/8/8/8/1N2KN2 w - - 0 1;                         Nd2
        """)
    void rejectsIllegalOrInvalidSan(String fen, String san) {
        assertThatExceptionOfType(IllegalMoveException.class).isThrownBy(() -> San.parse(Fen.parse(fen), san));
    }
}
