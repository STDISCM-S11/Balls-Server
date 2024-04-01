package org.discm.ballsserver;

import org.json.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;


public class Server {
    private ServerSocket server = null;
//    private Map<Integer, DataOutputStream> clientOutputStreams = new ConcurrentHashMap<>();
//    private int clientIdCounter = 0;

    private ScheduledExecutorService processingExecutor;

    private ScheduledExecutorService broadcastExecutor;
    private Queue<JSONObject> ballUpdates = new ConcurrentLinkedQueue<>();
    private Map<UUID, DataOutputStream> clientOutputStreams = new ConcurrentHashMap<>();
    private AtomicInteger clientIdCounter = new AtomicInteger();



    public Server(int port) {
        try {
            server = new ServerSocket(port);
            System.out.println("Server started on port: " + port);
            broadcastExecutor = Executors.newScheduledThreadPool(2);
//            scheduleBroadcastTasks();
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void scheduleBroadcastTasks() {
        // Broadcast balls every 100 milliseconds
        broadcastExecutor.scheduleAtFixedRate(this::broadcastBallsInBatches, 0, 100, TimeUnit.MILLISECONDS);
        // Broadcast sprites every 100 milliseconds (or use a different period if needed)
        broadcastExecutor.scheduleAtFixedRate(this::broadcastSprites, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void runServer() {
        ExecutorService clientHandlerPool = Executors.newCachedThreadPool();
        try {
            while (!server.isClosed()) {
                Socket clientSocket = server.accept();
                UUID clientId = UUID.randomUUID();
                ClientHandler clientHandler = new ClientHandler(clientSocket, clientId);
                clientHandlerPool.execute(clientHandler);
                clientIdCounter.incrementAndGet();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        } finally {
            clientHandlerPool.shutdown();
        }
    }

    private void broadcastBallsInBatches() {
        while (!ballUpdates.isEmpty()) {
            JSONArray jsonBallsBatch = new JSONArray();
            while (!ballUpdates.isEmpty() && jsonBallsBatch.length() < 10) { // batch size of 10, adjust as necessary
                jsonBallsBatch.put(ballUpdates.poll());
            }
            JSONObject ballsJson = new JSONObject();
            ballsJson.put("type", "ballBatch");
            ballsJson.put("data", jsonBallsBatch);
            broadcastData(ballsJson.toString());
        }
    }

//    private void broadcastBalls() {
//        JSONArray jsonBallsArray = new JSONArray();
//        for (Ball ball : BallManager.balls) { // This must be thread-safe
//            JSONObject jsonBall = new JSONObject();
//            jsonBall.put("x", ball.getX());
//            jsonBall.put("y", ball.getY());
//            jsonBall.put("angle", ball.getAngle());
//            jsonBall.put("velocity", ball.getVelocity());
//            jsonBallsArray.put(jsonBall);
//        }
//        JSONObject ballsJson = new JSONObject();
//        ballsJson.put("type", "ball");
//        ballsJson.put("data", jsonBallsArray);
//        broadcastData(ballsJson.toString());
//    }

    private void broadcastSprites() {
        JSONArray jsonSpritesArray = new JSONArray();
        for (Sprite sprite : SpriteManager.sprites) { // This must be thread-safe
            JSONObject jsonSprite = new JSONObject();
            jsonSprite.put("x", sprite.getX());
            jsonSprite.put("y", sprite.getY());
            jsonSpritesArray.put(jsonSprite);
        }
        JSONObject spritesJson = new JSONObject();
        spritesJson.put("type", "sprite");
        spritesJson.put("data", jsonSpritesArray);
        broadcastData(spritesJson.toString());
    }

    private void broadcastData(String data) {
        for (DataOutputStream out : clientOutputStreams.values()) {
            try {
                out.writeUTF(data);
            } catch (IOException e) {
                System.err.println("Error broadcasting data to client: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket client;
        private final DataInputStream in;
        private final DataOutputStream out;
        private UUID clientId;

        public ClientHandler(Socket clientSocket, UUID clientId) throws IOException {
            this.clientId = clientId;
            this.client = clientSocket;
            this.in = new DataInputStream(client.getInputStream());
            this.out = new DataOutputStream(client.getOutputStream());
            sendClientId();
        }

        private void sendClientId() {
            try {
                // Send the UUID to the client as a string
                out.writeUTF(clientId.toString());
                out.flush();
            } catch (IOException e) {
                System.err.println("Error sending client ID: " + e.getMessage());
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            StringBuilder jsonBuilder = new StringBuilder();
            int recvCtr = 0;
            try {
                while (!client.isClosed()) {
                    try {
                        String line = in.readUTF();
                        System.out.println("Received: " + line);
                        jsonBuilder.append(line).append("\n");

                        int index = jsonBuilder.indexOf("\n}");

                        if (index != -1) {
                            String jsonStr = jsonBuilder.substring(0, index + 1);
                            JSONObject jsonReceived = new JSONObject(jsonStr);
                            processingExecutor.submit(() -> processSpriteData(jsonReceived));
                            jsonBuilder.delete(0, jsonStr.length());
                        }
                    } catch (JSONException e) {

                        // If an exception occurs, it may be due to an incomplete message.
                        // Wait for more data to arrive and attempt to parse it later.
                        // Do not clear the builder so we can append more data to it.
                        System.out.println("Waiting for more data to complete JSON... Current buffer: " + jsonBuilder.toString());
                    }
                }
            } catch (IOException e) {
                System.err.println("ClientHandler exception: " + e.getMessage());
                e.printStackTrace();
                clientOutputStreams.remove(clientId);
            } finally {
                // Close resources and clean up
                try {
                    in.close();
                    out.close();
                    client.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        private void processSpriteData(JSONObject spriteData) {
            // Assuming spriteData is a JSONObject that contains the information needed to update sprites
            // Extract the sprite information and update your server's state

            String spriteId = spriteData.getString("id");
            long mostSig = Long.parseLong(spriteId.substring(0, 16), 16);
            long leastSig = Long.parseLong(spriteId.substring(16, 32), 16);
            UUID uuid = new UUID(mostSig, leastSig);

            float x = spriteData.getFloat("x");
            float y = spriteData.getFloat("y");

            SpriteManager.updateSpritePosition(uuid, x, y);
            System.out.println("processing sprite data");
            // After processing, you might want to broadcast the updated state to all clients
        }
    }

    public void stopServer() {
        try {
            broadcastExecutor.shutdownNow();
            server.close();
        } catch (IOException e) {
            System.err.println("Error closing the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Server server = new Server(4000);
        server.runServer();
    }
}