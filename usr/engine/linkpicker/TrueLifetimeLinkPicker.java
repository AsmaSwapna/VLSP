/** Interface choses n links using some rule*/

package usr.engine.linkpicker;
import java.util.ArrayList;
import java.util.PriorityQueue;

import org.w3c.dom.Node;

import usr.events.EndRouterEvent;
import usr.events.Event;
import usr.globalcontroller.GlobalController;
import usr.lifeEstimate.NodeAndLifetime;
/**
 * Class chooses links based upon finding the node with the longest lifetime
 * uses actual lifetime not estimate
 * @author richard
 *
 */
public class TrueLifetimeLinkPicker implements NodeLinkPicker {
    PriorityQueue <NodeAndLifetime> orderNodes_;

    public TrueLifetimeLinkPicker()
    {
        orderNodes_= new PriorityQueue<NodeAndLifetime>();
    }

    @Override
	public ArrayList <Integer> pickNLinks(ArrayList<Integer> nodes,
        GlobalController g, int noLinks, int node)
    {
        ArrayList<Integer> picked= new ArrayList<Integer>();
        long time= g.getElapsedTime();
        createQueue(nodes,time,g);
        for (int i= 0; i < noLinks; i++) {
            if (orderNodes_.size() == 0)
                break;
            int link= orderNodes_.poll().getNode();
            picked.add(link);
        }
        orderNodes_.clear();
        return picked;
    }

    @Override
	public int pickLink(ArrayList<Integer> nodes, GlobalController g,
        int node)
    {
    	long time= g.getElapsedTime();
    	createQueue(nodes, time, g);
        int thisNode= orderNodes_.poll().getNode();
        orderNodes_.clear();
        return thisNode;
    }


    private void createQueue(ArrayList<Integer> nodes, long time, GlobalController g)
    {
    	for (Event e: g.getEventScheduler().getEvents()) {
    		if (e instanceof EndRouterEvent) {
    			EndRouterEvent ere= (EndRouterEvent)e;
    			int rno;
    			try {
    				rno= ere.getNumber(g);
    			} catch (InstantiationException ex) {
    				continue;
    			}
    			if (nodes.contains(rno)) {
    				orderNodes_.add(new NodeAndLifetime(rno,ere.getTime()-g.getElapsedTime()));
    			}

    		}
    	}
    }

    @Override
	public void parseExtraXML(Node linkpicker)
    {

    }
}
