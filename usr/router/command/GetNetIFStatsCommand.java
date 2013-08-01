package usr.router.command;

import usr.protocol.MCRP;
import usr.logging.*;
import usr.router.RouterPort;
import usr.router.NetIF;
import usr.router.NetStats;
import usr.net.Address;
import java.util.List;
import java.util.Map;
import org.simpleframework.http.Response;
import org.simpleframework.http.Request;
import java.io.PrintStream;
import java.io.IOException;
import us.monoid.json.*;

/**
 * The GET_NETIF_STATS command.
 */
public class GetNetIFStatsCommand extends RouterCommand {
    /**
     * Construct a GetNetIFStatsCommand.
     */
    public GetNetIFStatsCommand() {
        super(MCRP.GET_NETIF_STATS.CMD, MCRP.GET_NETIF_STATS.CODE,
              MCRP.ERROR.CODE);
    }

    /**
     * Evaluate the Command.
     */
    public boolean evaluate(Request request, Response response) {
        try {
            PrintStream out = response.getPrintStream();

            JSONObject jsobj = new JSONObject();

            List<RouterPort> ports = controller.listPorts();
            int count = 0;
            NetStats stats = null;
            String statsString;

            // do localnet first
            NetIF localNetIF = controller.getLocalNetIF();

            if (localNetIF != null) {
                stats = localNetIF.getStats();

                // put out netif name
                statsString = localNetIF.getRemoteRouterName() + " "
                    + localNetIF.getName() + " " +
                    stats.toString();

                jsobj.put(Integer.toString(count), statsString);
                count++;
            }

            for (RouterPort rp : ports) {
                if (rp.equals(RouterPort.EMPTY)) {
                    continue;
                } else {
                    NetIF netIF = rp.getNetIF();

                    if (netIF != null) {
                        stats = netIF.getStats();

                        // put out netif name
                        statsString = netIF.getRemoteRouterName()
                            + " " + netIF.getName() + " "
                            + stats.toString();

                        //jsobj.put(netIF.getRemoteRouterName(),
                        // statsString);
                        jsobj.put(Integer.toString(count), statsString);
                        count++;
                    }
                }
            }

            jsobj.put("size", count);
            out.println(jsobj.toString());
            response.close();

            return true;
        } catch (IOException ioe) {
            Logger.getLogger("log").logln(USR.ERROR,
                                          leadin() + ioe.getMessage());
        } catch (JSONException jex) {
            Logger.getLogger("log").logln(USR.ERROR,
                                          leadin() + jex.getMessage());
        }

        finally {
            return false;
        }
    }

}