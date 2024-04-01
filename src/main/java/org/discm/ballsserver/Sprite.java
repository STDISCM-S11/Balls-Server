package org.discm.ballsserver;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.UUID;

public class Sprite {
    private float x, y, height = 10.0f; // Default height
    UUID uuid;

    public Sprite(UUID uuid, float startX, float startY) {
        this.uuid = uuid;
        this.x = startX;
        this.y = startY;
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.GREEN); // Example color, adjust as needed
        // Drawing a square for simplicity, adjust as needed for your sprite
        gc.fillRect(x - height / 2, y - height / 2, height, height);
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

    public UUID getUUID(){return uuid;}
}
