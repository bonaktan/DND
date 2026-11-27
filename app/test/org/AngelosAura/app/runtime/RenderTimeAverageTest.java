package org.AngelosAura.app.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class RenderTimeAverageTest {
    @Test
    void averagesRendersCompletedWithinTheLastSecond() {
        var average = new RenderTimeAverage();
        long second = TimeUnit.SECONDS.toNanos(1);

        assertEquals(100, average.record(0, 100));
        assertEquals(200, average.record(second / 2 - 300, second / 2));
        assertEquals(400, average.record(second - 400, second + 100));
        assertEquals(700, average.record(3 * second - 700, 3 * second));
    }
}
