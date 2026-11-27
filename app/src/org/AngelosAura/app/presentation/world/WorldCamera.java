package org.AngelosAura.app.presentation.world;

import static org.AngelosAura.app.GameTuning.Setting.CAMERA_EASING_POWER;
import static org.AngelosAura.app.GameTuning.Setting.CAMERA_FOLLOW_MILLIS;
import static org.AngelosAura.app.GameTuning.Setting.CAMERA_PADDING_PERCENT;
import static org.AngelosAura.app.GameTuning.Setting.CAMERA_VIEW_COLUMNS;
import static org.AngelosAura.app.GameTuning.Setting.CAMERA_VIEW_ROWS;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.math.Vector2;
import org.AngelosAura.app.GameTuning;

public final class WorldCamera {
    private static final float TILE_CENTER_OFFSET = 0.5f;
    private static final float MILLIS_PER_SECOND = 1000f;
    private static final float PERCENT_PER_WHOLE = 100f;

    private final OrthographicCamera camera;
    private final int mapColumns;
    private final int mapRows;
    private final GameTuning tuning;
    private final Vector2 followPosition;
    private final Vector2 followTarget;
    private double screenAspectRatio;
    private float followProgress = 1f;

    public WorldCamera(float targetX, float targetY, int mapColumns, int mapRows) {
        this(targetX, targetY, mapColumns, mapRows, new GameTuning());
    }

    public WorldCamera(
            float targetX, float targetY, int mapColumns, int mapRows, GameTuning tuning) {
        camera = new OrthographicCamera();
        this.mapColumns = mapColumns;
        this.mapRows = mapRows;
        this.tuning = tuning;
        updateViewSize();
        int padding = tuning.get(CAMERA_PADDING_PERCENT);
        followPosition =
                new Vector2(
                        boundedCenter(targetX, viewColumns(), mapColumns, padding),
                        boundedCenter(targetY, viewRows(), mapRows, padding));
        followTarget = new Vector2(followPosition);
    }

    public void resize(int width, int height) {
        if (width > 0 && height > 0) {
            screenAspectRatio = (double) width / height;
            updateViewSize();
        }
    }

    public float x() {
        return camera.position.x;
    }

    public float y() {
        return camera.position.y;
    }

    public float viewColumns() {
        return camera.viewportWidth;
    }

    public float viewRows() {
        return camera.viewportHeight;
    }

    public void follow(float targetX, float targetY, float seconds) {
        updateViewSize();
        int padding = tuning.get(CAMERA_PADDING_PERCENT);
        float centerX = boundedCenter(targetX, viewColumns(), mapColumns, padding);
        float centerY = boundedCenter(targetY, viewRows(), mapRows, padding);
        if (centerX != followTarget.x || centerY != followTarget.y) {
            followTarget.set(centerX, centerY);
            followProgress = 0f;
        }
        if (followProgress == 1f) {
            return;
        }

        float durationSeconds = tuning.get(CAMERA_FOLLOW_MILLIS) / MILLIS_PER_SECOND;
        float nextProgress = Math.min(followProgress + seconds / durationSeconds, 1f);
        float remainingRatio = (1f - nextProgress) / (1f - followProgress);
        // Apply the ease-out to the remaining distance so live changes never rewind the camera.
        float fraction = 1f - (float) Math.pow(remainingRatio, tuning.get(CAMERA_EASING_POWER));
        followPosition.lerp(followTarget, fraction);
        followProgress = nextProgress;
    }

    OrthographicCamera apply(int tileSize) {
        camera.position.set(snap(followPosition.x, tileSize), snap(followPosition.y, tileSize), 0f);
        HdpiUtils.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();
        return camera;
    }

    private void updateViewSize() {
        int minColumns = tuning.get(CAMERA_VIEW_COLUMNS);
        int minRows = tuning.get(CAMERA_VIEW_ROWS);
        double aspectRatio =
                screenAspectRatio > 0 ? screenAspectRatio : (double) minColumns / minRows;
        camera.viewportWidth = Math.max(minColumns, (float) (minRows * aspectRatio));
        camera.viewportHeight = Math.max(minRows, (float) (minColumns / aspectRatio));
    }

    static float boundedCenter(float target, float viewSize, int mapSize, int paddingPercent) {
        float inset =
                Math.min(mapSize / 2f, viewSize * (0.5f - paddingPercent / PERCENT_PER_WHOLE));
        return Math.clamp(target + TILE_CENTER_OFFSET, inset, mapSize - inset);
    }

    private static float snap(float coordinate, int tileSize) {
        return Math.round(coordinate * tileSize) / (float) tileSize;
    }
}
