package usr.test;

import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

import usr.logging.Logger;
import usr.logging.USR;
import usr.net.IPV4Address;
import usr.router.AppSocket;
import usr.router.Router;

/**
 * Test Router startup and simple AppSocket.
 */
public class RouterApp1SD {
    // the Router
    Router router = null;

    // the socket
    AppSocket socket;

    // Timer stuff
    Timer timer;
    TimerTask timerTask;

    // total no of Datagrams in
    int count = 0;
    // last time count
    int lastTimeCount = 0;
    // no per second
    int diffs = 0;

    public RouterApp1SD() {
        try {
            int port = 18191;
            int r2r = 18192;

            router = new Router(port, r2r, "Router-2");

            // start
            if (router.start()) {
            } else {
                router.stop();
            }

            // set ID
            router.setAddress(new IPV4Address("192.168.7.2")); // WAS new GIDAddress(2));

            // now set up an AppSocket to receive
            socket = new AppSocket(router, 3000);

            // set up timer to count throughput
            timerTask = new TimerTask() {
                boolean running = true;

                @Override
				public void run() {
                    if (running) {
                        diffs = count - lastTimeCount;
                        Logger.getLogger("log").logln(USR.STDOUT, "Task count: " + count + " diff: "  + diffs);
                        lastTimeCount = count;
                    }
                }

                @Override
				public boolean cancel() {
                    Logger.getLogger("log").log(USR.STDOUT, "cancel @ " + count);

                    if (running) {
                        running = false;
                    }


                    return running;
                }

                @Override
				public long scheduledExecutionTime() {
                    Logger.getLogger("log").log(USR.STDOUT, "scheduledExecutionTime:");
                    return 0;
                }

            };

            // if there is no timer, start one
            if (timer == null) {
                timer = new Timer();
                timer.schedule(timerTask, 1000, 1000);
            }



        } catch (Exception e) {
            Logger.getLogger("log").logln(USR.ERROR, "RouterApp1S exception: " + e);
            e.printStackTrace();
        }


    }

    /**
     * Read stuff
     */
    void readALot() {
        try {
            while ((socket.receive()) != null) {
                count++;
            }
        } catch (SocketException se) {
            Logger.getLogger("log").logln(USR.ERROR, se.getMessage());
        }

    }

    public static void main(String[] args) {
        RouterApp1SD app1sd = new RouterApp1SD();

        app1sd.readALot();
    }

}