package org.discm.ballsserver;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class BallManager {
    public static List<Ball> balls = new ArrayList<>();

    public static void addBall(Ball ball) {
        balls.add(ball);
    }

    public static void updateBalls(double deltaTime, double canvasWidth, double canvasHeight) {
        for (Ball ball : balls) {
            // Calculate the ball's next position based on its current velocity components
            ball.move(deltaTime);

            // Check canvas collision
            ball.checkCanvasCollision(canvasWidth, canvasHeight);
        }
    }

    public static void drawBalls(GraphicsContext gc, Pane gamePane) {
        double gamePaneX = gamePane.getBoundsInParent().getMinX(); // X-coordinate of the left edge of gamePane
        double gamePaneY = gamePane.getBoundsInParent().getMaxY(); // Y-coordinate of the bottom edge of gamePane

        for (Ball ball : balls) {
            // Adjust coordinates relative to the southwest corner of the gamePane
            double adjustedX = ball.getX() - gamePaneX;
            double adjustedY = gamePaneY - ball.getY(); // Subtract from maxY to convert to bottom-up coordinate system
            double diameter = Ball.getRadius() * 2;
            gc.setFill(Color.BLUE);
            gc.fillOval(adjustedX - Ball.getRadius(), adjustedY - Ball.getRadius(), diameter, diameter);
        }
    }
}
