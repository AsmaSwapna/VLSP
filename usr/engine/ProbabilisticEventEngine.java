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
    int timeToEnd_;   // Time to end of simulation (ms)
    ProbDistribution nodeCreateDist_= null;   //  Distribution for creating nodes
    ProbDistribution nodeDeathDist_= null;    // Distribution of node lifetimes
    ProbDistribution linkCreateDist_= null;   // Distribution for number of links created
    ProbDistribution linkDeathDist_= null;    // Distribution for link lifetimes
    private boolean preferentialAttachment_= false; // If true links are chosen using P.A.     

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
        if (g.getRouterList().indexOf(routerId) == -1) {
            //System.err.println("Router did not start -- adding no links");
            return;
        }
        // Schedule links
        int noLinks= linkCreateDist_.getIntVariate();
        ArrayList <Integer>nodes= new ArrayList<Integer>(g.getRouterList());
        nodes.remove(nodes.indexOf(routerId));
        int [] outlinks= g.getOutLinks(routerId);
        for (Integer l: outlinks) {
            nodes.remove(nodes.indexOf(l));
        }
        //Logger.getLogger("log").logln(USR.ERROR, "Trying to pick "+noLinks+" links");
        for (int i= 0; i < noLinks; i++) {
            if (nodes.size() <= 0) {
                break;
            }
            if (preferentialAttachment_) {  // Choose a node using pref. attach.
                int totLinks= 0;
                for (int l : nodes) {
                    totLinks+= g.getOutLinks(l).length;
                }
                int index= (int)Math.floor(Math.random()*totLinks);
                for (int j= 0; j < nodes.size(); j++) {
                    int l= nodes.get(j);
                    index-= g.getOutLinks(l).length;
                    if (index < 0 || j == nodes.size() - 1) {
                        nodes.remove(j);
                        e1= new SimEvent(SimEvent.EVENT_START_LINK,now, 
                              new Pair<Integer,Integer>(l, routerId));
                        s.addEvent(e1);
                        break;
                    }
                }
            } else { //Logger.getLogger("log").logln(USR.ERROR, "Choice set "+nodes);
                int index= (int)Math.floor( Math.random()*nodes.size());
                int newLink= nodes.get(index);
                //Logger.getLogger("log").logln(USR.ERROR, "Picked "+newLink);
                nodes.remove(index);
                e1= new SimEvent(SimEvent.EVENT_START_LINK,now, 
                new Pair<Integer,Integer>(newLink,routerId));
                s.addEvent(e1);
            }
        }
    }
    
    /** Parse the XML to get probability distribution information*/
    private void parseXMLFile(String fName) 
    {
        try { 
        DocumentBuilderFactory docBuilderFactory = 
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
        try {
            
           NodeList misc= doc.getElementsByTagName("Parameters");
           if (misc.getLength() > 1) {
              throw new SAXException ("Only one GlobalController tag allowed.");
            }
            if (misc.getLength() == 1) { 
               Node miscnode= misc.item(0);
                preferentialAttachment_= ReadXMLUtils.parseSingleBool(miscnode,
                    "PreferentialAttachment","Parameters",true);
                ReadXMLUtils.removeNode( miscnode,"PreferentialAttachment","Parameters");
            }
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
           
        }
          
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
