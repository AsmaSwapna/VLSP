package usr.localcontroller.command;

import usr.protocol.MCRP;
import usr.logging.*;
import java.io.IOException;
import usr.common.LocalHostInfo;
import org.simpleframework.http.Response;
import org.simpleframework.http.Request;
import java.io.PrintStream;
import java.io.IOException;
import us.monoid.json.*;

/**
 * The ConnectRouters command.
 */
public class ConnectRoutersCommand extends LocalCommand {
    /**
     * Construct a ConnectRoutersCommand.
     */
    public ConnectRoutersCommand() {
        super(MCRP.CONNECT_ROUTERS.CMD);
    }

    /**
     * Evaluate the Command CONNECT_ROUTERS Router1 Router2 Weight [Name]
     */
    public boolean evaluate(Request request, Response response) {

        try {
            PrintStream out = response.getPrintStream();

            // get full request string
            String path =  java.net.URLDecoder.decode(request.getPath().getPath(), "UTF-8");
            // strip off /command
            String value = path.substring(9);

            String [] args= value.split(" ");

            if (args.length != 4 && args.length !=5) {
                response.setCode(404);

                JSONObject jsobj = new JSONObject();
                jsobj.put("error", "Expected four or five arguments for Connect Routers Command");

                out.println(jsobj.toString());
                response.close();

                return false;

            }

            LocalHostInfo r1= null,r2= null;
            int weight;

            try {
                r1= new LocalHostInfo(args[1]);
                r2= new LocalHostInfo(args[2]);
                weight= Integer.parseInt(args[3]);

            } catch (NumberFormatException nfe) {
                response.setCode(404);

                JSONObject jsobj = new JSONObject();
                jsobj.put("error", "BAD weight for link: "+nfe.getMessage());

                out.println(jsobj.toString());
                response.close();

                return false;

            } catch (Exception e) {
                response.setCode(404);

                JSONObject jsobj = new JSONObject();
                jsobj.put("error", "CANNOT DECODE HOST INFO FOR CONNECT ROUTER COMMAND"+e.getMessage());

                out.println(jsobj.toString());
                response.close();

                return false;
            }

            String name = null;
            if (args.length == 5) {
                // there is a name too
                name = args[4];
            }


            String connectionName = controller.connectRouters(r1,r2, weight, name);

            if (connectionName != null) {
                JSONObject jsobj = new JSONObject();

                jsobj.put("name", connectionName);
                out.println(jsobj.toString());
                response.close();

                return true;

            } else {
                response.setCode(404);

                JSONObject jsobj = new JSONObject();
                jsobj.put("error", "CANNOT CONNECT ROUTERS");

                out.println(jsobj.toString());
                response.close();

                return false;
            }

        } catch (IOException ioe) {
            Logger.getLogger("log").logln(USR.ERROR, leadin() + ioe.getMessage());
        } catch (JSONException jex) {
            Logger.getLogger("log").logln(USR.ERROR, leadin() + jex.getMessage());
        } finally {
            return false;
        }


    }

}
