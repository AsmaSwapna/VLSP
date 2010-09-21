package usr.router;

import usr.net.*;

/**
 * An entry in a routing table.
 */
public class SimpleRoutingTableEntry implements RoutingTableEntry {
    private Address address_;
    private int cost_;
    NetIF inter_;

    SimpleRoutingTableEntry(Address addr, int cost, NetIF inter) {
        address_= addr;
        cost_= cost;
        inter_= inter;
    }
    
    SimpleRoutingTableEntry(String tableEntry, RouterFabric fabric) {
        String []args= tableEntry.split(" ");
        if (args.length != 2 && args.length != 3) {
            System.err.println("Attempt to construct routing table entry "+
             "from incorrect string" + tableEntry);
            System.exit(-1);
        }
        try {
            int gid= Integer.parseInt(args[0]);
            address_= new GIDAddress(gid); 
            cost_= Integer.parseInt(args[1]);
            System.err.println("READ GID "+gid+" cost "+cost_);
            if (args.length == 2) {
               inter_= null; 
            } else {
               inter_= fabric.findNetIF(args[2]);
            }
        } catch (Exception e) {
            System.err.println("Cannot parse" + tableEntry);
            return;
        }
    }
    
     SimpleRoutingTableEntry(String tableEntry, NetIF inter) {
        String []args= tableEntry.split(" ");
        if (args.length != 3 && args.length != 2) {
            System.err.println("Attempt to construct routing table entry "+
             "from incorrect string" + tableEntry);
            System.exit(-1);
        }
        
        try {
            int gid= Integer.parseInt(args[0]);
            address_= new GIDAddress(gid); 
            cost_= Integer.parseInt(args[1]);
            inter_= inter;
           // System.err.println("READ GID "+gid+" cost "+cost_);
        } catch (Exception e) {
            System.err.println("Cannot parse" + tableEntry);
            return;
        }
    }
    
    public Address getAddress() {
        return address_;
    }
    
    public NetIF getNetIF() {
        return inter_;
    }
    
    int getCost() {
        return cost_;
    }
    void setCost(int cost) 
    {
        cost_= cost;
    }
    
    
     
    public String toString() {
        String entry;
        if (inter_ == null) {
          entry= address_.toString() + " " + cost_;
        } else {
          entry= address_.toString() + " " + cost_ + " " + 
            inter_.getRemoteRouterName();
        }
        //System.err.println("ENTRY: "+entry);
        return entry;
    }

}
