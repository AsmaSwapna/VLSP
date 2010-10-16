/** Interface for Engine which adds events to the event list
*/

package usr.engine;

import usr.globalcontroller.*;
import usr.common.*;
import usr.logging.*;

import org.w3c.dom.Document;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 
import java.io.*;
import java.util.*;

/**
This engine uses probability distribtions to add events into the 
event library
*/
public class ProbabilisticEventEngine implements EventEngine {
    int timeToEnd_;
    ProbDistribution nodeCreateDist_= null;
    ProbDistribution nodeDeathDist_= null;
    ProbDistribution linkCreateDist_= null;
    ProbDistribution linkDeathDist_= null;
    

    /** Contructor from Parameter string */
    public ProbabilisticEventEngine(int time, String parms) 
    {
        timeToEnd_= time*1000;
        parseXMLFile(parms);
    }
    
    /** Initial events to add to schedule */
    public void initialEvents(EventScheduler s, GlobalController g)
    {
        // simulation start
        SimEvent e;
        e = new SimEvent(SimEvent.EVENT_START_SIMULATION, 0, null);
        s.addEvent(e);
        // Add first node
        long time= (int)(nodeCreateDist_.getVariate()*1000);
        e = new SimEvent(SimEvent.EVENT_START_ROUTER, time, null);
        s.addEvent(e);
        // simulation end
        e= new SimEvent(SimEvent.EVENT_END_SIMULATION, timeToEnd_, null);
        s.addEvent(e);

    }
    
    /** Add or remove events following a simulation event */
    public void preceedEvent(SimEvent e, EventScheduler s,  GlobalController g) 
    {

    }
    
    /** Add or remove events following a simulation event */
    public void followEvent(SimEvent e, EventScheduler s,  GlobalController g,
      Object o)
    {
        if (e.getType() == SimEvent.EVENT_START_ROUTER) {
            followRouter(e, s, g);
            return;
        }
    }
    
    private void followRouter(SimEvent e, EventScheduler s,  
        GlobalController g) {
        int routerId;
        routerId= g.getMaxRouterId();
        long now= e.getTime();
        SimEvent e1= null;
        long time;
        // Schedule node death if this will happen
        if (nodeDeathDist_ != null) {
            time= (long)(nodeDeathDist_.getVariate()*1000);
            e1= new SimEvent(SimEvent.EVENT_END_ROUTER, now+time, new Integer(routerId));
            s.addEvent(e1);
        }
        //  Schedule new node
        time= (long)(nodeCreateDist_.getVariate()*1000);
        //Logger.getLogger("log").logln(USR.ERROR, "Time to next router "+time);
        e1= new SimEvent(SimEvent.EVENT_START_ROUTER, now+time, null);
        s.addEvent(e1);
        // Schedule links
        int noLinks= linkCreateDist_.getIntVariate();
        ArrayList <Integer>nodes= new ArrayList<Integer>(g.getRouterList());
        nodes.remove(nodes.indexOf(routerId));
        List <Integer>outlinks= (List<Integer>)g.getOutLinks(routerId);
        if (outlinks != null) {
          for (Integer l: outlinks) {
              nodes.remove(nodes.indexOf(l));
          }
        }
        //Logger.getLogger("log").logln(USR.ERROR, "Trying to pick "+noLinks+" links");
        for (int i= 0; i < noLinks; i++) {
            if (nodes.size() <= 0) {
                break;
            }
            //Logger.getLogger("log").logln(USR.ERROR, "Choice set "+nodes);
            int index= (int)Math.floor( Math.random()*nodes.size());
            int newLink= nodes.get(index);
            //Logger.getLogger("log").logln(USR.ERROR, "Picked "+newLink);
            nodes.remove(index);
            e1= new SimEvent(SimEvent.EVENT_START_LINK,now, 
                new Pair<Integer,Integer>(newLink,routerId));
            s.addEvent(e1);
        }
    }
    
    /** Parse the XML to get probability distribution information*/
    private void parseXMLFile(String fName) 
    {
        try { DocumentBuilderFactory docBuilderFactory = 
          DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document doc = docBuilder.parse (new File(fName));

        // normalize text representation
        doc.getDocumentElement ().normalize ();
        String basenode= doc.getDocumentElement().getNodeName();
        if (!basenode.equals("ProbabilisticEngine")) {
            throw new SAXException("Base tag should be ProbabilisticEngine");
        }
        NodeList nbd= doc.getElementsByTagName("NodeBirthDist");
        nodeCreateDist_= ReadXMLUtils.parseProbDist(nbd,"NodeBirthDist");
        if (nodeCreateDist_ == null) {
            throw new SAXException ("Must specific NodeBirthDist");
        }
        NodeList lcd= doc.getElementsByTagName("LinkCreateDist");
        linkCreateDist_= ReadXMLUtils.parseProbDist(lcd,"LinkCreateDist");
        if (linkCreateDist_ == null) {
            throw new SAXException ("Must specific LinkCreateDist");
        }
        NodeList ndd= doc.getElementsByTagName("NodeDeathDist");
        nodeDeathDist_= ReadXMLUtils.parseProbDist(ndd,"NodeDeathDist");
        NodeList ldd= doc.getElementsByTagName("LinkDeathDist");
        linkDeathDist_= ReadXMLUtils.parseProbDist(ldd,"LinkDeathDist");
        
          
    } catch (java.io.FileNotFoundException e) {
          Logger.getLogger("log").logln(USR.ERROR, "Cannot find file "+fName);
          System.exit(-1);
      }catch (SAXParseException err) {
          System.err.println ("** Parsing error" + ", line " 
             + err.getLineNumber () + ", uri " + err.getSystemId ());
          Logger.getLogger("log").logln(USR.ERROR, " " + err.getMessage ());
          System.exit(-1);

      }catch (SAXException e) {
          Logger.getLogger("log").logln(USR.ERROR, "Exception in SAX XML parser.");
          Logger.getLogger("log").logln(USR.ERROR, e.getMessage());
          System.exit(-1);
          
      }catch (Throwable t) {
          t.printStackTrace ();
          System.exit(-1);
      }
    }
    
}
