/** Interface for Engine which adds events to the event list
 */

package usr.engine;

import usr.globalcontroller.*;
import rgc.xmlparse.*;
import rgc.probdistributions.*;
import usr.logging.*;
import usr.common.Pair;
import usr.APcontroller.*;
import usr.events.*;
import us.monoid.json.*;
import org.w3c.dom.Document;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import java.io.*;
import java.util.*;

/**
 * This engine uses probability distribtions to add events into the
 * event library
 */
public class ProbabilisticEventEngine  extends NullEventEngine
{
int timeToEnd_; 
    // Time to end of simulation (ms)
ProbDistribution nodeCreateDist_ = null;                
    //  Distribution for creating nodes
ProbDistribution nodeDeathDist_ = null;                 
    // Distribution of node lifetimes
ProbDistribution linkCreateDist_ = null;                
    // Distribution for number of links created
ProbDistribution linkDeathDist_ = null;                 
    // Distribution for link  lifetimes
private boolean preferentialAttachment_ = false;        
    // If true links are chosen using P.A.

/** Contructor from Parameter string */
public ProbabilisticEventEngine(int time, String parms) throws
    EventEngineException {
    timeToEnd_ = time * 1000;
    parseXMLFile(parms);
}

/** Start up and shut down events */
public void startStopEvents(EventScheduler s, GlobalController g)                      
{
    // simulation start
    StartSimulationEvent e0 = new StartSimulationEvent(0, this);

    s.addEvent(e0);

    // simulation end
    EndSimulationEvent e = new EndSimulationEvent(timeToEnd_, this);
    s.addEvent(e);
}
/** Initial events to add to schedule */
public void initialEvents(EventScheduler s, GlobalController g)                        
{
    // Start initial router
    long time;

    //  Schedule new node
    try {
        time = (long)(nodeCreateDist_.getVariate() * 1000);
    } catch (ProbException x) {
        Logger.getLogger("log").logln(USR.ERROR,
            leadin() +
            " Error generating trafficArriveDist variate");
        time = 0;
    }
    //Logger.getLogger("log").logln(USR.ERROR, "Time to next router
    // "+time);
    StartRouterEvent e1 = new StartRouterEvent(time, this);
    s.addEvent(e1);
}

/** 
 * Add nodes simply to prime AP controllers lifetime estimation
 */
public void warmUp (long period, APController controller, 
    GlobalController gc)
{
    long tmptime= -period;
    if (nodeDeathDist_ == null || nodeCreateDist_ == null) {
        Logger.getLogger("log").logln(USR.ERROR,"WarmUpPeriod option "+
            "does not make sense without node death and create dists");
            return;
    }
    long deathtime;
    while(true) {
        try {
            long diff= (long)nodeCreateDist_.getVariate()*1000;
            if (diff == 0) 
                diff= 1000;
            tmptime+= diff;
            if (tmptime >= 0) {
                return;
            }
            deathtime= tmptime+ (long)nodeDeathDist_.getVariate()*1000;
        } catch (ProbException e) {
            Logger.getLogger("log").logln(USR.ERROR,
                "Exception thrown in ProbabilisticEventEngine.warmUp "+
                e.getMessage());
            return;
        }
        controller.addWarmUpNode(tmptime);
        if (deathtime >= 0) {
            controller.removeWarmUpNode(tmptime,deathtime);
        } else {
            EndWarmupRouterEvent e= new EndWarmupRouterEvent(tmptime,
                deathtime, this);
            gc.addEvent(e);
        }
        
    }    
}

/** Add or remove events following a simulation event */
public void followEvent(Event e, EventScheduler s,
    JSONObject response, GlobalController g)                          {
    if (e instanceof StartRouterEvent)
        followRouter((StartRouterEvent)e, s, response, g);
}

private void followRouter(StartRouterEvent e, EventScheduler s, 
    JSONObject response, GlobalController g)
{
    long now = e.getTime();
    long time;
    int routerId= 0;
    try {
        routerId=(Integer)response.get("routerID");
    } catch (JSONException ex) {
        Logger.getLogger("log").logln(USR.ERROR,
            leadin() +
            " Error interpreting response from JSON to router create");
        return;
    }
    //  Schedule new node
    try {
        time = (long)(nodeCreateDist_.getVariate() * 1000);
    }catch (ProbException x) {
        Logger.getLogger("log").logln(USR.ERROR,
            leadin() +
            " Error generating trafficArriveDist variate");
        time = 0;
    }
    StartRouterEvent e1 = new StartRouterEvent(now + time, this);
    s.addEvent(e1);
    // Schedule node death if this will happen
    if (nodeDeathDist_ != null) {
        try {
            time = (long)(nodeDeathDist_.getVariate() * 1000);
        } catch (ProbException x) {
            Logger.getLogger("log").logln(USR.ERROR,
                leadin() +
                " Error generating nodeDeathDist variate");
            time = 0;
        }

        EndRouterEvent e2 =
            new EndRouterEvent(now + time, this, new Integer(
                    routerId));
        s.addEvent(e2);
    } else {
        System.err.println("Death dist null");
    }
    // Schedule links
    int noLinks = 1;
    try {
        noLinks = linkCreateDist_.getIntVariate();
    } catch (ProbException x) {
        Logger.getLogger("log").logln(USR.ERROR,
            leadin() +
            " Error generating linkCreateDist variate");
    }
    ArrayList <Integer>nodes = new ArrayList<Integer>(
        g.getRouterList());
    //System.err.println("Router list "+nodes);
    nodes.remove(nodes.indexOf(routerId));
    int [] outlinks = g.getOutLinks(routerId);
    for (Integer l : outlinks)
        nodes.remove(nodes.indexOf(l));
    //Logger.getLogger("log").logln(USR.ERROR, "Trying to pick
    // "+noLinks+" links");
    for (int i = 0; i < noLinks; i++) {
        if (nodes.size() <= 0)
            break;
            
        // Choose a node using pref. attach.
        if (preferentialAttachment_) { 
            int totLinks = 0;
            for (int l : nodes)
                totLinks += g.getOutLinks(l).length;
            int index = (int)Math.floor(Math.random() * totLinks);
            for (int j = 0; j < nodes.size(); j++) {
                int l = nodes.get(j);
                index -= g.getOutLinks(l).length;
                if (index < 0 || j == nodes.size() - 1) {
                    nodes.remove(j);
                    StartLinkEvent e3 =
                        new StartLinkEvent(now, this, l,
                            routerId);
                    s.addEvent(e3);
                    break;
                }
            }
        } else {                //Logger.getLogger("log").logln(USR.ERROR,
                                // "Choice
            // set "+nodes);
            int index = (int)Math.floor(Math.random() * nodes.size());
            int newLink = nodes.get(index);
            //Logger.getLogger("log").logln(USR.ERROR, "Picked
            // "+newLink);
            nodes.remove(index);
            StartLinkEvent e4 = new StartLinkEvent(now, this, newLink,
                routerId);
            s.addEvent(e4);
        }
    }
}

/** Parse the XML to get probability distribution information*/
private void parseXMLFile(String fName) throws EventEngineException {
    try {
        DocumentBuilderFactory docBuilderFactory =
            DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder =
            docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(new File(fName));

        // normalize text representation
        doc.getDocumentElement().normalize();
        String basenode = doc.getDocumentElement().getNodeName();
        if (!basenode.equals("ProbabilisticEngine")) throw new
                  SAXException(
                "Base tag should be ProbabilisticEngine");
        NodeList nbd = doc.getElementsByTagName("NodeBirthDist");
        nodeCreateDist_ = ProbDistribution.parseProbDist(nbd,
            "NodeBirthDist");
        if (nodeCreateDist_ == null) throw new SAXException(
                "Must specific NodeBirthDist");
        NodeList lcd = doc.getElementsByTagName("LinkCreateDist");
        linkCreateDist_ = ProbDistribution.parseProbDist(
            lcd, "LinkCreateDist");
        if (linkCreateDist_ == null) throw new SAXException(
                "Must specific LinkCreateDist");
        NodeList ndd = doc.getElementsByTagName("NodeDeathDist");
        nodeDeathDist_ = ProbDistribution.parseProbDist(ndd,
            "NodeDeathDist");
        NodeList ldd = doc.getElementsByTagName("LinkDeathDist");

        linkDeathDist_ = ProbDistribution.parseProbDist(ldd,
            "LinkDeathDist");
        try {
            NodeList misc = doc.getElementsByTagName("Parameters");
            if (misc.getLength() > 1) throw new SAXException(
                    "Only one GlobalController tag allowed.");
            if (misc.getLength() == 1) {
                Node miscnode = misc.item(0);
                preferentialAttachment_ =
                    ReadXMLUtils.parseSingleBool(
                        miscnode,
                        "PreferentialAttachment",
                        "Parameters", true);
                ReadXMLUtils.removeNode(miscnode,
                    "PreferentialAttachment",
                    "Parameters");
            }
        } catch (SAXException e) { throw e;
        } catch (XMLNoTagException e) {
        }
    } catch (java.io.FileNotFoundException e) { 
        throw new EventEngineException(
        "Parsing ProbabilisticEventEngine: Cannot find file "+ fName);
    } catch (SAXParseException err) { 
        throw new EventEngineException(
            "Parsing ProbabilisticEventEngine: error" + ", line "+ 
            err.getLineNumber() + ", uri " + err.getSystemId());
    }catch (SAXException e) { 
        throw new EventEngineException(
        "Parsing ProbabilisticEventEngine: Exception in SAX XML parser"
        + e.getMessage());
    }catch (Throwable t) { 
        throw new EventEngineException(
           "Parsing ProbabilisticEventEngine: " + t.getMessage());
    }
}

/**
 * Header for errors
 */
private String leadin(){
    return new String("ProbabilisticTrafficEngine:");
}
}
