package usr.localcontroller.command;

import usr.logging.*;
import org.simpleframework.http.Response;
import org.simpleframework.http.Request;
import java.io.PrintStream;
import java.io.IOException;
import us.monoid.json.*;

/**
 * The command to execute if the incoming command is unknown.
 */
public class UnknownCommand extends LocalCommand
{
/**
 * Construct a UnknownCommand
 */
public UnknownCommand(){
    super("__UNKNOWN__");
}

/**
 * Evaluate the Command.
 */
public boolean evaluate(Request request,
    Response response)                        {
    try{
        PrintStream out = response.getPrintStream();

        response.setCode(404);

        JSONObject jsobj = new JSONObject();
        jsobj.put("error", "UnknownCommand");

        out.println(jsobj.toString());
        response.close();
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