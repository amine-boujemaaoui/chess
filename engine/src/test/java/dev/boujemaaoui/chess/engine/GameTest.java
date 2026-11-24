package dev.boujemaaoui.chess.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GameTest {

    @Test
    void newGameStartsFromStartingPosition() {
        Game game = new Game();

        assertThat(Fen.format(game.position())).isEqualTo(Fen.STARTING_POSITION);
        assertThat(game.moves()).isEmpty();
        assertThat(game.status()).isEqualTo(GameStatus.ONGOING);
        assertThat(game.result()).isEqualTo(GameResult.ONGOING);
    }

    @Test
    void recordsPlayedMoves() {
        Game game = play(new Game(), "e2e4", "e7e5");

        assertThat(game.moves()).extracting(Move::toUci).containsExactly("e2e4", "e7e5");
        assertThat(game.positions()).hasSize(3);
        assertThat(Fen.format(game.position()))
            .isEqualTo("rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq e6 0 2");
    }

    @Test
    void rejectsIllegalMoveAndKeepsGameUnchanged() {
        Game game = new Game();

        assertThatExceptionOfType(IllegalMoveException.class).isThrownBy(() -> game.play(Move.fromUci("e2e5")));
        assertThat(game.moves()).isEmpty();
    }

    @Test
    void detectsCheckmate() {
        Game game = play(new Game(), "f2f3", "e7e5", "g2g4", "d8h4");

        assertThat(game.status()).isEqualTo(GameStatus.CHECKMATE);
        assertThat(game.result()).isEqualTo(GameResult.BLACK_WINS);
    }

    @Test
    void rejectsMoveAfterGameOver() {
        Game game = play(new Game(), "f2f3", "e7e5", "g2g4", "d8h4");

        assertThatExceptionOfType(IllegalMoveException.class).isThrownBy(() -> game.play(Move.fromUci("e1f2")));
    }

    @Test
    void detectsStalemate() {
        Game game = new Game(Fen.parse("7k/5Q2/6K1/8/8/8/8/8 b - - 0 1"));

        assertThat(game.status()).isEqualTo(GameStatus.STALEMATE);
        assertThat(game.result()).isEqualTo(GameResult.DRAW);
    }

    @ParameterizedTest(name = "{0} : {1}")
    @CsvSource(delimiter = ';', textBlock = """
        4k3/8/8/8/8/8/8/4K3 w - - 0 1;     true
        4k3/8/8/8/8/8/8/4KN2 w - - 0 1;    true
        4k3/8/8/8/8/8/8/4KB2 w - - 0 1;    true
        4kb2/8/8/8/8/8/8/2B1K3 w - - 0 1;  true
        4k3/8/8/8/8/8/8/2B1Kb2 w - - 0 1;  false
        4k3/8/8/8/8/8/8/3NKN2 w - - 0 1;   false
        4k3/8/8/8/8/8/8/4KR2 w - - 0 1;    false
        4k3/8/8/8/8/8/4P3/4K3 w - - 0 1;   false
        """)
    void detectsInsufficientMaterial(String fen, boolean insufficient) {
        Game game = new Game(Fen.parse(fen));

        assertThat(game.status() == GameStatus.INSUFFICIENT_MATERIAL).isEqualTo(insufficient);
    }

    @Test
    void appliesFiftyMoveRule() {
        Game game = play(new Game(Fen.parse("4k3/8/8/8/8/8/8/R3K3 w - - 99 60")), "a1a2");

        assertThat(game.status()).isEqualTo(GameStatus.FIFTY_MOVE_RULE);
        assertThat(game.result()).isEqualTo(GameResult.DRAW);
    }

    @Test
    void checkmateOnHundredthHalfMoveWinsOverFiftyMoveRule() {
        Game game = play(new Game(Fen.parse("6k1/5ppp/8/8/8/8/8/R5K1 w - - 99 60")), "a1a8");

        assertThat(game.status()).isEqualTo(GameStatus.CHECKMATE);
        assertThat(game.result()).isEqualTo(GameResult.WHITE_WINS);
    }

    @Test
    void detectsThreefoldRepetition() {
        Game game = play(new Game(), "g1f3", "g8f6", "f3g1", "f6g8", "g1f3", "g8f6", "f3g1");
        assertThat(game.status()).isEqualTo(GameStatus.ONGOING);

        play(game, "f6g8");

        assertThat(game.status()).isEqualTo(GameStatus.THREEFOLD_REPETITION);
        assertThat(game.result()).isEqualTo(GameResult.DRAW);
    }

    @Test
    void enPassantSquareWithoutPossibleCaptureDoesNotPreventRepetition() {
        Game game = play(new Game(), "e2e4", "g8f6", "g1f3", "f6g8", "f3g1", "g8f6", "g1f3", "f6g8");
        assertThat(game.status()).isEqualTo(GameStatus.ONGOING);

        play(game, "f3g1");

        assertThat(game.status()).isEqualTo(GameStatus.THREEFOLD_REPETITION);
    }

    private static Game play(Game game, String... uciMoves) {
        for (String uci : uciMoves) {
            game.play(Move.fromUci(uci));
        }
        return game;
    }
}
