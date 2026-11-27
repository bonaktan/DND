package org.AngelosAura.app.render;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import org.lwjgl.glfw.GLFW;

public final class WindowRenderer extends ApplicationAdapter {
    private static final String MESSAGE = "Hello, world!";
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;
    private int width;
    private int height;
    private int windowedWidth;
    private int windowedHeight;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        layout = new GlyphLayout();
        windowedWidth = Math.clamp(Gdx.graphics.getWidth() * 4 / 5, 1, 960);
        windowedHeight = Math.clamp(Gdx.graphics.getHeight() * 4 / 5, 1, 720);
        Gdx.input.setInputProcessor(
                new InputAdapter() {
                    @Override
                    public boolean keyDown(int keycode) {
                        if (keycode != Input.Keys.F11) {
                            return false;
                        }
                        Gdx.app.postRunnable(HelloWorld.this::toggleFullscreen);
                        return true;
                    }
                });
        Gdx.graphics.setContinuousRendering(false);
        Gdx.graphics.requestRendering();
    }

    @Override
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        font.getData().setScale(Math.max(0.5f, Math.min(width / 640f, height / 480f)));
        layout.setText(font, MESSAGE);
    }

    @Override
    public void render() {
        // TODO: replace with actual game rendering logic
        ScreenUtils.clear(Color.BLACK);
        batch.begin();
        font.draw(batch, layout, (width - layout.width) / 2f, (height + layout.height) / 2f);
        batch.end();
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
        batch.dispose();
        font.dispose();
    }
}
