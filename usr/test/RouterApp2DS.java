package usr.test;

import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Scanner;

import usr.logging.Logger;
import usr.logging.USR;
import usr.net.Datagram;
import usr.net.DatagramFactory;
import usr.net.DatagramSocket;
import usr.net.GIDAddress;
import usr.router.Router;

/**
 * Test Router startup and simple DatagramSocket.
 */
public class RouterApp2DS {
    // the router
    Router router = null;

    // the recv socket
    DatagramSocket recvSocket;

    // the send socket
    DatagramSocket sendSocket;

    /**
     * Construct a RouterApp2DS
     */
    public RouterApp2DS() {
        try {
            int port = 18181;
            int r2r = 18182;

            router = new Router(port, r2r, "Router-1");

            // start
            if (router.start()) {
            } else {
                router.stop();
            }

            // set up id
            router.setAddress(new GIDAddress(1));

            // now set up an DatagramSocket to receive
            recvSocket = new DatagramSocket(5555);

            // now set up an DatagramSocket to send
            sendSocket = new DatagramSocket();

            // and we want to connect to address 1 : port 5555
            sendSocket.connect(new GIDAddress(1), 5555);

        } catch (Exception e) {
            Logger.getLogger("log").logln(USR.ERROR, "RouterApp2DS exception: " + e);
            e.printStackTrace();
        }

    }

    /**
     * Write stuff
     */
    void writeALot(int count) {
        Datagram datagram = null;

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }

        for (int i = 0; i < count; i++) {
            String line = "line " + i;
            ByteBuffer buffer = ByteBuffer.allocate(line.length());
            buffer.put(line.getBytes());

            datagram = DatagramFactory.newDatagram(buffer);


            try {
                sendSocket.send(datagram);
                //Logger.getLogger("log").logln(USR.STDOUT, "Sent: " + datagram + " with " + new String(datagram.getPayload()));
            } catch (SocketException se) {
                return;
            }
        }
    }

    void end() {
        router.stop();
    }

    public static void main(String[] args) {
        int count = 10;

        if (args.length == 1) {
            // get no of writes
            Scanner scanner = new Scanner(args[0]);

            count = scanner.nextInt();
        }


        RouterApp2DS app2 = new RouterApp2DS();

        app2.writeALot(count);

        app2.end();
    }

}