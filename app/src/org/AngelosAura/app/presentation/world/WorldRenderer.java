package org.AngelosAura.app.presentation.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import java.nio.file.Path;
import org.AngelosAura.app.presentation.map.MapRenderer;

public final class WorldRenderer implements Disposable {
    private final MapRenderer mapRenderer;
    private final Texture playerTexture;

    public WorldRenderer(Path mapPath, Path playerTexturePath) {
        mapRenderer = new MapRenderer(mapPath);
        try {
            playerTexture = new Texture(Gdx.files.absolute(playerTexturePath.toString()));
        } catch (RuntimeException exception) {
            mapRenderer.dispose();
            throw exception;
        }
    }

    public int columns() {
        return mapRenderer.columns();
    }

    public int rows() {
        return mapRenderer.rows();
    }

    public void render(WorldCamera camera, float playerX, float playerY) {
        var view = camera.apply();
        ScreenUtils.clear(Color.BLACK);
        mapRenderer.render(view);

        var batch = mapRenderer.batch();
        batch.begin();
        batch.draw(playerTexture, playerX, playerY, 1f, 1f);
        batch.end();
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
        playerTexture.dispose();
    }
}
