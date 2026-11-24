package dev.boujemaaoui.chess.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PositionTest {

    @ParameterizedTest(name = "{1}")
    @CsvSource(delimiter = ';', textBlock = """
        # coup simple : le compteur de demi-coups augmente
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; g1f3; rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNBQKB1R b KQkq - 1 1
        # avance double : case de prise en passant
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; e2e4; rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1
        # coup des noirs : le numéro de coup augmente, la case de prise en passant disparaît
        rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1; g8f6; rnbqkb1r/pppppppp/5n2/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 1 2
        # prise : le compteur de demi-coups revient à zéro
        4k3/8/8/3p4/8/8/8/3RK3 w - - 5 10; d1d5; 4k3/8/8/3R4/8/8/8/4K3 b - - 0 10
        # prise en passant : le pion pris disparaît
        4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1; e5d6; 4k3/8/3P4/8/8/8/8/4K3 b - - 0 1
        # petit roque des blancs
        r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1; e1g1; r3k2r/8/8/8/8/8/8/R4RK1 b kq - 1 1
        # grand roque des noirs
        r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1; e8c8; 2kr3r/8/8/8/8/8/8/R3K2R w KQ - 1 2
        # déplacer une tour retire le roque de son côté
        r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1; a1a2; r3k2r/8/8/8/8/8/R7/4K2R b Kkq - 1 1
        # prendre une tour retire le roque adverse de ce côté
        r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1; h1h8; r3k2R/8/8/8/8/8/8/R3K3 b Qq - 0 1
        # promotion
        4k3/P7/8/8/8/8/8/4K3 w - - 0 1; a7a8q; Q3k3/8/8/8/8/8/8/4K3 b - - 0 1
        """)
    void playsMove(String fen, String uci, String expectedFen) {
        assertThat(Fen.format(Fen.parse(fen).play(move(uci)))).isEqualTo(expectedFen);
    }

    @Test
    void leavesOriginalPositionUnchanged() {
        Position position = Fen.parse(Fen.STARTING_POSITION);

        position.play(move("e2e4"));

        assertThat(Fen.format(position)).isEqualTo(Fen.STARTING_POSITION);
    }

    @Test
    void rejectsMoveFromEmptySquare() {
        Position position = Fen.parse(Fen.STARTING_POSITION);

        assertThatIllegalArgumentException().isThrownBy(() -> position.play(move("e4e5")));
    }

    private static Move move(String uci) {
        Square from = Square.fromName(uci.substring(0, 2));
        Square to = Square.fromName(uci.substring(2, 4));
        return uci.length() == 5 ? new Move(from, to, PieceType.fromSymbol(uci.charAt(4))) : new Move(from, to);
    }
}
