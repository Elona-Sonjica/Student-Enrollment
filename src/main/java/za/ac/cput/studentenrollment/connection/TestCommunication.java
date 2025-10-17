/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.connection;

/**
 *
 * @author elzas
 */

public class TestCommunication {
    public static void main(String[] args) {
        System.out.println("=== Simple Client-Server Test ===");
        
        // Start server in background thread
        Thread serverThread = new Thread(() -> {
            Server server = new Server();
            server.start();
        });
        serverThread.setDaemon(true);
        serverThread.start();
        
        // Wait for server to start
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Test client
        ClientCommunicator client = new ClientCommunicator();
        
        if (client.connect()) {
            System.out.println("Client connected to server");
            
            // Test getting courses
            Request request = new Request(RequestType.GET_ALL_COURSES);
            Response response = client.sendRequest(request);
            
            if (response.isSuccess()) {
                System.out.println("Got courses from server successfully");
            } else {
                System.out.println("Failed: " + response.getMessage());
            }
            
            client.disconnect();
        }
    }
}