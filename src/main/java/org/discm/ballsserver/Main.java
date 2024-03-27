package org.discm.ballsserver;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.canvas.GraphicsContext;

import java.io.IOException;

public class Main extends Application {

    @FXML
    private Pane gamePane;

    private static final int CANVAS_WIDTH = 1280;
    private static final int CANVAS_HEIGHT = 720;
    private static final int WINDOW_WIDTH = 1920;
    private static final int WINDOW_HEIGHT = 1080;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main.fxml"));
        fxmlLoader.setController(this);
        Parent root = (Parent) fxmlLoader.load();

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        primaryStage.setTitle("Bouncing Balls");
        primaryStage.setScene(scene);
        primaryStage.show();

        Canvas gameCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        gamePane.getChildren().add(gameCanvas);

        AnimationTimer timer = new AnimationTimer() {
            private long lastTime = System.nanoTime();

            @Override
            public void handle(long now) {
                double deltaTime = (now - lastTime) / 1e9; // Convert nanoseconds to seconds
                lastTime = now;

                BallManager.updateBalls(deltaTime, CANVAS_WIDTH, CANVAS_HEIGHT);

                // Clear canvas
                GraphicsContext gc = gameCanvas.getGraphicsContext2D();
                gc.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

                // Draw balls
                BallManager.drawBalls(gc, gamePane);
            }
        };
        timer.start();

        // Set up event handling for the "Spawn ball" button
        Button spawnButton = (Button) scene.lookup("#spawnButton");
        spawnButton.setOnAction(event -> spawnBall());
    }

    private void spawnBall() {
        // Retrieve values from text fields
        TextField xField = (TextField) gamePane.getScene().lookup("#xField");
        TextField yField = (TextField) gamePane.getScene().lookup("#yField");
        TextField angleField = (TextField) gamePane.getScene().lookup("#angleField");
        TextField velocityField = (TextField) gamePane.getScene().lookup("#velocityField");

        double x = Double.parseDouble(xField.getText());
        double y = Double.parseDouble(yField.getText());
        double angle = Double.parseDouble(angleField.getText());
        double velocity = Double.parseDouble(velocityField.getText());

        // Example: Add a ball
        Ball ball = new Ball(x, y, velocity, angle);
        BallManager.addBall(ball);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
