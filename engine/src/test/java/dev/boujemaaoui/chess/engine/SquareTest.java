package dev.boujemaaoui.chess.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SquareTest {

    @Test
    void exposesFileAndRankFromZero() {
        assertThat(Square.A1.file()).isZero();
        assertThat(Square.A1.rank()).isZero();
        assertThat(Square.E4.file()).isEqualTo(4);
        assertThat(Square.E4.rank()).isEqualTo(3);
        assertThat(Square.H8.file()).isEqualTo(7);
        assertThat(Square.H8.rank()).isEqualTo(7);
    }

    @Test
    void findsSquareFromFileAndRank() {
        assertThat(Square.of(4, 3)).isEqualTo(Square.E4);
    }

    @Test
    void rejectsCoordinatesOutsideTheBoard() {
        assertThatIllegalArgumentException().isThrownBy(() -> Square.of(8, 0));
        assertThatIllegalArgumentException().isThrownBy(() -> Square.of(0, -1));
    }

    @Test
    void parsesAlgebraicName() {
        assertThat(Square.fromName("e4")).isEqualTo(Square.E4);
        assertThat(Square.fromName("h8")).isEqualTo(Square.H8);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "e", "e9", "i1", "E4", "e44"})
    void rejectsInvalidName(String name) {
        assertThatIllegalArgumentException().isThrownBy(() -> Square.fromName(name));
    }

    @Test
    void printsAlgebraicName() {
        assertThat(Square.E4).hasToString("e4");
    }

    @Test
    void offsetsWithinTheBoard() {
        assertThat(Square.E4.offset(1, 2)).contains(Square.F6);
        assertThat(Square.E4.offset(-4, -3)).contains(Square.A1);
    }

    @Test
    void offsetOutsideTheBoardIsEmpty() {
        assertThat(Square.H8.offset(1, 0)).isEmpty();
        assertThat(Square.A1.offset(0, -1)).isEmpty();
    }
}
