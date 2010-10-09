package usr.net;

import usr.net.Address;
import usr.logging.*;

/**
 * A Datagram.
 */
public interface Datagram {
    /**
     * Get the header len
     */
    public byte getHeaderLength();

    /**
     * Get the total len
     */
    public short getTotalLength();

    /**
     * Get the checksum size
     */
    public byte getChecksumLength();

    /**
     * Get the flags
     */
    public byte getFlags();

    /**
     * Get the TTL
     */
    public int getTTL();

    /**
     * Set the TTL
     */
    public void setTTL(int ttl);

    /**
     * Get the protocol
     */
    public byte getProtocol();

    /**
     * Set the protocol
     */
    public Datagram setProtocol(int p);

    /**
     * Get src address.
     */
    public Address getSrcAddress();

    /**
     * Set the src address
     */
    public Datagram setSrcAddress(Address addr);

    /**
     * Get dst address.
     */
    public Address getDstAddress();

    /**
     * Set the dst address
     */
    public Datagram setDstAddress(Address addr);

    /**
     * Get src port.
     */
    public int getSrcPort();

    /**
     * Set the src port 
     */
    public Datagram setSrcPort(int port);

    /**
     * Get dst port.
     */
    public int getDstPort();

    /**
     * Set the dst port 
     */
    public Datagram setDstPort(int port);
  
    /** Reduce TTL and return true if packet still valid */
    public boolean TTLReduce();

    /**
     * Get header
     */
    public byte[] getHeader();

    /**
     * Get payload
     */
    public byte[] getPayload();

    /**
     * Get the checksum
     */
    public byte[] getChecksum();

}
