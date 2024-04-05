package org.discm.ballsserver;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String clientId;
    private Server server;
    private BufferedOutputStream out;

    public ClientHandler(Socket socket, String clientId, Server server) {
        this.clientSocket = socket;
        this.clientId = clientId;
        this.server = server;
    }

    public String getClientId() {
        return clientId;
    }

    @Override
    public void run() {
        sendAndReceiveSprites();
    }

    private void sendAndReceiveSprites(){
        ObjectMapper mapper = new ObjectMapper(); // Jackson's JSON object mapper

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedOutputStream(clientSocket.getOutputStream());
            PrintWriter printWriter = new PrintWriter(clientSocket.getOutputStream(), true);

            // Send client ID back to the client
            printWriter.println(clientId);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                Map<String, Object> messageData = mapper.readValue(inputLine, Map.class);
                String receivedClientId = (String) messageData.get("clientId");
                float x = ((Number) messageData.get("x")).floatValue();
                float y = ((Number) messageData.get("y")).floatValue();


                // Update sprite position using the server method
                server.updateSpritePosition(receivedClientId, x, y);
                String broadcastMessage = mapper.writeValueAsString(messageData);
                server.broadcastMessage(clientId);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + clientId);
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing output stream for client: " + clientId);
            e.printStackTrace();
        }

        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket for client: " + clientId);
            e.printStackTrace();
        }

        // Inform the server that this client has disconnected
        server.clientDisconnected(clientId);
    }


    public void sendMessage(String message) {
        try{
            byte[] jsonData = message.getBytes();
//            System.out.println("len: "+ jsonData.length + " message: " + message);
            out.write(jsonData);
            out.flush();
        } catch(IOException e){
            e.printStackTrace();
        }

    }
}