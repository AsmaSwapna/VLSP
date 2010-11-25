package usr.router;
import usr.net.Datagram;
import usr.net.Address;

import usr.logging.*;
/**
 * Interface is for "glue" to hold together netifs -- it allows routing between them
 */
public interface NetIFListener {


    /** Return the router Fabric device for this datagram -- this is
    the correct way to route datagrams */    
    public FabricDevice getRouteFabric(Datagram dg);
    
    /** Is this address an address associated with this netiflistener*/
    public boolean ourAddress(Address a);
    
    /** Deal with TTL expire */
    public void TTLDrop(Datagram dg);

}
