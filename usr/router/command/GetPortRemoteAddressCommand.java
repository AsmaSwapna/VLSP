package usr.router.command;

import usr.protocol.MCRP;
import usr.logging.*;
import org.simpleframework.http.Response;
import org.simpleframework.http.Request;
import java.io.PrintStream;
import java.io.IOException;
import us.monoid.json.*;
import usr.router.RouterPort;
import usr.router.NetIF;
import usr.net.*;
import java.util.Scanner;

/**
 * The GET_PORT_REMOTE_ADDRESS command.
 * GET_PORT_REMOTE_ADDRESS port
 * GET_PORT_REMOTE_ADDRESS port0
 */
public class GetPortRemoteAddressCommand extends RouterCommand {
    /**
     * Construct a GetPortRemoteAddressCommand
     */
    public GetPortRemoteAddressCommand() {
        super(MCRP.GET_PORT_REMOTE_ADDRESS.CMD,
              MCRP.GET_PORT_REMOTE_ADDRESS.CODE,
              MCRP.GET_PORT_REMOTE_ADDRESS.ERROR);
    }

    /**
     * Evaluate the Command.
     */
    public boolean evaluate(Request request, Response response) {
        try {
            PrintStream out = response.getPrintStream();

            // get full request string
            String path = java.net.URLDecoder.decode(
                    request.getPath().getPath(), "UTF-8");

            // strip off /command
            String value = path.substring(9);

            // strip off COMMAND
            String rest = value.substring(
                    MCRP.GET_PORT_REMOTE_ADDRESS.CMD.length()).trim();
            String[] parts = rest.split(" ");

            if (parts.length == 1) {
                String routerPortName = parts[0];

                // find port
                String portNo;

                if (routerPortName.startsWith("port")) {
                    portNo = routerPortName.substring(4);
                } else {
                    portNo = routerPortName;
                }

                Scanner scanner = new Scanner(portNo);
                int p = scanner.nextInt();
                RouterPort routerPort = controller.getPort(p);

                if ((routerPort == null) || (routerPort ==
                                             RouterPort.EMPTY)) {
                    response.setCode(404);

                    JSONObject jsobj = new JSONObject();
                    jsobj.put("error",
                              " invalid port " + routerPortName);

                    out.println(jsobj.toString());
                    response.close();

                    return false;
                } else {
                    // get name on netIF in port
                    NetIF netIF = routerPort.getNetIF();
                    Address address = netIF.getRemoteRouterAddress();

                    JSONObject jsobj = new JSONObject();

                    if (address != null) {
                        jsobj.put("address", address.toString());
                        out.println(jsobj.toString());
                    } else {
                        jsobj.put("address", "");
                        out.println(jsobj.toString());
                    }

                    response.close();

                    return true;
                }
            } else {
                response.setCode(404);

                JSONObject jsobj = new JSONObject();
                jsobj.put("error", getName() + " wrong no of args ");

                out.println(jsobj.toString());
                response.close();

                return false;
            }
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