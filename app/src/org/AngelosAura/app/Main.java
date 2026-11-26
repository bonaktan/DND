package org.AngelosAura.app;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private BitmapFont font;

    public static void main() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("libGDX + LWJGL3 Demo");
        config.setWindowedMode(480, 270);
        config.setResizable(false);
        config.useVsync(true);
        new Lwjgl3Application(new Main(), config);
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(2f);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.09f, 0.12f, 0.18f, 1f);
        batch.begin();
        font.draw(batch, "Hello World!", 24, 150);
        batch.end();
    }

    @Override
    public void dispose() {
        font.dispose();
        batch.dispose();
    }

    public int test() {
        return 2;
    }
}
