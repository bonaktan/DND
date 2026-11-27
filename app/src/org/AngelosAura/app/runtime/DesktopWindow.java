package org.AngelosAura.app.runtime;

import static org.AngelosAura.app.GameTuning.Setting.RENDER_FPS;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import org.lwjgl.glfw.GLFW;

public final class DesktopWindow {
    private int windowedWidth;
    private int windowedHeight;

    public void open(ApplicationListener listener) {
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("DND - Alpha");
        config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        config.setForegroundFPS(RENDER_FPS.defaultValue());
        config.useVsync(true);
        new Lwjgl3Application(listener, config);
    }

    public void installFullscreenShortcut() {
        windowedWidth = Math.clamp(Gdx.graphics.getWidth() * 4 / 5, 1, 960);
        windowedHeight = Math.clamp(Gdx.graphics.getHeight() * 4 / 5, 1, 720);
        Gdx.input.setInputProcessor(
                new InputAdapter() {
                    @Override
                    public boolean keyDown(int keycode) {
                        if (keycode != Input.Keys.F11) {
                            return false;
                        }
                        Gdx.app.postRunnable(DesktopWindow.this::toggleFullscreen);
                        return true;
                    }
                });
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

    // wayland-specific functions, bon uses wayland for devel so syempre may favoritism <3
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
}
