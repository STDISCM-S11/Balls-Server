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
//                PrintWriter out = new PrintWriter(client.getOutputStream(), true);
//                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                while (true) {
                    JSONArray jsonArray = new JSONArray();
                    ArrayList<Ball> balls = BallManager.balls;
//                    if(balls.isEmpty()){
//                        continue;
//                    }

                    for (Ball ball : balls) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("x", ball.getX());
                        jsonObject.put("y", ball.getY());
                        jsonObject.put("angle", ball.getAngle());
                        jsonObject.put("velocity", ball.getVelocity());
                        jsonArray.put(jsonObject);
//                        System.out.println(ball.getX());
//                        System.out.println(ball.getY());
                    }

                    for(int i = 0; i < id; i++){
                        // Send JSON string to client
                        PrintWriter out = new PrintWriter(clientOutputStreams.get(i), true);
                        out.println(jsonArray.toString());
                    }



                    // Wait for some time before sending again
                    Thread.sleep(10); // Adjust the delay according to your requirements
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
