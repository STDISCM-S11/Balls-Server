package org.discm.ballsserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private String clientId;
    private Server server;
    private PrintWriter out;

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
        ObjectMapper mapper = new ObjectMapper(); // Jackson's JSON object mapper

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Send client ID back to the client
            out.println(clientId);

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client: " + inputLine); // Print the raw JSON message

                // Parse the JSON message
                Map<String, Object> messageData = mapper.readValue(inputLine, Map.class);

                // Extract data from the JSON map
                String receivedClientId = (String) messageData.get("clientId");
                float x = ((Number) messageData.get("x")).floatValue();
                float y = ((Number) messageData.get("y")).floatValue();

                // Update sprite position using the server method
                server.updateSpritePosition(receivedClientId, x, y);

                // Broadcast the updated position and client ID to other clients
                String broadcastMessage = mapper.writeValueAsString(messageData);
                server.broadcastMessage(clientId, broadcastMessage);
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + clientId);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket for client: " + clientId);
                e.printStackTrace();
            }
            server.clientDisconnected(clientId);
//            System.out.println("disconnecting client");
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }
}