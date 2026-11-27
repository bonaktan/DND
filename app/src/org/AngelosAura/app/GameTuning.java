package org.AngelosAura.app;

import java.util.EnumMap;
import java.util.List;

public final class GameTuning {
    public enum Setting {
        PLAYER_SPEED("Player speed (tiles/s)", 5, 1, 30, 1),
        PLAYER_ACCELERATION("Player acceleration (tiles/s^2)", 25, 1, 100, 5),
        PLAYER_DECELERATION("Player deceleration (tiles/s^2)", 30, 1, 100, 5),
        CAMERA_FOLLOW_MILLIS("Camera follow time (ms)", 500, 50, 2000, 50),
        CAMERA_EASING_POWER("Camera easing power", 2, 1, 8, 1),
        CAMERA_PADDING_PERCENT("Camera edge padding (%)", 5, 0, 50, 1),
        CAMERA_VIEW_COLUMNS("Minimum camera view columns", 40, 1, 120, 1),
        CAMERA_VIEW_ROWS("Minimum camera view rows", 25, 1, 120, 1),
        RENDER_FPS("Render FPS cap", 60, 15, 240, 1);

        private final String label;
        private final int defaultValue;
        private final int minimum;
        private final int maximum;
        private final int step;

        Setting(String label, int defaultValue, int minimum, int maximum, int step) {
            this.label = label;
            this.defaultValue = defaultValue;
            this.minimum = minimum;
            this.maximum = maximum;
            this.step = step;
        }

        public String label() {
            return label;
        }

        public int defaultValue() {
            return defaultValue;
        }
    }

    public static final List<Setting> SETTINGS = List.of(Setting.values());

    private final EnumMap<Setting, Integer> overrides = new EnumMap<>(Setting.class);

    public int get(Setting setting) {
        return overrides.getOrDefault(setting, setting.defaultValue);
    }

    public void adjust(Setting setting, int direction) {
        int value = get(setting) + Integer.signum(direction) * setting.step;
        overrides.put(setting, Math.clamp(value, setting.minimum, setting.maximum));
    }

    public void reset(Setting setting) {
        overrides.remove(setting);
    }
}
