package org.AngelosAura.app.game;

public abstract class Mob {
    private float x;
    private float y;

    protected Mob(float x, float y) {
        moveTo(x, y);
    }

    public final float x() {
        return x;
    }

    public final float y() {
        return y;
    }

    final void moveTo(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
