package org.discm.ballsserver;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List; // Make sure to import the List interface

public class Server {
    private static final int PORT = 4000;
    private Map<String, ClientHandler> clientHandlers = Collections.synchronizedMap(new HashMap<>());
    private ScheduledExecutorService broadcastExecutor = Executors.newScheduledThreadPool(1);

    public Server() {
        startServer();
        schedulePeriodicBallUpdateBroadcast();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientId = UUID.randomUUID().toString();
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientId, this);
                clientHandlers.put(clientId, clientHandler);

                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

     private void schedulePeriodicBallUpdateBroadcast() {
        Runnable broadcastTask = () -> {
            JSONArray jsonBalls = new JSONArray();
            List<Ball> balls = BallManager.getBalls(); // Make sure BallManager provides this method in a thread-safe manner
            for (Ball ball : balls) {
                JSONObject jsonBall = new JSONObject();
                jsonBall.put("x", ball.getX());
                jsonBall.put("y", ball.getY());
                // Add other ball attributes as needed
                jsonBalls.put(jsonBall);
            }
            broadcastMessage(null, jsonBalls.toString());
        };

        broadcastExecutor.scheduleAtFixedRate(broadcastTask, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void broadcastMessage(String senderId, String message) {
        for (ClientHandler clientHandler : clientHandlers.values()) {
            if (!clientHandler.getClientId().equals(senderId)) { // Prevent echoing back to sender
                clientHandler.sendMessage(message);
            }
        }
    }

    public void updateSpritePosition(String clientId, float x, float y) {
        // Use SpriteManager to update the sprite position
        SpriteManager.updateSpritePosition(clientId, x, y);

        // Call a method to redraw the GUI with updated sprite positions
        redrawGUI();
    }

    private void redrawGUI() {
        Platform.runLater(() -> {
            // Assuming there's a method in your Main class or elsewhere to redraw or refresh the canvas
            GraphicsContext gc = Main.getGraphicsContext();
            SpriteManager.drawSprites(gc);
        });
    }

//    public static void main(String[] args) {
//        new Server().startServer();
//    }
}