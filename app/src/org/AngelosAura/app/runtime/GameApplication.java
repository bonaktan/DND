package org.AngelosAura.app.runtime;

import static org.AngelosAura.app.GameTuning.Setting.RENDER_FPS;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import java.net.URISyntaxException;
import java.nio.file.Path;
import org.AngelosAura.app.GameTuning;
import org.AngelosAura.app.game.GameSession;
import org.AngelosAura.app.presentation.world.WorldCamera;
import org.AngelosAura.app.presentation.world.WorldRenderer;

public final class GameApplication extends ApplicationAdapter {
    private static final float SIMULATION_STEP_SECONDS = 1f / 60f;
    private static final float MAX_CATCH_UP_SECONDS = 0.25f;

    private final DesktopWindow desktopWindow;
    private final GameTuning tuning = new GameTuning();
    private final RenderTimeAverage renderTimeAverage = new RenderTimeAverage();
    private WorldRenderer worldRenderer;
    private GameSession gameSession;
    private WorldCamera camera;
    private DebugPopups debugPopups;
    private boolean showPerformance;
    private boolean showEnvironment;
    private boolean showTuning;
    private int selectedTuningIndex;
    private float simulationTime;
    private int pendingMoveX;
    private int pendingMoveY;

    public GameApplication(DesktopWindow desktopWindow) {
        this.desktopWindow = desktopWindow;
    }

    @Override
    public void create() {
        Path assets = resolveAssetsRoot();
        worldRenderer =
                new WorldRenderer(
                        assets.resolve("map/TheVillage.tmx"), assets.resolve("textures/cat.png"));

        int columns = worldRenderer.columns();
        int rows = worldRenderer.rows();
        gameSession = new GameSession(columns, rows, tuning);
        var player = gameSession.player();
        camera = new WorldCamera(player.x(), player.y(), columns, rows, tuning);
        desktopWindow.installFullscreenShortcut();
        Gdx.graphics.requestRendering();
    }

    @Override
    public void resize(int width, int height) {
        camera.resize(width, height);
    }

    @Override
    public void render() {
        updateDebugShortcuts();
        if (showTuning) {
            updateTuningControls();
        }

        float dt = Gdx.graphics.getDeltaTime();
        long simulationStart = showPerformance ? System.nanoTime() : 0;
        updateMovement(dt);
        long renderStart = System.nanoTime();

        var player = gameSession.player();
        camera.follow(player.x(), player.y(), dt);
        worldRenderer.render(camera, player.x(), player.y());
        long averageRenderNanos = renderTimeAverage.record(renderStart, System.nanoTime());

        if (showPerformance || showEnvironment || showTuning) {
            if (debugPopups == null) {
                debugPopups = new DebugPopups();
            }
            if (showPerformance) {
                debugPopups.renderPerformance(
                        Gdx.graphics.getFramesPerSecond(),
                        averageRenderNanos,
                        renderStart - simulationStart);
            }
            if (showEnvironment) {
                debugPopups.renderEnvironment(
                        player.x(),
                        player.y(),
                        camera,
                        worldRenderer.columns(),
                        worldRenderer.rows(),
                        showPerformance);
            }
            if (showTuning) {
                debugPopups.renderTuning(
                        tuning, selectedTuningIndex, showPerformance, showEnvironment);
            }
        }
    }

    private void updateDebugShortcuts() {
        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.F1)) {
                showPerformance = !showPerformance;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.F2)) {
                showEnvironment = !showEnvironment;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
                showTuning = !showTuning;
            }
        }
    }

    private void updateTuningControls() {
        int selectionDelta =
                (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_DOWN) ? 1 : 0)
                        - (Gdx.input.isKeyJustPressed(Input.Keys.PAGE_UP) ? 1 : 0);
        selectedTuningIndex =
                Math.floorMod(selectedTuningIndex + selectionDelta, GameTuning.SETTINGS.size());
        var setting = GameTuning.SETTINGS.get(selectedTuningIndex);
        int previousFps = tuning.get(RENDER_FPS);

        int adjustment =
                (Gdx.input.isKeyJustPressed(Input.Keys.EQUALS) ? 1 : 0)
                        - (Gdx.input.isKeyJustPressed(Input.Keys.MINUS) ? 1 : 0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE)) {
            tuning.reset(setting);
        } else if (adjustment != 0) {
            tuning.adjust(setting, adjustment);
        }

        int renderFps = tuning.get(RENDER_FPS);
        if (renderFps != previousFps) {
            Gdx.graphics.setForegroundFPS(renderFps);
        }
    }

    private void updateMovement(float dt) {
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
        // Keep a movement input that begins and ends between simulation updates.
        if (dx != 0 || dy != 0) {
            pendingMoveX = dx;
            pendingMoveY = dy;
        }

        simulationTime = Math.min(simulationTime + dt, MAX_CATCH_UP_SECONDS);
        int moveX = pendingMoveX;
        int moveY = pendingMoveY;
        while (simulationTime >= SIMULATION_STEP_SECONDS) {
            gameSession.movePlayer(moveX, moveY, SIMULATION_STEP_SECONDS);
            simulationTime -= SIMULATION_STEP_SECONDS;
            pendingMoveX = 0;
            pendingMoveY = 0;
            moveX = dx;
            moveY = dy;
        }
    }

    @Override
    public void dispose() {
        worldRenderer.dispose();
        if (debugPopups != null) {
            debugPopups.dispose();
        }
    }

    private static Path resolveAssetsRoot() {
        String configured = System.getProperty("dnd.assets");
        if (configured != null) {
            return Path.of(configured).toAbsolutePath().normalize();
        }

        try {
            Path appJar =
                    Path.of(
                            GameApplication.class
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
