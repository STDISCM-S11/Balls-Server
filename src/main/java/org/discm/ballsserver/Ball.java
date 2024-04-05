package org.discm.ballsserver;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Ball {
    private static final double PI = Math.PI;
    private static final double radius = 5.0; // Adjust the radius as needed

    private double x;
    private double y;
    private double velocity;
    private double angle;

    private double dx;
    private double dy;

    public Ball(double startX, double startY, double velocity, double startAngle) {
        this.x = startX;
        this.y = startY;
        this.velocity = velocity;
        this.angle = startAngle;

        double angleRadians = Math.toRadians(startAngle);
        dx = velocity * Math.cos(angleRadians);
        dy = velocity * Math.sin(angleRadians);
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.ORANGE);
        gc.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
    }

    public void move(double deltaTime) {
        // Predict the ball's next position based on its current velocity
        x += dx * deltaTime;
        y += dy * deltaTime;
    }

    public void checkCanvasCollision(double width, double height) {
        double radiusThreshold = radius;
        double leftBoundary = 0 + radiusThreshold;
        double rightBoundary = width - radiusThreshold;
        double bottomBoundary = 0 + radiusThreshold;
        double topBoundary = height - radiusThreshold;

        if (x < leftBoundary || x > rightBoundary) {
            dx *= -1;
            x = Math.min(Math.max(x, leftBoundary), rightBoundary);
        }

        if (y < bottomBoundary || y > topBoundary) {
            dy *= -1;
            y = Math.min(Math.max(y, bottomBoundary), topBoundary);
        }
        normalizeVelocity();
    }

    public void invertDirection() {
        dx *= -1;
        dy *= -1;
        normalizeVelocity();
    }

    private void normalizeVelocity() {
        double velocityMagnitude = Math.sqrt(dx * dx + dy * dy);
        dx = (dx / velocityMagnitude) * velocity;
        dy = (dy / velocityMagnitude) * velocity;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getAngle() {
        return angle;
    }

    public double getVelocity() {
        return velocity;
    }

    public static double getRadius() {
        return radius;
    }


}
