package org.AngelosAura.app.presentation.map;

import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.ImageResolver;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.XmlReader.Element;
import java.nio.file.Path;

public final class MapRenderer implements Disposable {
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer renderer;
    private final int columns;
    private final int rows;
    private final int tileSize;

    public MapRenderer(Path mapPath) {
        map = new RelativeTilesetMapLoader().load(mapPath.toString());
        try {
            columns = map.getProperties().get("width", Integer.class);
            rows = map.getProperties().get("height", Integer.class);
            tileSize = map.getProperties().get("tilewidth", Integer.class);
            renderer = new OrthogonalTiledMapRenderer(map, 1f / tileSize);
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

    public int tileSize() {
        return tileSize;
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

    private static final class RelativeTilesetMapLoader extends TmxMapLoader {
        private RelativeTilesetMapLoader() {
            super(new AbsoluteFileHandleResolver());
        }

        @Override
        protected void addStaticTiles(
                FileHandle mapFile,
                ImageResolver imageResolver,
                TiledMapTileSet tileSet,
                Element tileset,
                Array<Element> tileElements,
                String name,
                int firstGid,
                int tileWidth,
                int tileHeight,
                int spacing,
                int margin,
                int offsetX,
                int offsetY,
                String imageSource,
                int imageWidth,
                int imageHeight,
                FileHandle image) {
            // libGDX 1.14.2 needs the TSX as the base for per-tile images in external tilesets.
            String tilesetSource = tileset.getAttribute("source", null);
            FileHandle imageBase =
                    image == null && tilesetSource != null
                            ? getRelativeFileHandle(mapFile, tilesetSource)
                            : mapFile;
            super.addStaticTiles(
                    imageBase,
                    imageResolver,
                    tileSet,
                    tileset,
                    tileElements,
                    name,
                    firstGid,
                    tileWidth,
                    tileHeight,
                    spacing,
                    margin,
                    offsetX,
                    offsetY,
                    imageSource,
                    imageWidth,
                    imageHeight,
                    image);
        }
    }
}
