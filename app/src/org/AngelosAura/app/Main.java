package org.AngelosAura.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.AngelosAura.app.render.WindowRenderer;

public final class Main {
    public static void main() {
        startLwjgl3();
    }

    private static void startLwjgl3() {
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("DND - Alpha");
        config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        config.useVsync(true);
        new Lwjgl3Application(new WindowRenderer(), config);
    }
}
