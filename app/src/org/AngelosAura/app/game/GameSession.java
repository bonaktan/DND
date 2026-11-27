package org.AngelosAura.app.game;

import org.AngelosAura.app.game.mob.Player;

public final class GameSession {
    private static final float PLAYER_SPEED = 5f;

    private final int columns;
    private final int rows;
    private final Mob player;

    public GameSession(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        player = new Player(columns / 2, rows / 2);
    }

    public Mob player() {
        return player;
    }

    public void movePlayer(int dx, int dy, float seconds) {
        float step = PLAYER_SPEED * seconds;
        if (dx != 0 && dy != 0) {
            step /= (float) Math.sqrt(2);
        }

        player.moveTo(
                Math.clamp(player.x() + dx * step, 0f, columns - 1f),
                Math.clamp(player.y() + dy * step, 0f, rows - 1f));
    }
}
