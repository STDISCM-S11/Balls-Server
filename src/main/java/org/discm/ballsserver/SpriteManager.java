package org.discm.ballsserver;

import javafx.scene.canvas.GraphicsContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SpriteManager {
    public static List<Sprite> sprites = new ArrayList<>(); // Use ArrayList for storing sprites
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

    public static synchronized void updateSpritePosition(UUID spriteId, float x, float y) {
        // Find the sprite by spriteId and update its position
        Sprite spriteToUpdate = sprites.stream()
                .filter(sprite -> sprite.getUUID().equals(spriteId))
                .findFirst()
                .orElse(null);

        if (spriteToUpdate != null) {
            spriteToUpdate.setX(x);
            spriteToUpdate.setY(y);
        }
    }
}