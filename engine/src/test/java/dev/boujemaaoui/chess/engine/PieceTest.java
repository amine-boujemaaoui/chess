package dev.boujemaaoui.chess.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class PieceTest {

    @ParameterizedTest
    @ValueSource(chars = {'P', 'N', 'B', 'R', 'Q', 'K', 'p', 'n', 'b', 'r', 'q', 'k'})
    void convertsFenCharacterBackAndForth(char fenChar) {
        assertThat(Piece.fromFenChar(fenChar).toFenChar()).isEqualTo(fenChar);
    }

    @ParameterizedTest
    @ValueSource(chars = {'P', 'K'})
    void upperCaseIsWhite(char fenChar) {
        assertThat(Piece.fromFenChar(fenChar).color()).isEqualTo(Color.WHITE);
    }

    @ParameterizedTest
    @ValueSource(chars = {'p', 'k'})
    void lowerCaseIsBlack(char fenChar) {
        assertThat(Piece.fromFenChar(fenChar).color()).isEqualTo(Color.BLACK);
    }

    @ParameterizedTest
    @ValueSource(chars = {'x', '1', ' ', 'A'})
    void rejectsUnknownCharacter(char fenChar) {
        assertThatIllegalArgumentException().isThrownBy(() -> Piece.fromFenChar(fenChar));
    }
}
