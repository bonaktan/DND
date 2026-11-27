package org.AngelosAura.app;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public final class Main {
    public static void main() {
        startLwjgl3();
    }

    private static void startLwjgl3() {
        var config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Hello, world!");
        config.setFullscreenMode(Lwjgl3ApplicationConfiguration.getDisplayMode());
        config.useVsync(true);
        new Lwjgl3Application(new HelloWorld(), config);
    }
}
