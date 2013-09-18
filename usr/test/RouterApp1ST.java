package usr.test;

import usr.logging.Logger;
import usr.logging.USR;
import usr.net.Datagram;
import usr.net.IPV4Address;
import usr.router.AppSocket;
import usr.router.Router;

/**
 * Test Router startup and simple AppSocket.
 * Uses separate Thread for app.
 */
public class RouterApp1ST {
    // the Router
    Router router = null;

    public RouterApp1ST() {
        try {
            int port = 18191;
            int r2r = 18192;

            router = new Router(port, r2r, "Router-2");

            // set ID
            router.setAddress(new IPV4Address("192.168.7.2")); // WAS new GIDAddress(2));

            // start
            if (router.start()) {
            } else {
                router.stop();
            }
        } catch (Exception e) {
            Logger.getLogger("log").logln(USR.ERROR, "RouterApp1S exception: " + e);
            e.printStackTrace();
        }

    }

    public void execute(Thread t) {
        t.start();
    }

    public static void main(String[] args) {
        RouterApp1ST rapp1s = new RouterApp1ST();

        App1S app1s = new App1S(rapp1s.router);

        rapp1s.execute(app1s);
    }

}

class App1S extends Thread {
    Router router;

    // the socket
    AppSocket socket;

    int count = 0;

    public App1S(Router r) {
        router = r;
    }

    public void run() {
        try {
            // now set up an AppSocket to receive
            // the socket
            socket = new AppSocket(router, 3000);

            Datagram datagram;

            while ((datagram = socket.receive()) != null) {

                System.out.print(count + ". ");
                System.out.print("HL: " + datagram.getHeaderLength() +
                                 " TL: " + datagram.getTotalLength() +
                                 " From: " + datagram.getSrcAddress() +
                                 " To: " + datagram.getDstAddress() +
                                 ". ");
                byte[] payload = datagram.getPayload();

                if (payload == null) {
                    System.out.print("No payload");
                } else {
                    System.out.print(new String(payload));
                }
                System.out.print("\n");

                count++;
            }

        } catch (Exception e) {
            Logger.getLogger("log").logln(USR.ERROR, "App1S exception: " + e);
            e.printStackTrace();
        }


    }

}