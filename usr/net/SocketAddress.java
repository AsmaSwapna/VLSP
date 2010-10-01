package usr.net;

/**
 * A Socket Address has an address and a port.
 */
public class SocketAddress {
    // The address
    Address address;

    // The port
    int port;

    /**
     * Construct a SocketAddress from an address and a port.
     */
    public SocketAddress(Address addr, int port) {
        this.address = addr;
        this.port = port;
    }

    /**
     * Get the Address
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Get the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Equals
     */
    public boolean equals(Object obj) {
        if (obj instanceof SocketAddress) {
            SocketAddress sockaddr = (SocketAddress)obj;

            if (sockaddr.getAddress().equals(getAddress()) &&
                sockaddr.getPort() == getPort()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * To String
     */
    public String toString() {
        return address + ":" + port;
    }


}
