package org.discm.ballsserver;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Sprite {
    private float x, y, height = 10.0f; // Default height
    private Color color; // Color property for the sprite
    String uuid;

    public Sprite(String uuid, float startX, float startY) {
        this.uuid = uuid;
        this.x = startX;
        this.y = startY;
        this.color = Color.GREEN; // Default sprite color, can be changed
    }

    public void draw(GraphicsContext gc) {
        // Save the current state of the GraphicsContext
        gc.save();

        // Translate to the center of the sprite to rotate around its center
        gc.translate(x, y);

        // Rotate 45 degrees (to make the square look like a diamond)
        gc.rotate(45);

        // Need to offset the square back to its intended position after rotation
        gc.setFill(color); // Set the fill color for the sprite
        gc.fillRect(-height / 2, -height / 2, height, height);

        // Restore the original state of the GraphicsContext
        gc.restore();
    }

    // Movement methods
    public void moveUp(float amount) {
        this.y += amount;
        adjustHeight();
    }

    public void moveDown(float amount) {
        this.y -= amount;
        adjustHeight();
    }

    public void moveLeft(float amount) {
        this.x -= amount;
        adjustHeight();
    }

    public void moveRight(float amount) {
        this.x += amount;
        adjustHeight();
    }

    private void adjustHeight() {
        this.height = this.height == 10.0f ? 7.5f : 10.0f;
    }

    // Getters and setters
    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String getUUID(){return uuid;}

    public void setColor(Color newColor) {
        this.color = newColor;
    }

    public float getHeight() {
        return height;
    }
}
