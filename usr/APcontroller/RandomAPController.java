package usr.APcontroller;

import java.util.*;
import usr.router.RouterController;
import usr.globalcontroller.GlobalController;
import usr.router.RouterOptions;
import usr.logging.*;

/** Implements Random AP Controller -- default actions are from NullAPController*/

public class RandomAPController extends NullAPController {

    RandomAPController (RouterOptions o) {
        super(o);
    } 
        
    /** Router regular AP update action */
    public void routerUpdate(long time, RouterController r) 
    {
        //System.err.println ("Controller called");
    }
    
    /** Controller regular AP update action */
    public void controllerUpdate(long time, GlobalController g)
    {
        super.controllerUpdate(time, g);
        if (gotMinAPs(g)) {
            if (overMaxAPs(g) && canRemoveAP(g)) {   // Too many APs, remove one
                int no= noToRemove(g);
                removeAP(time,g,no);

                
            }
            return;
        }
        int no= noToAdd(g);
        if (no <= 0)
            return;
        addAP(time,g,no);
        
    }
    
    /** Add no aggregation points chosen at random */
    void addAP(long time, GlobalController g, int no) 
    {
      for (int i= 0; i < no; i++) {
        ArrayList <Integer> elect= nonAPNodes(g);
        Logger.getLogger("log").logln(USR.STDOUT,leadin()+" adding random AP");
        // No nodes which can be made managers
        int nNodes= elect.size();
        if (nNodes == 0) {
            return;
        }
        // Choose a random node to become an AP manager
        int index= (int)Math.floor( Math.random()*nNodes);
        int elected= elect.get(index);
        addAccessPoint(time, elected, g);
      }
    }
    
    
    /** Remove no aggregation points chosen at random */
    void removeAP(long time, GlobalController g, int no) 
    {
        for (int i= 0; i < no; i++) {
            ArrayList <Integer> APs= new ArrayList<Integer>(getAPList());
            while (APs.size() > 0) {
                int nAPs= APs.size();
                int j= (int)Math.floor( Math.random()*nAPs);
                int rno= APs.get(j);
                if (removable(rno,g)) {
                    Logger.getLogger("log").logln(USR.STDOUT,
                            leadin()+" too many APs remove "+rno);
                    removeAccessPoint(time, rno);
                    break;
                }
                APs.remove(j);
            }
        }
    }
    String leadin() {
        return ("RandomAPController:");
    }
     /** Create new APInfo */
    
    public APInfo newAPInfo() {
        return new RandomAPInfo();
    }
    
}
