package org.discm.ballsserver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Stream;

public class Server {
    private static final int PORT = 4000;
    private Map<String, ClientHandler> clientHandlers = Collections.synchronizedMap(new HashMap<>());

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                String clientId = UUID.randomUUID().toString();
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientId, this);
                clientHandlers.put(clientId, clientHandler);
                new Thread(clientHandler).start();
                sendInitialBalls(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String senderId) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> messageData = new HashMap<String, Object>();
        String message;
        if(clientHandlers.size() < 2){
            return;
        }
        for (ClientHandler clientHandler : clientHandlers.values()) {
            Object[] broadcastSprites = SpriteManager.sprites.stream()
                    .filter(sprite -> !sprite.getUUID().equals(clientHandler.getClientId()))
                    .toArray();

            messageData.put("spriteData", broadcastSprites);
            message = mapper.writeValueAsString(messageData);
            clientHandler.sendMessage(message + "\n");
        }
    }

    public void sendInitialBalls(ClientHandler clientHandler){
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> messageData = new HashMap<String, Object>();
        String message;
        messageData.put("ballData", BallManager.balls);
        try {
            message = mapper.writeValueAsString(messageData);
            clientHandler.sendMessage(message + "\n");

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }


    }
    public void sendBalls(ArrayList<Ball> balls) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> messageData = new HashMap<String, Object>();
        String message;
        messageData.put("ballData", balls);
        try {
            message = mapper.writeValueAsString(messageData);
            for (ClientHandler clientHandler : clientHandlers.values()) {
                clientHandler.sendMessage(message + "\n");
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
            GraphicsContext gc = Main.getGraphicsContext();
            gc.clearRect(0, 0, Main.getCanvasWidth(), Main.getCanvasHeight());
            SpriteManager.drawSprites(gc);
        });
    }

    public synchronized void clientDisconnected(String clientId) {
        // This method will handle everything related to a client disconnection.
        SpriteManager.removeSprite(clientId); // Remove the sprite.
        clientHandlers.remove(clientId); // Remove the client handler.

        // Update the GUI to reflect the removal of the sprite.
        redrawGUI();
    }

    public synchronized void removeClient(String clientId) {
        clientHandlers.remove(clientId);
        // Additional cleanup if necessary
    }

    public synchronized void removeSprite(String spriteId) {
        SpriteManager.removeSprite(spriteId);
        redrawGUI();
    }

//    public static void main(String[] args) {
//        new Server().startServer();
//    }
}