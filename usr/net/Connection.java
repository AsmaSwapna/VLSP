package usr.net;

import java.io.IOException;
import usr.logging.*;

/**
 * A Connection between 2 routers.
 * Each router holds 1 EndPoint for the Connection.
 */
public interface Connection {

    /**
     * Connect
     */
    public boolean connect() throws IOException;

    /**
     * Send a Datagram over the Connection.
     */
    public boolean sendDatagram(Datagram dg);

    /**
     * Read a Datagram from a Connection.
     */
    public Datagram readDatagram();

    /**
     * Get the EndPoint of this Connection.
     */
    public EndPoint getEndPoint();

    /**
     * Get the User Space Routing Address for this Connection.
     */
    public Address getAddress();

    /**
     * Set the Address for this connection.
     */
    public Connection setAddress(Address addr);


}
