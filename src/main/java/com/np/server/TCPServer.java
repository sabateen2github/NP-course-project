package com.np.server;

import com.np.server.auth.SessionServer;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

class serveClient extends Thread {
    Socket mySocket;

    public serveClient(Socket s) {
        mySocket = s;
    }

    public void run() {
        try {

            RequestsHandler.handleRequest(mySocket);

            // Close stream
            mySocket.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

public class TCPServer {

    public static final int SERVICE_PORT = 2500;

    public static Map<String, SessionServer> sessionMap = Collections.synchronizedMap(new HashMap<>());

    public static void main(String args[]) {
        try {
            ServerSocket server = new ServerSocket(SERVICE_PORT);
            System.out.println("TCP service started");
            // Loop indefinitely, accepting clients
            for (; ; ) {
                // Get the next TCP client
                Socket nextClient = server.accept();
                // Display connection details
                System.out.println("Received request from " + nextClient.getInetAddress() + ":" + nextClient.getPort());
                //Create a new thread and pass the created socket to it
                serveClient nextThread = new serveClient(nextClient);
                //launch the thread
                nextThread.start();
            }
        } catch (BindException be) {
            System.err.println("Service already running on port " + SERVICE_PORT);
        } catch (IOException ioe) {
            System.err.println("I/O error - " + ioe);
        }
    }
}
