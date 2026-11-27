package org.AngelosAura.app.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.AngelosAura.app.game.mob.Player;
import org.junit.jupiter.api.Test;

class GameSessionTest {
    @Test
    void spawnsPlayerMobAtMapCenter() {
        var session = new GameSession(31, 21);

        assertInstanceOf(Player.class, session.player());
        assertEquals(15f, session.player().x());
        assertEquals(10f, session.player().y());
    }

    @Test
    void diagonalAndCardinalMovementCoverTheSameDistance() {
        var cardinalSession = new GameSession(30, 20);
        var diagonalSession = new GameSession(30, 20);

        cardinalSession.movePlayer(1, 0, 0.5f);
        diagonalSession.movePlayer(1, 1, 0.5f);

        assertEquals(17f, cardinalSession.player().x());
        assertEquals(10f, cardinalSession.player().y());
        assertEquals(
                cardinalSession.player().x() - 15f,
                Math.hypot(diagonalSession.player().x() - 15f, diagonalSession.player().y() - 10f),
                0.00001);
    }

    @Test
    void acceleratesToTopSpeedAndCoastsToAStop() {
        var session = new GameSession(30, 20);

        session.movePlayer(1, 0, 0.1f);
        assertEquals(15.125f, session.player().x(), 0.00001f);
        session.movePlayer(1, 0, 0.1f);
        assertEquals(15.5f, session.player().x(), 0.00001f);
        session.movePlayer(1, 0, 0.1f);
        assertEquals(16f, session.player().x(), 0.00001f);

        session.movePlayer(0, 0, 0.2f);
        float stoppedX = session.player().x();
        assertEquals(16f + 5f / 12f, stoppedX, 0.00001f);
        session.movePlayer(0, 0, 1f);
        assertEquals(stoppedX, session.player().x());
    }

    @Test
    void retainsMomentumWhenReversingDirection() {
        var session = new GameSession(30, 20);

        session.movePlayer(1, 0, 0.2f);
        session.movePlayer(-1, 0, 0.2f);
        assertEquals(16f, session.player().x(), 0.00001f);
        session.movePlayer(-1, 0, 0.2f);
        assertEquals(15.5f, session.player().x(), 0.00001f);
    }

    @Test
    void clampsMovementToTheMapEdges() {
        var session = new GameSession(4, 3);

        session.movePlayer(-1, 1, 1f);
        assertEquals(0f, session.player().x());
        assertEquals(2f, session.player().y());

        session.movePlayer(1, -1, 0.1f);
        float diagonalStep = 0.125f / (float) Math.sqrt(2);
        assertEquals(diagonalStep, session.player().x(), 0.00001f);
        assertEquals(2f - diagonalStep, session.player().y(), 0.00001f);

        session.movePlayer(1, -1, 1f);
        assertEquals(3f, session.player().x());
        assertEquals(0f, session.player().y());
    }
}
