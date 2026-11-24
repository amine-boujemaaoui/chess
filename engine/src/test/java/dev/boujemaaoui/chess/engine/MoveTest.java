package dev.boujemaaoui.chess.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MoveTest {

    @Test
    void parsesUciMove() {
        assertThat(Move.fromUci("e2e4")).isEqualTo(new Move(Square.E2, Square.E4));
    }

    @Test
    void parsesUciPromotion() {
        assertThat(Move.fromUci("e7e8n")).isEqualTo(new Move(Square.E7, Square.E8, PieceType.KNIGHT));
    }

    @ParameterizedTest
    @ValueSource(strings = {"e2e4", "a7a8q", "h2h1r"})
    void formatsParsedUciIdentically(String uci) {
        assertThat(Move.fromUci(uci).toUci()).isEqualTo(uci);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"e2", "e2e9", "i2i4", "E2E4", "e7e8k", "e7e8p", "e2e4 "})
    void rejectsInvalidUci(String uci) {
        assertThatExceptionOfType(IllegalMoveException.class).isThrownBy(() -> Move.fromUci(uci));
    }
}
