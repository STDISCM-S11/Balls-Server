package org.discm.ballsserver;

import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;

public class SpriteManager {
    public static ArrayList<Sprite> sprites = new ArrayList<>();
    private boolean explorerMode;
    private float pixelWidth = 50.0f, pixelHeight = 50.0f;

    public SpriteManager() {
        this.explorerMode = false; // Default mode
    }

    public void addSprite(Sprite sprite) {
        sprites.add(sprite);
    }

    public void drawSprites(GraphicsContext gc) {
        for (Sprite sprite : sprites) {
            sprite.draw(gc);
        }
    }

    // Additional management methods as needed...
}
