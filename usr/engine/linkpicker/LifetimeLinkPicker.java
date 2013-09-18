/** Interface choses n links using some rule*/

package usr.engine.linkpicker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.w3c.dom.Node;

import usr.common.Pair;
import usr.globalcontroller.GlobalController;

public class LifetimeLinkPicker implements NodeLinkPicker {
    Random rand_;
    ArrayList <Pair<Integer,Integer>> lifetimes_;
    
    public LifetimeLinkPicker()
    {
        rand_= new Random();
    }
    
    @Override
	public ArrayList <Integer> pickNLinks(ArrayList<Integer> nodes,
        GlobalController g, int noLinks, int node)
    {
        ArrayList<Integer> picked= new ArrayList<Integer>();
        for (int i= 0; i < noLinks; i++) {
            if (nodes.size() == 0)
                break;
            int got= pickLink(nodes,g, node);
            picked.add(got); 
        }
        return picked;
    }
    
    @Override
	public int pickLink(ArrayList<Integer> nodes, GlobalController g,
        int node)
    {
        return 0;
    }
    
    public void sortLifetimes(ArrayList <Integer> nodes)
    {
        
        Collections.sort(lifetimes_);
    }
    
    @Override
	public void parseExtraXML(Node linkpicker)
    {
        
    }
}
