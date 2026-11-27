package org.AngelosAura.app.game;

import org.AngelosAura.app.game.mob.Player;

public final class GameSession {
    private static final float PLAYER_SPEED = 5f;
    private static final float PLAYER_ACCELERATION = 25f;
    private static final float PLAYER_DECELERATION = 30f;

    private final int columns;
    private final int rows;
    private final Mob player;
    private float velocityX;
    private float velocityY;

    public GameSession(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;
        player = new Player(columns / 2, rows / 2);
    }

    public Mob player() {
        return player;
    }

    public void movePlayer(int dx, int dy, float seconds) {
        float inputLength = (float) Math.hypot(dx, dy);
        float speedPerInput = PLAYER_SPEED / Math.max(1f, inputLength);
        float targetX = dx * speedPerInput;
        float targetY = dy * speedPerInput;
        float rate = inputLength > 0f ? PLAYER_ACCELERATION : PLAYER_DECELERATION;
        float timeToTarget = (float) Math.hypot(targetX - velocityX, targetY - velocityY) / rate;
        float rampTime = Math.min(seconds, timeToTarget);
        float nextVelocityX = targetX;
        float nextVelocityY = targetY;
        if (rampTime < timeToTarget) {
            float progress = rampTime / timeToTarget;
            nextVelocityX = velocityX + (targetX - velocityX) * progress;
            nextVelocityY = velocityY + (targetY - velocityY) * progress;
        }

        float cruiseTime = seconds - rampTime;
        float x =
                player.x()
                        + (velocityX + nextVelocityX) * 0.5f * rampTime
                        + nextVelocityX * cruiseTime;
        float y =
                player.y()
                        + (velocityY + nextVelocityY) * 0.5f * rampTime
                        + nextVelocityY * cruiseTime;
        float clampedX = Math.clamp(x, 0f, columns - 1f);
        float clampedY = Math.clamp(y, 0f, rows - 1f);
        player.moveTo(clampedX, clampedY);
        velocityX = stopAtEdge(nextVelocityX, clampedX, columns - 1f);
        velocityY = stopAtEdge(nextVelocityY, clampedY, rows - 1f);
    }

    private static float stopAtEdge(float velocity, float position, float maxPosition) {
        if ((position == 0f && velocity < 0f) || (position == maxPosition && velocity > 0f)) {
            return 0f;
        }
        return velocity;
    }
}
