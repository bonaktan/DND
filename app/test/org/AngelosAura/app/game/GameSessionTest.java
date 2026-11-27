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

        assertEquals(17.5f, cardinalSession.player().x());
        assertEquals(10f, cardinalSession.player().y());
        assertEquals(
                2.5,
                Math.hypot(diagonalSession.player().x() - 15f, diagonalSession.player().y() - 10f),
                0.00001);
    }

    @Test
    void clampsMovementToTheMapEdges() {
        var session = new GameSession(4, 3);

        session.movePlayer(-1, 1, 1f);
        assertEquals(0f, session.player().x());
        assertEquals(2f, session.player().y());

        session.movePlayer(1, -1, 1f);
        assertEquals(3f, session.player().x());
        assertEquals(0f, session.player().y());
    }
}
