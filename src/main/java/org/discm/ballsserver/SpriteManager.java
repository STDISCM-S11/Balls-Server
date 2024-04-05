package org.discm.ballsserver;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
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

    public static void drawSprites(GraphicsContext gc) {
        for (Sprite sprite : sprites) {
            sprite.draw(gc);
        }
    }

    public static synchronized void updateSpritePosition(String spriteId, float x, float y) {
        Sprite spriteToUpdate = sprites.stream()
                .filter(sprite -> sprite.getUUID().equals(spriteId))
                .findFirst()
                .orElse(null);
    
        if (spriteToUpdate == null) {
            // Create a new sprite with the given ID if it doesn't exist
            spriteToUpdate = new Sprite(spriteId, x, y);
            sprites.add(spriteToUpdate);
        } else {
            // Update existing sprite's position
            spriteToUpdate.setX(x);
            spriteToUpdate.setY(y);
        }
    }

    public static synchronized void removeSprite(String spriteId) {
        Platform.runLater(() -> {
            // Find the sprite to remove by UUID
            Sprite spriteToRemove = sprites.stream()
                    .filter(sprite -> sprite.getUUID().equals(spriteId.substring(0, spriteId.length() - 1)))
                    .findFirst()
                    .orElse(null);
            if (spriteToRemove != null) {
                // Change the sprite color to match the background
                spriteToRemove.setColor(Color.CORNFLOWERBLUE); // or your background color

                // Redraw the sprite with the background color to "erase" it
                Main.getGraphicsContext().fillRect(spriteToRemove.getX(), spriteToRemove.getY(), spriteToRemove.getHeight(), spriteToRemove.getHeight());

                // Finally, remove the sprite from the list
//                System.out.println("Remove sprite" + sprites.toArray().length);

                sprites.remove(spriteToRemove);
//                System.out.println("Removed already " + sprites.toArray().length);
//                System.out.println("removing sprite");
            }
        });
    }
}