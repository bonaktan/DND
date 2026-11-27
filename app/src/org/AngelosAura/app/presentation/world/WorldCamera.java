package org.AngelosAura.app.presentation.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;

public final class WorldCamera {
    private static final float TILE_CENTER_OFFSET = 0.5f;

    private final OrthographicCamera camera;
    private final FitViewport viewport;

    public WorldCamera(int columns, int rows, float targetX, float targetY) {
        camera = new OrthographicCamera();
        camera.position.set(targetX + TILE_CENTER_OFFSET, targetY + TILE_CENTER_OFFSET, 0f);
        viewport = new FitViewport(columns, rows, camera);
    }

    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    public void follow(float targetX, float targetY, float seconds) {
        float offsetX = targetX + TILE_CENTER_OFFSET - camera.position.x;
        float offsetY = targetY + TILE_CENTER_OFFSET - camera.position.y;
        float distance = Vector2.len(offsetX, offsetY);
        float fraction = distance < 0.01f ? 1f : 1f - (float) Math.exp(-4f * seconds);
        camera.position.add(offsetX * fraction, offsetY * fraction, 0f);
    }

    OrthographicCamera apply() {
        viewport.apply();
        return camera;
    }
}
