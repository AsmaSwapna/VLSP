package usr.router;

import usr.net.*;
import java.util.Scanner;
import java.net.*;
import java.nio.channels.*;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A RouterConnections accepts new connections from exisiting routers.
 */
public class RouterConnections implements Runnable {
    // The RouterController 
    RouterController controller;

    // the port this router is listening on
    int port;

    // A Server socket
    ServerSocketChannel channel = null;
    ServerSocket serverSocket = null;

    // The Thread
    Thread myThread;

    // are we running
    boolean running = false;

    /**
     * Construct a RouterConnections, given a specific port.
     */
    public RouterConnections(RouterController cont, int port) {
        controller = cont;
        this.port = port;
    }

    /**
     * Start the connections listening.
     */
    public boolean start() {
	// initialise the socket
        try {
            // WAS: serverSocket = new ServerSocket(port);
            channel = ServerSocketChannel.open();
            //channel.configureBlocking(true);
            serverSocket = channel.socket();
            serverSocket.bind(new InetSocketAddress(port));

            System.out.println(leadin() + "Listening on port: " + port);

            System.out.println(leadin() + "Ready to accept on " + serverSocket);


            myThread = new Thread(this, "RouterConnections" + hashCode());
            running = true;
            myThread.start();

            return true;
        }
	catch (IOException ioe) {
            System.err.println(leadin() + "Cannot listen on port: " + port);
            return false;
        }

    }
    
    /**
     * Stop the listener.
     */
    public boolean stop() {
        try {
            running = false;
            myThread.interrupt();

            return true;

        } catch (Exception e) {
            return false;
        }

    }

    /**
     * The main thread loop.
     */
    public void run() {
        while (running) {
            try {
                TCPEndPointDst dst = new TCPEndPointDst(serverSocket);
                NetIF netIF = new TCPNetIF(dst);
                netIF.connect();

                Socket local = dst.getSocket();

                System.out.println(leadin() + "Did accept on: " + serverSocket);

                System.out.println(leadin() + "newConnection: " + dst.getSocket());

                InetSocketAddress refAddr = new InetSocketAddress(local.getInetAddress(), local.getPort());

                System.out.println("RouterConnections  => " + refAddr + " # " + refAddr.hashCode());


                netIF.setID(refAddr.hashCode());

                System.out.println(leadin() + "netif = " + netIF);

                controller.registerTemporaryNetIF(netIF);

            } catch (IOException ioe) {
                // only print if running, not when stopping
                if (running) {
                    System.err.println(leadin() + "accept failed");
                }
            }

        }
        
    }

    /**
     * Create the String to print out before a message
     */
    String leadin() {
        final String R2R = "R2R: ";

        if (controller == null) {
            return R2R;
        } else {
            return controller.getName() + " " + R2R;
        }

    }


}
