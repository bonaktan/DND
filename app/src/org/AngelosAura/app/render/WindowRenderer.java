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
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
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

    @Override
    public void create() {
        Path tmx = resolveAssetsRoot().resolve("map/TestingMap.tmx");
        map = new TmxMapLoader(new AbsoluteFileHandleResolver()).load(tmx.toString());

        int columns = map.getProperties().get("width", Integer.class);
        int rows = map.getProperties().get("height", Integer.class);

        camera = new OrthographicCamera();
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
        Gdx.graphics.setContinuousRendering(false);
        Gdx.graphics.requestRendering();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        mapRenderer.setView(camera);
        mapRenderer.render();
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
