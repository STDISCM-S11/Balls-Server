package org.discm.ballsserver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;




public class Server {
    ServerSocket server = null;
    private Map<Integer, DataOutputStream> clientOutputStreams = new ConcurrentHashMap<>();
    private int id = 0;
    public Server(int port){
        try{
            server = new ServerSocket(port);
            System.out.println("Server started on port: " + port);
//            while (true){
//                Socket client = server.accept();
//                pool.execute(new ClientHandler(client));
//
//            }
        }catch (IOException e){
            System.err.println("Server exception: " + e.getMessage());

        }
    }

    public void runServer(){
        try(ExecutorService pool = Executors.newSingleThreadExecutor()){
            while(true){
                Socket client = server.accept();
                System.out.println(client);
                pool.execute(new ClientHandler(client));
            }
        }catch (IOException e){
            System.err.println("Server exception: " + e.getMessage());
        }



    }
    private class ClientHandler implements Runnable{
        private Socket client;
        public ClientHandler(Socket client){
            this.client = client;
            try{
                clientOutputStreams.put(id, new DataOutputStream(this.client.getOutputStream()));
                id++;
            }catch (IOException e){
                System.err.println("Server exception: " + e.getMessage());
            }


        }

    @Override
    public void run() {
    try {
        PrintWriter out = new PrintWriter(client.getOutputStream(), true);
        
        while (true) {
            // Ball Data Sending
            ArrayList<Ball> balls = new ArrayList<>(BallManager.balls); // Assume BallManager.balls is thread-safe
            final int batchSize = 10; // Or any other appropriate batch size
            int totalBatches = (balls.size() + batchSize - 1) / batchSize;
            
            for (int batch = 0; batch < totalBatches; batch++) {
                int start = batch * batchSize;
                int end = Math.min(start + batchSize, balls.size());
                
                JSONObject jsonToSend = new JSONObject();
                JSONArray jsonBallsArray = new JSONArray();
                
                for (int i = start; i < end; i++) {
                    Ball ball = balls.get(i);
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("x", ball.getX());
                    jsonObject.put("y", ball.getY());
                    jsonObject.put("angle", ball.getAngle());
                    jsonObject.put("velocity", ball.getVelocity());
                    jsonBallsArray.put(jsonObject);
                }
                
                jsonToSend.put("type", "ballBatch");
                jsonToSend.put("batch", batch + 1);
                jsonToSend.put("totalBatches", totalBatches);
                jsonToSend.put("data", jsonBallsArray);
                
                out.println(jsonToSend.toString()); // Send the batch
            }
            
            // Sprite Data Sending
            JSONObject spritesJson = new JSONObject();
            JSONArray jsonSpritesArray = new JSONArray();
            ArrayList<Sprite> sprites = new ArrayList<>(SpriteManager.sprites); // Assume SpriteManager.sprites is thread-safe
            for (Sprite sprite : sprites) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("x", sprite.getX());
                jsonObject.put("y", sprite.getY());
                jsonSpritesArray.put(jsonObject);
            }
            
            spritesJson.put("type", "sprite");
            spritesJson.put("data", jsonSpritesArray);
            
            out.println(spritesJson.toString()); // Send sprite data

            Thread.sleep(10); // Control the update frequency
        }
    } catch (IOException e) {
        System.err.println("Server exception: " + e.getMessage());
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // Restore interrupted status
        System.err.println("Server thread interrupted: " + e.getMessage());
    } finally {
        try {
            client.close(); // Ensure the client socket is properly closed when done
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
    }
}

    }
}
