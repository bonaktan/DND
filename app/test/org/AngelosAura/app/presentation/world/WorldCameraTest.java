package org.AngelosAura.app.presentation.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WorldCameraTest {
    @Test
    void stopsPanningTowardEitherEdge() {
        int viewSize = 45;
        int mapSize = 100;
        float nearEdge = WorldCamera.boundedCenter(0f, viewSize, mapSize);
        float farEdge = WorldCamera.boundedCenter(mapSize - 1f, viewSize, mapSize);

        assertTrue(nearEdge > 0.5f);
        assertTrue(farEdge < mapSize - 0.5f);
        assertEquals(nearEdge, WorldCamera.boundedCenter(-1f, viewSize, mapSize));
        assertEquals(farEdge, WorldCamera.boundedCenter(mapSize, viewSize, mapSize));
    }

    @Test
    void followsTargetsInTheInterior() {
        assertEquals(50.5f, WorldCamera.boundedCenter(50f, 45, 100));
    }

    @Test
    void centersMapsTooSmallToMeetTheEdgeLimit() {
        int mapSize = 2;
        assertEquals(mapSize / 2f, WorldCamera.boundedCenter(0f, 45, mapSize));
        assertEquals(mapSize / 2f, WorldCamera.boundedCenter(1f, 45, mapSize));
    }
}
