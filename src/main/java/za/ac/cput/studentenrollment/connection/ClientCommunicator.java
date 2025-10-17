/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package za.ac.cput.studentenrollment.connection;

import java.io.*;
import java.net.Socket;

/**
 *
 * @author elzas
 */

public class ClientCommunicator {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 6666;
    
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            return true;
        } catch (IOException e) {
            System.out.println("Cannot connect to server: " + e.getMessage());
            return false;
        }
    }

    public Response sendRequest(Request request) {
        try {
            output.writeObject(request);
            output.flush();
            return (Response) input.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error sending request: " + e.getMessage());
            return new Response(ResponseStatus.ERROR, "Communication error: " + e.getMessage());
        }
    }

    public void disconnect() {
        try {
            if (output != null) output.close();
            if (input != null) input.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.out.println("Error disconnecting: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }
}