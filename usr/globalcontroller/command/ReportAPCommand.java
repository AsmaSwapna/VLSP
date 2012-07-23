package usr.globalcontroller.command;

import usr.protocol.MCRP;
import usr.logging.*;
import java.util.Scanner;
import java.net.UnknownHostException;
import org.simpleframework.http.Response;
import org.simpleframework.http.Request;
import java.io.PrintStream;
import java.io.IOException;
import us.monoid.json.*;

/**
 * The REPORT_AP command selets whether a router is or is not
 * an aggregation point
 */
public class ReportAPCommand extends GlobalCommand {
    /**
     * Construct a ReportAPCommand
     */
    public ReportAPCommand() {
        super(MCRP.REPORT_AP.CMD);
    }

    /**
     * Evaluate the Command.
     */
    public boolean evaluate(Request request, Response response) {

        try {
            PrintStream out = response.getPrintStream();

            // get full request string
            String path =  java.net.URLDecoder.decode(request.getPath().getPath(), "UTF-8");
            // strip off /command
            String value = path.substring(9);

            String[] parts = value.split(" ");

            if (parts.length != 3) {
                response.setCode(404);

                JSONObject jsobj = new JSONObject();
                jsobj.put("error", "REPORT_AP command requires GID and AP GID");

                out.println(jsobj.toString());
                response.close();

                return false;
            } else {

                int GID;
                int AP;
                try {
                    GID= Integer.parseInt(parts[1]);
                    AP= Integer.parseInt(parts[2]);
                } catch (Exception e) {
                    response.setCode(404);

                    JSONObject jsobj = new JSONObject();
                    jsobj.put("error", "REPORT_AP command requires GID and AP GID");

                    out.println(jsobj.toString());
                    response.close();

                    return false;
                }

                if (controller.reportAP(GID, AP)) {
                    JSONObject jsobj = new JSONObject();

                    jsobj.put("gid", GID);
                    jsobj.put("ap", AP);
                    jsobj.put("msg", GID+" reports AP "+AP);
                    out.println(jsobj.toString());
                    response.close();

                    return true;

                } else {
                    response.setCode(404);

                    JSONObject jsobj = new JSONObject();
                    jsobj.put("error", "Incorrect GID number "+GID);

                    out.println(jsobj.toString());
                    response.close();

                    return false;
                }

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
