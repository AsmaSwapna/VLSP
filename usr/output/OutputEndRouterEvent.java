package usr.output;

import usr.logging.*;
import java.util.*;
import java.io.*;
import usr.globalcontroller.GlobalController;
import usr.globalcontroller.GlobalController;
import usr.events.*;
import us.monoid.json.*;
import usr.common.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/** Class to output network stuff */
public class OutputEndRouterEvent implements OutputFunction
{
/** In fact this only requests output -- actual output occurs later */
public void makeOutput(long t, PrintStream p, OutputType o,
    GlobalController gc)
{
    
}

public void makeEventOutput(Event event, JSONObject result, 
    PrintStream s, OutputType out, GlobalController gc)
{
    //System.err.println("STOP EVENT "+ event.getClass().toString());
    if (!(event instanceof EndRouterEvent)) 
        return;
    int rId;
    try {
        rId= (Integer)result.get("router");
    } catch (JSONException je) {
        return;
    }
    s.println(gc.elapsedToString(gc.getElapsedTime())
        + ANSI.RED + " STOP ROUTER " + rId + ANSI.RESET_COLOUR); 
}


public void parseExtraXML(Node n) throws SAXException
{
}

}
