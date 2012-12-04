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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;


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
NodeLinkPicker linkPicker_= null;
    //Method to pick links

/** Contructor from Parameter string */
public ProbabilisticEventEngine(int time, String parms) throws
    EventEngineException {
    init(time);
    Document doc= parseXMLHead(parms);
    parseXMLMain(doc);
    
}

/** Constructor used from subclasses where XML parse not required*/
protected ProbabilisticEventEngine(int time)
{
    init(time);

}

/** Common elements in all constructors*/
private void init(int time)
{
    timeToEnd_ = time * 1000;
    linkPicker_= new RandomLinkPicker();
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
        if (deathtime <= 0) {
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
    JSONObject response, GlobalController g)                          
{
    if (e instanceof StartRouterEvent)
        followRouter((StartRouterEvent)e, s, response, g);
}

private void followRouter(StartRouterEvent e, EventScheduler s, 
    JSONObject response, GlobalController g)
{
    
    long now= e.getTime();
    int routerId= scheduleDeath(now,s,response,g, this);
    if (routerId >= 0) {
        int nlinks= howManyLinks();
        createNLinks(routerId,now,s,g, nlinks, this);
    }
    scheduleNextRouter(now,s,g, this);
}

 
/** If router created successfully schedule it's death -- return number
 * of router or -1 if no router*/
protected int scheduleDeath(long now, EventScheduler s, 
    JSONObject response, GlobalController g, EventEngine eng)
{
    long time;
    int routerId= 0;
    try {
        if ( (Boolean)response.get("success")) {
            routerId=(Integer)response.get("routerID");
        }
    } catch (JSONException ex) {
        Logger.getLogger("log").logln(USR.ERROR,
            leadin() +
            " Error interpreting response from JSON to router create");
        return -1;
    }
    if (nodeDeathDist_ != null) {
        try {
            time = (long)(nodeDeathDist_.getVariate() * 1000);
        } catch (ProbException x) {
            Logger.getLogger("log").logln(USR.ERROR,
                leadin() + " Error generating nodeDeathDist variate");
            time = 0;
        }
        EndRouterEvent e2 =
            new EndRouterEvent(now + time, eng, 
                new Integer(routerId));
        s.addEvent(e2);
    } 
    return routerId;
}

protected void scheduleNextRouter(long now, 
    EventScheduler s, GlobalController g, EventEngine eng)
{
    long time;
    //  Schedule new node
    try {
        time = (long)(nodeCreateDist_.getVariate() * 1000);
    } catch (ProbException x) {
        Logger.getLogger("log").logln(USR.ERROR,
            leadin() + " Error generating trafficArriveDist variate");
        time = 0;
    }
    StartRouterEvent ev = new StartRouterEvent(now + time, eng);
    s.addEvent(ev);
}

protected int howManyLinks()
{
    int noLinks = 1;
    try {
        noLinks = linkCreateDist_.getIntVariate();
    } catch (ProbException x) {
        Logger.getLogger("log").logln(USR.ERROR,
            leadin() + " Error generating linkCreateDist variate");
    }  
    return noLinks;
}

private void createNLinks(int routerId, long now, 
    EventScheduler s, GlobalController g, int noLinks, EventEngine eng)
{
    
    // Schedule links

    ArrayList <Integer> picked= chooseNLinks(routerId, g,noLinks);
    for (int i: picked) {
        StartLinkEvent e = new StartLinkEvent(now, eng, i,
            routerId);
        s.addEvent(e);
    }
}


/** Choose n links to be connected to */
private ArrayList <Integer> chooseNLinks(int routerId,
    GlobalController g, int noLinks)   
{
    // Get a list of nodes which can be connected to
    ArrayList <Integer>nodes = new ArrayList<Integer>(g.getRouterList());
    nodes.remove(nodes.indexOf(routerId));
    int [] outlinks = g.getOutLinks(routerId);
    for (Integer l : outlinks)
        nodes.remove(nodes.indexOf(l));
    ArrayList <Integer> picked= linkPicker_.pickNLinks(nodes, g, noLinks);
    
    return picked;
}

/** Parse the XML to get probability distribution information*/
protected Document parseXMLHead(String fName) throws EventEngineException {
    Document doc;
    try {
        DocumentBuilderFactory docBuilderFactory =
            DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder =
            docBuilderFactory.newDocumentBuilder();
        doc = docBuilder.parse(new File(fName));

        // normalize text representation
        doc.getDocumentElement().normalize();
        String basenode = doc.getDocumentElement().getNodeName();
        if (!basenode.equals("ProbabilisticEngine")) throw new
                  SAXException(
                "Base tag should be ProbabilisticEngine");
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
    return doc; 
}

protected void parseXMLMain(Document doc) 
throws EventEngineException
{
    try 
    {
        NodeList nbd = doc.getElementsByTagName("NodeBirthDist");
        if (nbd.getLength() != 1) {
            throw new EventEngineException("ProbabilisticEventEngine "+
                "XML must specify exactly one NodeBirthDist");
        }
        nodeCreateDist_ = ProbDistribution.parseProbDist
            (nbd,"NodeBirthDist");
        ReadXMLUtils.removeNode(nbd.item(0).getParentNode(),
            "NodeBirthDist","ProbabilisticEngine");
        if (nodeCreateDist_ == null) throw new SAXException(
                "Must specific NodeBirthDist");
        NodeList lcd = doc.getElementsByTagName("LinkCreateDist");
        linkCreateDist_ = ProbDistribution.parseProbDist(
            lcd, "LinkCreateDist");
        if (linkCreateDist_ == null) throw new SAXException(
                "Must specific LinkCreateDist");
        ReadXMLUtils.removeNode(lcd.item(0).getParentNode(),
            "LinkCreateDist","ProbabilisticEngine");
        NodeList ndd = doc.getElementsByTagName("NodeDeathDist");
        nodeDeathDist_ = ProbDistribution.parseProbDist(ndd,
            "NodeDeathDist");
        ReadXMLUtils.removeNode(ndd.item(0).getParentNode(),
            "NodeDeathDist","ProbabilisticEngine");
        NodeList ldd = doc.getElementsByTagName("LinkDeathDist");
        linkDeathDist_ = ProbDistribution.parseProbDist(ldd,
            "LinkDeathDist");
        try {
            NodeList misc = doc.getElementsByTagName("Parameters");
            if (misc.getLength() > 0) throw new SAXException(
                "Parameters tag deprecated, replace with LinkPicker.");
        } catch (SAXException e) { 
            throw new EventEngineException("Error parsing SAX in "+
                "ProbabilisticEventEngine "+e.getMessage());
        } 
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
    parseLinkPicker(doc);
    
    Element el = doc.getDocumentElement();
    NodeList rest = el.getChildNodes();
    for (int i = 0; i < rest.getLength(); i++) {
        Node n = rest.item(i);
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            throw new EventEngineException(
                "Unrecognised tag constructing "+
                "ProbabilisticEventEngine "+n.getNodeName());
            
        }
    }
    
}


private void parseLinkPicker(Document doc) throws EventEngineException
{
    NodeList lpns= doc.getElementsByTagName("ProbabilisticEngine");
    Node lpn= lpns.item(0);
    String linkpick;
    try {
        linkpick= ReadXMLUtils.parseSingleString(lpn,
            "LinkPicker","ProbabilisicEngine",true);
        ReadXMLUtils.removeNode(lpn, "LinkPicker","ProbabilisicEngine");
    } catch (SAXException e) {
      throw new EventEngineException ("Error parsing XML in ProbMLEE "+
            e.getMessage());
    } catch (XMLNoTagException e) {
        return;
    }
    Class <?> lpclass = null;
    
    try {
        lpclass = Class.forName(linkpick);
    } catch (ClassNotFoundException e) { 
        throw new EventEngineException(
        "Could not find linkPicker class "+linkpick+" "+e.getMessage());
    }
    try {
        Class[] args = new Class[0];
        Constructor <?> c = lpclass.getConstructor(args);
        Object[] arglist = new Object[0];
        linkPicker_ = (NodeLinkPicker)c.newInstance(arglist);
    } catch (InvocationTargetException e) {
        throw new EventEngineException(
            "Could not construct linkpicker " +linkpick+
            "\n Error message:" +
            e.getMessage());
    } catch (InstantiationException e) {
        throw new EventEngineException(
            "Could not instantiate linkpicker " +linkpick +
            "\n Error message:" +
            e.getMessage());
    } catch (NoSuchMethodException e) {
        throw new EventEngineException(
            "Could not find constructor for linkpicker " +linkpick+
            "\n Error message:" +
            e.getMessage());
    } catch (IllegalAccessException e) {
        throw new EventEngineException(
            "Could not instantiate object for linkpicker " + linkpick +
            "\n Error message:" +
            e.getMessage());
    }

}

/**
 * Header for errors
 */
private String leadin(){
    return new String("ProbabilisticTrafficEngine:");
}
}
