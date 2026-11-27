package org.AngelosAura.app;

import org.AngelosAura.app.runtime.DesktopWindow;
import org.AngelosAura.app.runtime.GameApplication;

public final class Main {
    public static void main() {
        var window = new DesktopWindow();
        window.open(new GameApplication(window));
    }
}
