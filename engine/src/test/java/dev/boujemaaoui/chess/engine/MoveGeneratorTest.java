package dev.boujemaaoui.chess.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MoveGeneratorTest {

    @Test
    void startingPositionHasTwentyMoves() {
        assertThat(movesOf(Fen.STARTING_POSITION)).hasSize(20);
    }

    @Test
    void onlyPiecesOfTheSideToMoveAreMoved() {
        assertThat(movesOf("4k3/8/8/8/8/8/8/4K2n w - - 0 1"))
            .containsExactlyInAnyOrder("e1d1", "e1f1", "e1d2", "e1e2", "e1f2");
    }

    @Test
    void knightJumpsInAllEightDirections() {
        assertThat(movesFrom("8/8/8/8/3N4/8/8/8 w - - 0 1", Square.D4))
            .containsExactlyInAnyOrder("d4c6", "d4e6", "d4f5", "d4f3", "d4e2", "d4c2", "d4b3", "d4b5");
    }

    @Test
    void knightInTheCornerStaysOnTheBoard() {
        assertThat(movesFrom("8/8/8/8/8/8/8/N7 w - - 0 1", Square.A1))
            .containsExactlyInAnyOrder("a1b3", "a1c2");
    }

    @Test
    void knightCapturesEnemyButNotOwnPieces() {
        assertThat(movesFrom("8/8/8/8/8/1p6/2P5/N7 w - - 0 1", Square.A1))
            .containsExactly("a1b3");
    }

    @Test
    void bishopSlidesUntilBlocked() {
        assertThat(movesFrom("8/8/5p2/8/3B4/8/1P6/8 w - - 0 1", Square.D4))
            .containsExactlyInAnyOrder("d4e5", "d4f6", "d4c5", "d4b6", "d4a7", "d4e3", "d4f2", "d4g1", "d4c3");
    }

    @Test
    void rookSlidesUntilBlocked() {
        assertThat(movesFrom("8/8/8/8/8/p7/8/R3K3 w - - 0 1", Square.A1))
            .containsExactlyInAnyOrder("a1a2", "a1a3", "a1b1", "a1c1", "a1d1");
    }

    @Test
    void queenCombinesRookAndBishopMoves() {
        assertThat(movesFrom("8/8/8/8/3Q4/8/8/8 w - - 0 1", Square.D4)).hasSize(27);
    }

    @Test
    void kingMovesOneSquareInEveryDirection() {
        assertThat(movesFrom("8/8/8/8/8/8/3P4/4K3 w - - 0 1", Square.E1))
            .containsExactlyInAnyOrder("e1d1", "e1f1", "e1e2", "e1f2");
    }

    @Test
    void pawnOnStartingRankAdvancesOneOrTwoSquares() {
        assertThat(movesFrom(Fen.STARTING_POSITION, Square.E2)).containsExactlyInAnyOrder("e2e3", "e2e4");
    }

    @Test
    void pawnAwayFromStartingRankAdvancesOneSquare() {
        assertThat(movesFrom("8/8/8/8/8/4P3/8/8 w - - 0 1", Square.E3)).containsExactly("e3e4");
    }

    @Test
    void blockedPawnCannotAdvance() {
        assertThat(movesFrom("8/8/8/8/8/4p3/4P3/8 w - - 0 1", Square.E2)).isEmpty();
    }

    @Test
    void pawnCannotJumpOverPieceOnDoubleAdvance() {
        assertThat(movesFrom("8/8/8/8/4p3/8/4P3/8 w - - 0 1", Square.E2)).containsExactly("e2e3");
    }

    @Test
    void pawnCapturesDiagonallyOnlyEnemyPieces() {
        assertThat(movesFrom("8/8/8/3p1N2/4P3/8/8/8 w - - 0 1", Square.E4))
            .containsExactlyInAnyOrder("e4e5", "e4d5");
    }

    @Test
    void blackPawnMovesDownTheBoard() {
        assertThat(movesFrom("8/3p4/4P3/8/8/8/8/8 b - - 0 1", Square.D7))
            .containsExactlyInAnyOrder("d7d6", "d7d5", "d7e6");
    }

    @Test
    void pawnReachingLastRankPromotesToFourPieces() {
        assertThat(movesFrom("3r4/4P3/8/8/8/8/8/8 w - - 0 1", Square.E7))
            .containsExactlyInAnyOrder(
                "e7e8q", "e7e8r", "e7e8b", "e7e8n",
                "e7d8q", "e7d8r", "e7d8b", "e7d8n");
    }

    @Test
    void blackPawnPromotesOnFirstRank() {
        assertThat(movesFrom("8/8/8/8/8/8/p7/8 b - - 0 1", Square.A2))
            .containsExactlyInAnyOrder("a2a1q", "a2a1r", "a2a1b", "a2a1n");
    }

    @Test
    void pawnCapturesEnPassant() {
        assertThat(movesFrom("4k3/8/8/3pP3/8/8/8/4K3 w - d6 0 1", Square.E5))
            .containsExactlyInAnyOrder("e5e6", "e5d6");
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', textBlock = """
        rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1; false
        4k3/8/8/8/8/8/8/4K2r w - - 0 1;                           true
        4k3/8/8/8/8/8/8/4K1Br w - - 0 1;                          false
        4k3/3P4/8/8/8/8/8/4K3 b - - 0 1;                          true
        4k3/4P3/8/8/8/8/8/4K3 b - - 0 1;                          false
        4k3/8/3N4/8/8/8/8/4K3 b - - 0 1;                          true
        4k3/8/8/b7/8/8/8/4K3 w - - 0 1;                           true
        """)
    void detectsCheck(String fen, boolean inCheck) {
        assertThat(MoveGenerator.isInCheck(Fen.parse(fen))).isEqualTo(inCheck);
    }

    @Test
    void startingPositionHasTwentyLegalMoves() {
        assertThat(legalMovesOf(Fen.STARTING_POSITION)).hasSize(20);
    }

    @Test
    void pinnedPieceCannotLeaveThePin() {
        assertThat(legalMovesOf("4k3/4r3/8/8/8/8/4N3/4K3 w - - 0 1"))
            .noneMatch(move -> move.startsWith("e2"));
    }

    @Test
    void kingCannotMoveIntoCheck() {
        assertThat(legalMovesOf("3rk3/8/8/8/8/8/8/4K3 w - - 0 1"))
            .containsExactlyInAnyOrder("e1e2", "e1f1", "e1f2");
    }

    @Test
    void checkMustBeAnswered() {
        assertThat(legalMovesOf("4k3/8/8/8/8/8/8/R3K2r w - - 0 1"))
            .containsExactlyInAnyOrder("e1d2", "e1e2", "e1f2");
    }

    @Test
    void enPassantExposingTheKingIsIllegal() {
        assertThat(legalMovesOf("8/8/8/KPp4r/8/8/8/4k3 w - c6 0 1"))
            .contains("b5b6")
            .doesNotContain("b5c6");
    }

    @ParameterizedTest(name = "{2}")
    @CsvSource(delimiter = ';', textBlock = """
        r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1;   e1g1 e1c1; deux roques possibles
        r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1;   e8g8 e8c8; roques des noirs
        r3k2r/8/8/8/8/8/8/R3K2R w kq - 0 1;     ;          sans droit de roque
        r3k2r/8/8/8/8/8/8/RN2K1NR w KQkq - 0 1; ;          cases bloquées
        r3k2r/8/8/8/4r3/8/8/R3K2R w KQkq - 0 1; ;          roi en échec
        r3k2r/8/8/8/5r2/8/8/R3K2R w KQkq - 0 1; e1c1;      case traversée attaquée
        r3k2r/8/8/8/6r1/8/8/R3K2R w KQkq - 0 1; e1c1;      destination attaquée
        r3k2r/8/8/8/1r6/8/8/R3K2R w KQkq - 0 1; e1g1 e1c1; b1 attaquée sans gêner le grand roque
        """)
    void castlesOnlyWhenAllowed(String fen, String expectedCastlings, String description) {
        Position position = Fen.parse(fen);
        Square kingSquare = position.sideToMove() == Color.WHITE ? Square.E1 : Square.E8;
        List<String> castlings = MoveGenerator.legalMoves(position).stream()
            .filter(move -> move.from() == kingSquare && Math.abs(move.to().file() - kingSquare.file()) == 2)
            .map(Move::toString)
            .toList();

        assertThat(castlings).containsExactlyInAnyOrder(
            expectedCastlings == null ? new String[0] : expectedCastlings.split(" "));
    }

    private static List<String> movesOf(String fen) {
        return MoveGenerator.pseudoLegalMoves(Fen.parse(fen)).stream().map(Move::toString).toList();
    }

    private static List<String> legalMovesOf(String fen) {
        return MoveGenerator.legalMoves(Fen.parse(fen)).stream().map(Move::toString).toList();
    }

    private static List<String> movesFrom(String fen, Square from) {
        return MoveGenerator.pseudoLegalMoves(Fen.parse(fen)).stream()
            .filter(move -> move.from() == from)
            .map(Move::toString)
            .toList();
    }
}
