package org.AngelosAura.app.presentation.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

public final class WorldCamera {
    private static final int VIEW_COLUMNS = 45;
    private static final int VIEW_ROWS = 30;
    private static final float EDGE_PADDING_FRACTION = 0.05f;
    private static final float TILE_CENTER_OFFSET = 0.5f;
    private static final float FOLLOW_DURATION_SECONDS = 0.5f;
    private static final Interpolation FOLLOW_EASING = new Interpolation.PowOut(2);

    private final OrthographicCamera camera;
    private final FitViewport viewport;
    private final int mapColumns;
    private final int mapRows;
    private final Vector2 followPosition;
    private final Vector2 followTarget;
    private final Vector2 followStart = new Vector2();
    private float followElapsedSeconds = FOLLOW_DURATION_SECONDS;

    public WorldCamera(float targetX, float targetY, int mapColumns, int mapRows) {
        camera = new OrthographicCamera();
        this.mapColumns = mapColumns;
        this.mapRows = mapRows;
        followPosition =
                new Vector2(
                        boundedCenter(targetX, VIEW_COLUMNS, mapColumns),
                        boundedCenter(targetY, VIEW_ROWS, mapRows));
        followTarget = new Vector2(followPosition);
        viewport = new FitViewport(VIEW_COLUMNS, VIEW_ROWS, camera);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    public float x() {
        return camera.position.x;
    }

    public float y() {
        return camera.position.y;
    }

    public int viewColumns() {
        return VIEW_COLUMNS;
    }

    public int viewRows() {
        return VIEW_ROWS;
    }

    public void follow(float targetX, float targetY, float seconds) {
        float centerX = boundedCenter(targetX, VIEW_COLUMNS, mapColumns);
        float centerY = boundedCenter(targetY, VIEW_ROWS, mapRows);
        if (centerX != followTarget.x || centerY != followTarget.y) {
            followStart.set(followPosition);
            followTarget.set(centerX, centerY);
            followElapsedSeconds = 0f;
        }
        if (followElapsedSeconds == FOLLOW_DURATION_SECONDS) {
            return;
        }

        followElapsedSeconds = Math.min(followElapsedSeconds + seconds, FOLLOW_DURATION_SECONDS);
        followPosition
                .set(followStart)
                .interpolate(
                        followTarget,
                        followElapsedSeconds / FOLLOW_DURATION_SECONDS,
                        FOLLOW_EASING);
    }

    OrthographicCamera apply(int tileSize) {
        camera.position.set(snap(followPosition.x, tileSize), snap(followPosition.y, tileSize), 0f);
        viewport.apply();
        return camera;
    }

    static float boundedCenter(float target, int viewSize, int mapSize) {
        float inset = Math.min(mapSize / 2f, viewSize * (0.5f - EDGE_PADDING_FRACTION));
        return Math.clamp(target + TILE_CENTER_OFFSET, inset, mapSize - inset);
    }

    private static float snap(float coordinate, int tileSize) {
        return Math.round(coordinate * tileSize) / (float) tileSize;
    }
}
