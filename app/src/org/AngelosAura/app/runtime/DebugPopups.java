package org.AngelosAura.app.runtime;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.HdpiUtils;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Disposable;
import org.AngelosAura.app.presentation.world.WorldCamera;

final class DebugPopups implements Disposable {
    private static final int SCREEN_MARGIN = 12;
    private static final int PANEL_GAP = 8;
    private static final int PANEL_PADDING = 10;
    private static final int MIN_PANEL_WIDTH = 204;
    private static final int PERFORMANCE_PANEL_HEIGHT = 66;
    private static final int ENVIRONMENT_PANEL_HEIGHT = 82;
    private static final Color PANEL_COLOR = new Color(0.07f, 0.08f, 0.11f, 1f);
    private static final int HUNDREDTHS_PER_MILLISECOND = 100;
    private static final int NANOS_PER_HUNDREDTH_OF_MILLISECOND = 10_000;

    private final ShapeRenderer background = new ShapeRenderer();
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final StringBuilder text = new StringBuilder();
    private final GlyphLayout layout = new GlyphLayout();

    void renderPerformance(int fps, long averageRenderNanos, long simulationNanos) {
        text.setLength(0);
        text.append("FPS: ").append(fps).append("\nRender time (1s avg): ");
        appendMilliseconds(averageRenderNanos);
        text.append("\nSimulation time: ");
        appendMilliseconds(simulationNanos);
        renderPanel(0, PERFORMANCE_PANEL_HEIGHT);
    }

    void renderEnvironment(
            float playerX,
            float playerY,
            WorldCamera camera,
            int columns,
            int rows,
            boolean belowPerformance) {
        text.setLength(0);
        text.append("Player: ").append(playerX).append(", ").append(playerY).append(" tiles");
        text.append("\nCamera: ")
                .append(camera.x())
                .append(", ")
                .append(camera.y())
                .append(" tiles");
        text.append("\nMap: ").append(columns).append(" x ").append(rows).append(" tiles");
        text.append("\nViewport: ")
                .append(camera.viewColumns())
                .append(" x ")
                .append(camera.viewRows())
                .append(" tiles");
        renderPanel(
                belowPerformance ? PERFORMANCE_PANEL_HEIGHT + PANEL_GAP : 0,
                ENVIRONMENT_PANEL_HEIGHT);
    }

    private void renderPanel(int topOffset, int panelHeight) {
        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        int panelTop = height - SCREEN_MARGIN - topOffset;
        layout.setText(font, text);
        float panelWidth =
                Math.max(MIN_PANEL_WIDTH, (float) Math.ceil(layout.width) + 2 * PANEL_PADDING);
        HdpiUtils.glViewport(0, 0, width, height);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
        background.setProjectionMatrix(batch.getProjectionMatrix());

        background.begin(ShapeRenderer.ShapeType.Filled);
        background.setColor(PANEL_COLOR);
        background.rect(SCREEN_MARGIN, panelTop - panelHeight, panelWidth, panelHeight);
        background.end();

        batch.begin();
        font.draw(batch, layout, SCREEN_MARGIN + PANEL_PADDING, panelTop - PANEL_PADDING);
        batch.end();
    }

    private void appendMilliseconds(long nanos) {
        long hundredths = nanos / NANOS_PER_HUNDREDTH_OF_MILLISECOND;
        long fraction = hundredths % HUNDREDTHS_PER_MILLISECOND;
        text.append(hundredths / HUNDREDTHS_PER_MILLISECOND).append('.');
        if (fraction < 10) {
            text.append('0');
        }
        text.append(fraction).append(" ms");
    }

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
        background.dispose();
    }
}
