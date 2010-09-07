package usr.router;

import java.util.List;

/**
 * A RouterFabric within UserSpaceRouting.
 */
public interface RouterFabric {
    /**
     * Add a Network Interface to this Router.
     */
    public RouterPort addNetIF(NetIF netIF);

    /**
     * Remove a Network Interface from this Router.
     */
    public boolean removeNetIF(NetIF netIF);

    /**
     * Get port N.
     */
    public RouterPort getPort(int p);

    /**
     * Get a list of all the ports with Network Interfaces.
     */
    public List<RouterPort> listPorts();


    /** Return the interface which connects to a given host */
    public NetIF findNetIF(String host);

    /**
     * Close ports
     */
    public void closePorts();

    /**
     * Close port.
     */
    public void closePort(RouterPort port);

    /**
     * Start me up.
     */
    public boolean start();

    /**
     * Stop the RouterController.
     */
    public boolean stop();
    
    /** List Routing table */
    
    public String listRoutingTable();
}
