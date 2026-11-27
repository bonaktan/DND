package org.AngelosAura.app.presentation.world;

import static org.AngelosAura.app.GameTuning.Setting.CAMERA_PADDING_PERCENT;
import static org.AngelosAura.app.GameTuning.Setting.CAMERA_VIEW_COLUMNS;
import static org.AngelosAura.app.GameTuning.Setting.CAMERA_VIEW_ROWS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.AngelosAura.app.GameTuning;
import org.junit.jupiter.api.Test;

class WorldCameraTest {
    @Test
    void expandsTheViewToMatchWideAndTallScreens() {
        int minColumns = CAMERA_VIEW_COLUMNS.defaultValue();
        int minRows = CAMERA_VIEW_ROWS.defaultValue();
        WorldCamera camera = new WorldCamera(0f, 0f, 2, 2);

        camera.resize(2 * minColumns + 1, 2 * minRows);
        assertEquals(minColumns + 0.5f, camera.viewColumns());
        assertEquals(minRows, camera.viewRows());

        camera.resize(2 * minColumns, 2 * minRows + 1);
        assertEquals(minColumns, camera.viewColumns());
        assertEquals(minRows + 0.5f, camera.viewRows());

        camera.resize(minColumns, minRows);
        assertEquals(minColumns, camera.viewColumns());
        assertEquals(minRows, camera.viewRows());
    }

    @Test
    void appliesNewMinimumsWithoutResizingTheScreen() {
        GameTuning tuning = new GameTuning();
        int minColumns = tuning.get(CAMERA_VIEW_COLUMNS);
        int minRows = tuning.get(CAMERA_VIEW_ROWS);
        WorldCamera camera = new WorldCamera(0f, 0f, 100, 100, tuning);
        camera.resize(2 * minColumns, minRows);

        tuning.adjust(CAMERA_VIEW_ROWS, 1);
        camera.follow(0f, 0f, 0f);

        assertTrue(camera.viewRows() > minRows);
        assertTrue(camera.viewColumns() > 2 * minColumns);
        assertEquals(tuning.get(CAMERA_VIEW_ROWS), camera.viewRows());
    }

    @Test
    void stopsPanningTowardEitherEdge() {
        int viewSize = 45;
        int mapSize = 100;
        float nearEdge = boundedCenter(0f, viewSize, mapSize);
        float farEdge = boundedCenter(mapSize - 1f, viewSize, mapSize);

        assertTrue(nearEdge > 0.5f);
        assertTrue(farEdge < mapSize - 0.5f);
        assertEquals(nearEdge, boundedCenter(-1f, viewSize, mapSize));
        assertEquals(farEdge, boundedCenter(mapSize, viewSize, mapSize));
    }

    @Test
    void followsTargetsInTheInterior() {
        assertEquals(50.5f, boundedCenter(50f, 45, 100));
    }

    @Test
    void centersMapsTooSmallToMeetTheEdgeLimit() {
        int mapSize = 2;
        assertEquals(mapSize / 2f, boundedCenter(0f, 45, mapSize));
        assertEquals(mapSize / 2f, boundedCenter(1f, 45, mapSize));
    }

    @Test
    void moreEdgePaddingLetsTheCameraGetCloserToTheEdge() {
        assertTrue(
                WorldCamera.boundedCenter(0f, 45, 100, 10)
                        < WorldCamera.boundedCenter(0f, 45, 100, 0));
    }

    private static float boundedCenter(float target, int viewSize, int mapSize) {
        return WorldCamera.boundedCenter(
                target, viewSize, mapSize, CAMERA_PADDING_PERCENT.defaultValue());
    }
}
