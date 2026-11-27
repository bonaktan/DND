package org.AngelosAura.app.presentation.map;

import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Disposable;
import java.nio.file.Path;

public final class MapRenderer implements Disposable {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final int columns;
    private final int rows;

    public MapRenderer(Path mapPath) {
        map = new TmxMapLoader(new AbsoluteFileHandleResolver()).load(mapPath.toString());
        try {
            columns = map.getProperties().get("width", Integer.class);
            rows = map.getProperties().get("height", Integer.class);
            renderer = new OrthogonalTiledMapRenderer(map, 1f / 16f);
        } catch (RuntimeException exception) {
            map.dispose();
            throw exception;
        }
    }

    public int columns() {
        return columns;
    }

    public int rows() {
        return rows;
    }

    public void render(OrthographicCamera camera) {
        renderer.setView(camera);
        renderer.render();
    }

    public Batch batch() {
        return renderer.getBatch();
    }

    @Override
    public void dispose() {
        renderer.dispose();
        map.dispose();
    }
}
