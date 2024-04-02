package org.discm.ballsserver;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.canvas.GraphicsContext;
import javafx.application.Platform;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main extends Application {

    @FXML
    private Pane gamePane;

    // Spawn Buttons for each form
    @FXML
    private Button spawnButton0, spawnButton1, spawnButton2, spawnButton3;

    private static final int CANVAS_WIDTH = 1280;
    private static final int CANVAS_HEIGHT = 720;
    private static final int WINDOW_WIDTH = 1920;
    private static final int WINDOW_HEIGHT = 1080;

    private Server server;
    private SpriteManager spriteManager;

    private final long optimalTime = 1_000_000_000L / 60; // Class field for optimal time
    private long lastTime = 0; // Class field for last time
    private static GraphicsContext graphicsContext; // Static field for the GraphicsContext


    @Override
    public void start(Stage primaryStage) throws IOException {
        ExecutorService thread = Executors.newSingleThreadExecutor();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        fxmlLoader.setController(this);
        Parent root = (Parent) fxmlLoader.load();

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        primaryStage.setTitle("Distributed Particle Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        Canvas gameCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gamePane.getChildren().add(gameCanvas);
        graphicsContext = gameCanvas.getGraphicsContext2D(); // Initialize the static field

        // Initialize sprite manager and create a sprite
        spriteManager = new SpriteManager();
//        spriteManager.drawSprites(); // Example position

        server = new Server();
        thread.execute(() -> server.startServer());

        setupAnimationTimer(gameCanvas);

        // Set up event handling for the "Spawn ball" button
//        Button spawnButton = (Button) scene.lookup("#spawnButton");
//        spawnButton.setOnAction(event -> spawnBall());
        spawnButton0.setOnAction(event -> spawnBall0());
        spawnButton1.setOnAction(event -> spawnBall1());
        spawnButton2.setOnAction(event -> spawnBall2());
        spawnButton3.setOnAction(event -> spawnBall3());
    }

    public void setupAnimationTimer(Canvas gameCanvas) {
        final long[] frameTimes = new long[100];
        final int[] frameTimeIndex = {0};
        final boolean[] arrayFilled = {false};
        final long[] lastUpdateTime = {System.nanoTime()}; // Make this an array


        Label fpsLabel = (Label) gamePane.getScene().lookup("#fpsLabel");
        Label fpsLabel1 = (Label) gamePane.getScene().lookup("#fpsLabel1");
        Label fpsLabel2 = (Label) gamePane.getScene().lookup("#fpsLabel2");
        Label fpsLabel3 = (Label) gamePane.getScene().lookup("#fpsLabel3");

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long oldFrameTime = frameTimes[frameTimeIndex[0]];
                frameTimes[frameTimeIndex[0]] = now;
                frameTimeIndex[0] = (frameTimeIndex[0] + 1) % frameTimes.length;
                if (frameTimeIndex[0] == 0) {
                    arrayFilled[0] = true;
                }
                if ((now - lastUpdateTime[0]) >= optimalTime) {
                    lastUpdateTime[0] = now;
                    if (arrayFilled[0]) {
                        long elapsedNanos = now - oldFrameTime;
                        long elapsedNanosPerFrame = elapsedNanos / frameTimes.length;
                        double frameRate = 1_000_000_000.0 / elapsedNanosPerFrame;
                        if(frameRate > 60.0) {
                            frameRate = 60.0;
                        }

                        double finalFrameRate = frameRate; // Must be effectively final for use in lambda
                        Platform.runLater(() -> {
                            fpsLabel.setText(String.format("FPS: %.1f", finalFrameRate));
                            fpsLabel1.setText(String.format("FPS: %.1f", finalFrameRate));
                            fpsLabel2.setText(String.format("FPS: %.1f", finalFrameRate));
                            fpsLabel3.setText(String.format("FPS: %.1f", finalFrameRate));
                        });
                    }
                }

                double deltaTime = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                // Existing ball update and drawing logic
                BallManager.updateBalls(deltaTime, CANVAS_WIDTH, CANVAS_HEIGHT);
                GraphicsContext gc = gameCanvas.getGraphicsContext2D();
                gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
                BallManager.drawBalls(gc, gamePane);
                draw(gameCanvas.getGraphicsContext2D());
            }
        };
        timer.start();
    }


    private void draw(GraphicsContext gc) {
        gc.save(); // Save the current state of the GraphicsContext

        // Clear the canvas
        gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        // Apply transformation to flip the y-axis
        gc.scale(1, -1);
        gc.translate(0, -CANVAS_HEIGHT);

        // Now (0,0) is at the bottom left, and you can draw using these coordinates
        BallManager.drawBalls(gc, gamePane);
        spriteManager.drawSprites(gc); // Ensure sprite drawing logic accounts for the flipped y-axis

        gc.restore(); // Restore the original state of the GraphicsContext
    }

    private void spawnBall0() {
        // Retrieve values from text fields
        TextField xField = (TextField) gamePane.getScene().lookup("#xField");
        TextField yField = (TextField) gamePane.getScene().lookup("#yField");
        TextField angleField = (TextField) gamePane.getScene().lookup("#angleField");
        TextField velocityField = (TextField) gamePane.getScene().lookup("#velocityField");

        // Retrieve values from text fields
        double x = Double.parseDouble(xField.getText());
        // Subtract y from CANVAS_HEIGHT to flip the y-coordinate
        double y = CANVAS_HEIGHT - Double.parseDouble(yField.getText());
        double angle = Double.parseDouble(angleField.getText());
        double velocity = Double.parseDouble(velocityField.getText());

        // Adjusted y-coordinate for the bottom-left origin
        Ball ball = new Ball(x, y, velocity, angle);
        BallManager.addBall(ball);
    }

    private void spawnBall1() {
        // Implementation for spawning ball from Form 1
        try {
            TextField n1 = (TextField) gamePane.getScene().lookup("#n1");
            TextField startX1 = (TextField) gamePane.getScene().lookup("#startX1");
            TextField startY1 = (TextField) gamePane.getScene().lookup("#startY1");
            TextField endX1 = (TextField) gamePane.getScene().lookup("#endX1");
            TextField endY1 = (TextField) gamePane.getScene().lookup("#endY1");
            TextField angle1 = (TextField) gamePane.getScene().lookup("#angle1");
            TextField velocity1 = (TextField) gamePane.getScene().lookup("#velocity1");

            int numBalls = Integer.parseInt(n1.getText());
            double startX = Double.parseDouble(startX1.getText());
            double startY = CANVAS_HEIGHT - Double.parseDouble(startY1.getText());
            double endX = Double.parseDouble(endX1.getText());
            double endY = CANVAS_HEIGHT - Double.parseDouble(endY1.getText());
            double angle = Double.parseDouble(angle1.getText());
            double velocity = Double.parseDouble(velocity1.getText());

            // Calculate the number of balls to spawn based on distance between start and end points
            double distance = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));

            double spacing = distance/(numBalls -1);

            // Calculate the x and y increments per ball
            double deltaX = (endX - startX) / distance; // Use numBalls - 1 for spacing
            double deltaY = (endY - startY) / distance; // Use numBalls - 1 for spacing

            // Now spawning balls with adjusted y-coordinates
            for (int i = 0; i < numBalls; i++) {
                double x = startX + deltaX* spacing * i;
                double y = startY + deltaY * spacing * i;
                Ball ball = new Ball(x, y, velocity, angle);
                BallManager.addBall(ball);
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid input in Form 1.");
        }
    }

    private void spawnBall2() {
        // Implementation for spawning ball from Form 2
        try {
            TextField n2 = (TextField) gamePane.getScene().lookup("#n2");
            TextField x2 = (TextField) gamePane.getScene().lookup("#x2");
            TextField y2 = (TextField) gamePane.getScene().lookup("#y2");
            TextField startAngle2 = (TextField) gamePane.getScene().lookup("#startAngle2");
            TextField endAngle2 = (TextField) gamePane.getScene().lookup("#endAngle2");
            TextField velocity2 = (TextField) gamePane.getScene().lookup("#velocity2");

            int numBalls = Integer.parseInt(n2.getText());
            double x = Double.parseDouble(x2.getText());
            // Adjust y from canvas height to flip y-coordinate
            double y = CANVAS_HEIGHT - Double.parseDouble(y2.getText());
            double startAngle = Double.parseDouble(startAngle2.getText());
            double endAngle = Double.parseDouble(endAngle2.getText());
            double velocity = Double.parseDouble(velocity2.getText());

            // Calculate the angle increment per ball
            double deltaAngle = (endAngle - startAngle) / numBalls;

            // Adjusting angle calculation for flipped y-coordinate as necessary
            for (int i = 0; i <= numBalls; i++) {
                double angle = startAngle + deltaAngle * i;
                Ball ball = new Ball(x, y, velocity, angle);
                BallManager.addBall(ball);
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid input in Form 2.");
        }
    }

    private void spawnBall3() {
        // Implementation for spawning ball from Form 3
        try {
            TextField n3 = (TextField) gamePane.getScene().lookup("#n3");
            TextField x3 = (TextField) gamePane.getScene().lookup("#x3");
            TextField y3 = (TextField) gamePane.getScene().lookup("#y3");
            TextField angle3 = (TextField) gamePane.getScene().lookup("#angle3");
            TextField startVelocity3 = (TextField) gamePane.getScene().lookup("#startVelocity3");
            TextField endVelocity3 = (TextField) gamePane.getScene().lookup("#endVelocity3");

            int numBalls = Integer.parseInt(n3.getText());
            double x = Double.parseDouble(x3.getText());
            // Adjust y from canvas height to flip y-coordinate
            double y = CANVAS_HEIGHT - Double.parseDouble(y3.getText());
            double angle = Double.parseDouble(angle3.getText());
            double startVelocity = Double.parseDouble(startVelocity3.getText());
            double endVelocity = Double.parseDouble(endVelocity3.getText());

            // Calculate the velocity increment per ball
            double deltaVelocity = (endVelocity - startVelocity) / numBalls;

            // Spawn balls
            // Adjusting for the number of balls and velocity spread
            for (int i = 0; i <= numBalls; i++) {
                double velocity = startVelocity + deltaVelocity * i;
                Ball ball = new Ball(x, y, velocity, angle);
                BallManager.addBall(ball);
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid input in Form 3.");
        }
    }

    public static GraphicsContext getGraphicsContext() {
        return graphicsContext;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
