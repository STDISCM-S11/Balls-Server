package org.discm.ballsserver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
                while (true) {
                    JSONObject jsonToSend = new JSONObject();
                    JSONArray jsonBallsArray = new JSONArray();
                    ArrayList<Ball> balls = BallManager.balls;
                    for (Ball ball : balls) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("x", ball.getX());
                        jsonObject.put("y", ball.getY());
                        jsonObject.put("angle", ball.getAngle());
                        jsonObject.put("velocity", ball.getVelocity());
                        jsonBallsArray.put(jsonObject);
                    }
                    jsonToSend.put("type", "ball");
                    jsonToSend.put("data", jsonBallsArray);

                    JSONArray jsonSpritesArray = new JSONArray();
                    ArrayList<Sprite> sprites = SpriteManager.sprites;
                    for (Sprite sprite : sprites) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("x", sprite.getX());
                        jsonObject.put("y", sprite.getY());
                        jsonSpritesArray.put(jsonObject);
                    }

                    JSONObject spritesJson = new JSONObject();
                    spritesJson.put("type", "sprite");
                    spritesJson.put("data", jsonSpritesArray);

                    for (int i = 0; i < id; i++) {
                        PrintWriter outBalls = new PrintWriter(clientOutputStreams.get(i), true);
                        PrintWriter outSprites = new PrintWriter(clientOutputStreams.get(i), true);
                        outBalls.println(jsonToSend.toString());
                        outSprites.println(spritesJson.toString());
                    }

                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
//            try (DataInputStream in = new DataInputStream(new BufferedInputStream(client.getInputStream()))) {
//                String line;
//                while (!(line = in.readUTF()).equals("Over")) {
////                    processRequest(line, new DataOutputStream(clientSocket.getOutputStream()));
//                }
//            } catch (IOException e) {
//                System.err.println("ClientHandler exception: " + e.getMessage());
//            }
        }
    }
}
