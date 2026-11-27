package org.AngelosAura.app.runtime;

import com.badlogic.gdx.utils.LongQueue;
import java.util.concurrent.TimeUnit;

final class RenderTimeAverage {
    private static final long WINDOW_NANOS = TimeUnit.SECONDS.toNanos(1);

    private final LongQueue completionTimes = new LongQueue();
    private final LongQueue durations = new LongQueue();
    private long totalNanos;

    long record(long startNanos, long completedNanos) {
        while (completionTimes.notEmpty()
                && completedNanos - completionTimes.first() >= WINDOW_NANOS) {
            completionTimes.removeFirst();
            totalNanos -= durations.removeFirst();
        }

        long duration = completedNanos - startNanos;
        completionTimes.addLast(completedNanos);
        durations.addLast(duration);
        totalNanos += duration;
        return totalNanos / durations.size;
    }
}
