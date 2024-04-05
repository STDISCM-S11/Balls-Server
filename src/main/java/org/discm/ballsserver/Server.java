package org.discm.ballsserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private static final int PORT = 4000;
    private Map<String, ClientHandler> clientHandlers = Collections.synchronizedMap(new HashMap<>());
    private boolean sendBalls = true;
    private ArrayList<Ball> ballsToSend = new ArrayList<Ball>();
    ReentrantLock lock = new ReentrantLock();

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
//                lock.lock();
                Socket clientSocket = serverSocket.accept();
                String clientId = UUID.randomUUID().toString();
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientId, this);
                clientHandlers.put(clientId, clientHandler);
                new Thread(clientHandler).start();
                sendInitialBalls(clientHandler);
//                lock.unlock();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String senderId) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> messageData = new HashMap<String, Object>();
        String message;
        for (ClientHandler clientHandler : clientHandlers.values()) {
            Object[] broadcastSprites = SpriteManager.sprites.stream()
                    .filter(sprite -> !sprite.getUUID().equals(clientHandler.getClientId()))
                    .toArray();
            if(sendBalls){
//                System.out.println(ballsToSend);
                messageData.put("ballData", this.ballsToSend.toArray());
                System.out.println(clientHandler.getClientId() + ": " + sendBalls);

            }
            messageData.put("spriteData", broadcastSprites);
            message = mapper.writeValueAsString(messageData);
//            System.out.println(clientHandler.getClientId() + ": " + message);
//            System.out.println(message);
            clientHandler.sendMessage(message + "\n");
        }
        sendBalls = false;
        ballsToSend.clear();
    }

    public void sendInitialBalls(ClientHandler clientHandler){
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> messageData = new HashMap<String, Object>();
        String message;
        messageData.put("ballData", BallManager.balls);
        try {
            message = mapper.writeValueAsString(messageData);
//            System.out.println(message);
            clientHandler.sendMessage(message + "\n");

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }
    public void sendBalls(ArrayList<Ball> balls) {
        sendBalls = true;
//        System.out.println(balls.toArray().length);
        this.ballsToSend.addAll(balls);
//        ballsToSend.addAll(balls);
//        ObjectMapper mapper = new ObjectMapper();
//        Map<String, Object> messageData = new HashMap<String, Object>();
//        String message;
//        messageData.put("ballData", balls.toArray());
//
//        // {ballData: [{"x": float, "y": float}]}
//        try {
//            message = mapper.writeValueAsString(messageData);
//
//            for (ClientHandler clientHandler : clientHandlers.values()) {
//                clientHandler.sendMessage(message + "\n");
//            }
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException(e);
//        }

    }

    public void updateSpritePosition(String clientId, float x, float y) {
        // Use SpriteManager to update the sprite position
        SpriteManager.updateSpritePosition(clientId, x, y);

        // Call a method to redraw the GUI with updated sprite positions
        redrawGUI();
    }

    private void redrawGUI() {
        Platform.runLater(() -> {
            GraphicsContext gc = Main.getGraphicsContext();
            gc.clearRect(0, 0, Main.getCanvasWidth(), Main.getCanvasHeight());
            SpriteManager.drawSprites(gc);
        });
    }

    public synchronized void clientDisconnected(String clientId) {
        lock.lock();
        // This method will handle everything related to a client disconnection.
        SpriteManager.removeSprite(clientId); // Remove the sprite.
        clientHandlers.remove(clientId); // Remove the client handler.
        lock.unlock();
        // Update the GUI to reflect the removal of the sprite.
        redrawGUI();
    }

    public synchronized void removeClient(String clientId) {
        clientHandlers.remove(clientId);
    }

    public synchronized void removeSprite(String spriteId) {
        SpriteManager.removeSprite(spriteId);
        redrawGUI();
    }

}