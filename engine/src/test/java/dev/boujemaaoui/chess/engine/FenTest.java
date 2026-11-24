package dev.boujemaaoui.chess.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FenTest {

    @Test
    void parsesStartingPosition() {
        Position position = Fen.parse(Fen.STARTING_POSITION);

        assertThat(position.pieceAt(Square.E1)).contains(new Piece(Color.WHITE, PieceType.KING));
        assertThat(position.pieceAt(Square.D8)).contains(new Piece(Color.BLACK, PieceType.QUEEN));
        assertThat(position.pieceAt(Square.A2)).contains(new Piece(Color.WHITE, PieceType.PAWN));
        assertThat(position.pieceAt(Square.E4)).isEmpty();
        assertThat(position.pieces()).hasSize(32);
        assertThat(position.sideToMove()).isEqualTo(Color.WHITE);
        assertThat(position.castlingRights()).isEqualTo(CastlingRights.ALL);
        assertThat(position.enPassantSquare()).isEmpty();
        assertThat(position.halfmoveClock()).isZero();
        assertThat(position.fullmoveNumber()).isEqualTo(1);
    }

    @Test
    void parsesPartialCastlingRightsAndEnPassantSquare() {
        Position position = Fen.parse("rnbqkbnr/ppp1pppp/8/3pP3/8/8/PPPP1PPP/RNBQKBNR w Kq d6 0 3");

        assertThat(position.castlingRights()).isEqualTo(new CastlingRights(true, false, false, true));
        assertThat(position.enPassantSquare()).contains(Square.D6);
        assertThat(position.fullmoveNumber()).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        Fen.STARTING_POSITION,
        "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1",
        "rnbqkbnr/pppp1ppp/8/8/3Pp3/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 2",
        "8/8/8/4k3/8/8/8/4K3 b - - 42 87",
    })
    void formatsParsedPositionIdentically(String fen) {
        assertThat(Fen.format(Fen.parse(fen))).isEqualTo(fen);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP w KQkq - 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR/8 w KQkq - 0 1",
        "rnbqkbnr/pppppppp/9/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        "rnbqkbnr/pppppppp/7/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        "rnbqkbnrp/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        "rnbqkbnr/ppppxppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR x KQkq - 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkx - 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KKkq - 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq e4 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq z3 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq e3 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR b KQkq e6 0 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - -1 1",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 0",
        "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - a 1",
    })
    void rejectsInvalidFen(String fen) {
        assertThatExceptionOfType(InvalidFenException.class).isThrownBy(() -> Fen.parse(fen));
    }

    @Test
    void rejectsNull() {
        assertThatExceptionOfType(InvalidFenException.class).isThrownBy(() -> Fen.parse(null));
    }
}
