package org.AngelosAura.app.render;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.assets.loaders.resolvers.AbsoluteFileHandleResolver;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.lwjgl.glfw.GLFW;

public final class WindowRenderer extends ApplicationAdapter {
    private TiledMap map;
    private int windowedWidth;
    private int windowedHeight;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Texture playerTexture;
    private float playerX;
    private float playerY;
    private int columns;
    private int rows;

    @Override
    public void create() {
        Path tmx = resolveAssetsRoot().resolve("map/TestingMap.tmx");
        map = new TmxMapLoader(new AbsoluteFileHandleResolver()).load(tmx.toString());

        columns = map.getProperties().get("width", Integer.class);
        rows = map.getProperties().get("height", Integer.class);
        playerX = columns / 2;
        playerY = rows / 2;
        Path cat = resolveAssetsRoot().resolve("textures/cat.png");
        playerTexture = new Texture(Gdx.files.absolute(cat.toString()));
        camera = new OrthographicCamera();
        camera.position.set(playerX + 0.5f, playerY + 0.5f, 0f);
        viewport = new FitViewport(columns, rows, camera);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f / 16f);
        windowedWidth = Math.clamp(Gdx.graphics.getWidth() * 4 / 5, 1, 960);
        windowedHeight = Math.clamp(Gdx.graphics.getHeight() * 4 / 5, 1, 720);
        Gdx.input.setInputProcessor(
                new InputAdapter() {
                    @Override
                    public boolean keyDown(int keycode) {
                        if (keycode != Input.Keys.F11) {
                            return false;
                        }
                        Gdx.app.postRunnable(WindowRenderer.this::toggleFullscreen);
                        return true;
                    }
                });
        Gdx.graphics.requestRendering();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        int dx = 0;
        int dy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            dx++;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            dx--;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) {
            dy++;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            dy--;
        }
        float dt = Gdx.graphics.getDeltaTime();
        float step = 5f * dt; // 5 tiles per second
        if (dx != 0 && dy != 0) {
            step /= (float) Math.sqrt(2);
        }

        playerX = Math.clamp(playerX + dx * step, 0f, columns - 1f);
        playerY = Math.clamp(playerY + dy * step, 0f, rows - 1f);

        updateCamera(dt);
        viewport.apply();
        mapRenderer.setView(camera);
        mapRenderer.render();

        var batch = mapRenderer.getBatch();
        batch.begin();
        batch.draw(playerTexture, playerX, playerY, 1f, 1f);
        batch.end();
    }

    private void updateCamera(float dt) {
        float offsetX = playerX + 0.5f - camera.position.x;
        float offsetY = playerY + 0.5f - camera.position.y;
        float distance = Vector2.len(offsetX, offsetY);
        float fraction = distance < 0.01f ? 1f : 1f - (float) Math.exp(-4f * dt);
        camera.position.add(offsetX * fraction, offsetY * fraction, 0f);
    }

    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            if (usesWayland()) {
                setWaylandWindowedMode();
            } else {
                Gdx.graphics.setWindowedMode(windowedWidth, windowedHeight);
            }
        } else {
            windowedWidth = Gdx.graphics.getWidth();
            windowedHeight = Gdx.graphics.getHeight();
            // Use the same primary display as startup without looking up the window's
            // position.
            var displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
            if (usesWayland()) {
                setWaylandFullscreenMode(displayMode);
            } else {
                Gdx.graphics.setFullscreenMode(displayMode);
            }
        }
    }

    private boolean usesWayland() {
        return GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND;
    }

    private void setWaylandWindowedMode() {
        // Wayland leaves placement of the resized window to the compositor.
        GLFW.glfwSetWindowMonitor(
                windowHandle(), 0, 0, 0, windowedWidth, windowedHeight, GLFW.GLFW_DONT_CARE);
    }

    private void setWaylandFullscreenMode(DisplayMode displayMode) {
        GLFW.glfwSetWindowMonitor(
                windowHandle(),
                GLFW.glfwGetPrimaryMonitor(),
                0,
                0,
                displayMode.width,
                displayMode.height,
                displayMode.refreshRate);
    }

    private long windowHandle() {
        return ((Lwjgl3Graphics) Gdx.graphics).getWindow().getWindowHandle();
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
        map.dispose();
        playerTexture.dispose();
    }

    private static Path resolveAssetsRoot() {
        String configured = System.getProperty("dnd.assets");
        if (configured != null) {
            return Path.of(configured).toAbsolutePath().normalize();
        }

        try {
            Path appJar =
                    Path.of(
                            WindowRenderer.class
                                    .getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI());
            return appJar.getParent().getParent().resolve("assets");
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Cannot resolve the installed assets folder", e);
        }
    }
}
