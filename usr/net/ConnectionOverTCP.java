package usr.net;

import usr.protocol.Protocol;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Create a connection that sends data 
 * as USR Datagrams over TCP.
 */
public class ConnectionOverTCP implements Connection {
    // End point
    TCPEndPoint endPoint;
    
    static final int PACKETS_BEFORE_SHUFFLE= 10;
    static final byte []checkbytes="USRD".getBytes();
    // The underlying connection
    SocketChannel channel;

    // The local address for this connection
    Address localAddress;

    // A ByteBuffer to read into
    int bufferSize_ = 20000;
    ByteBuffer buffer;

    // current position in the ByteBuffer
    int bufferEndData_= 0;
    int bufferStartData_= 0;

    // counts
    int inCounter = 0;
    int outCounter = 0;

    /**
     * Construct a ConnectionOverTCP given a TCPEndPointSrc
     */
    public ConnectionOverTCP(TCPEndPointSrc src) throws IOException {
        endPoint = src;
        buffer = ByteBuffer.allocate(bufferSize_);
    }

    /**
     * Construct a ConnectionOverTCP given a TCPEndPointDst
     */
    public ConnectionOverTCP(TCPEndPointDst dst) throws IOException {
        endPoint = dst;
        buffer = ByteBuffer.allocate(bufferSize_);
    }

    /**
     * Connect.
     */
    public boolean connect() throws IOException {
        endPoint.connect();

        Socket socket = endPoint.getSocket(); 

        if (socket == null) {
            throw new Error("EndPoint: " + endPoint + " is not connected");
        }
        

        channel = socket.getChannel();

        if (channel == null) {
            throw new Error("Socket: " + socket + " has no channel");
        }
        
        return true;
    }

    /**
     * Get the Address for this connection.
     */
    public Address getAddress() {
        return localAddress;
    }

    /**
     * Set the Address for this connection.
     */
    public Connection setAddress(Address addr) {
        localAddress = addr;
        return this;
    }


    public boolean sendDatagram(Datagram dg) {
        //System.err.println("ConnectionOverTCP: " + outCounter + " sendDatagram() ");
      //  System.err.println("SENDING DATAGRAM LENGTH "+dg.getTotalLength());
      //  ByteBuffer b= ((DatagramPatch)dg).toByteBuffer();
     //   System.err.println("WRITE as bytes "+ b.asCharBuffer());
       // for (int i= 0; i < dg.getTotalLength(); i++) {
        //      System.err.println("At pos"+i+" char is "+ (char)b.get());
       // }
        try {
            int count = getChannel().write(((DatagramPatch)dg).toByteBuffer());

            if (count == -1) {
                return false;
            } else {
                //System.err.println("ConnectionOverTCP: " + endPoint + " " + outCounter + " write " + count);
                outCounter++;

                return true;
            }
        } catch (IOException ioe) {
            return false;
        }
    }


    /**
     * Read a Datagram.
     */
    public Datagram readDatagram() {
        Datagram dg =  readDatagramAndWait();

        if (dg == null) {
            //System.err.println("ConnectionOverTCP: " + endPoint + " " + inCounter + " read NULL");
        } else {
            //System.err.println("ConnectionOverTCP: " + endPoint + " " + inCounter + " read " + dg.getTotalLength());
            inCounter++;
        }

        return dg;
    }

    /**
     * Read a Datagram.
     * This actually reads from the network connection.
     * The datagram read is a GID type datagram or at least
     * assumes length as a short in bits 5-8
     */
    Datagram readDatagramAndWait() {
    
        // Read a datagram at pos bufferStartData_ -- it may be partly read into buffer
        // and it may have other datagrams following it in buffer

        try {
          // Do we have enough space left in buffer to read at least
          // packet length
          if (bufferSize_ - bufferStartData_ < 8) {
              shuffleBuffer();
          }
          
          // Try to read more data
          readMoreData();
          // get at least enough data to read length or exit
          if (bufferEndData_ - bufferStartData_ < 8) {
              return null;
          }
          short packetLen= getPacketLen();
          
          // If our buffer is too short (want several packets before recopy)
          //, make it longer and read more data
         
          if (packetLen * PACKETS_BEFORE_SHUFFLE > bufferSize_) {
             // System.err.println("Increasing buffer size");
              bufferSize_= packetLen * PACKETS_BEFORE_SHUFFLE;
              ByteBuffer bigB= ByteBuffer.allocate(bufferSize_);
              int bufferRead= bufferEndData_- bufferStartData_;
              buffer.position(bufferStartData_);
              buffer.limit(bufferEndData_);
              bigB.put(buffer);
              buffer=bigB;
              bufferStartData_= 0;
              bufferEndData_= bufferRead;
              readMoreData();
          }
          // Because of buffer position we cannot read a full packet
          if (packetLen > bufferSize_ - bufferStartData_) {
          
              shuffleBuffer();
              readMoreData();  
          }
          
          if (bufferEndData_ - bufferStartData_ < packetLen) {
              return null;
          }
          // OK -- we got a full packet of data, let's make a datagram of it
          
          byte[] latestDGData = new byte[packetLen];
          
          //System.out.println("READING PACKET FROM "+bufferStartData_+ " to "+
          //  bufferStartData_+packetLen);
          buffer.position(bufferStartData_);
          buffer.get(latestDGData);
          //for (int i= 0; i < packetLen; i++) {
           //   System.err.println("At pos"+i+" char is "+ (char) latestDGData[i]);
          //}
          
          bufferStartData_+= packetLen;
          ByteBuffer newBB = ByteBuffer.wrap(latestDGData);
          // get an empty Datagram
          Datagram dg = DatagramFactory.newDatagram(Protocol.DATA, null);  // WAS new IPV4Datagram();                        
          // and fill in contents
          ((DatagramPatch)dg).fromByteBuffer(newBB);
          
          checkDatagram(latestDGData,dg);
          return dg;
          
        } catch (IOException ioe) {
             //System.err.println("Connection over TCP read error "+ioe.getMessage());
             // TODO:  THIS ERROR DOES OCCUR SOMETIMES -- DO NOT KNOW WHY
             return null;
        }
              
     
    } 
    
    void checkDatagram (byte []latestDGData, Datagram dg) 
    {
    if (latestDGData[0] != checkbytes[0] ||
          latestDGData[1] != checkbytes[1] ||
          latestDGData[2] != checkbytes[2] ||
          latestDGData[3] != checkbytes[3])
              {
              System.err.println("Read incorrect datagram "+latestDGData);
              System.err.println("Buffer size "+bufferSize_+" start pos "+bufferStartData_ +
                " end Pos "+bufferEndData_);
              ByteBuffer b= ((DatagramPatch)dg).toByteBuffer();
              System.err.println("READ as bytes "+ b.asCharBuffer());
              System.exit(-1);
          }
    }
    
    void shuffleBuffer() 
    {
        //System.err.println("Shuffling the buffer " + inCounter);
        int remaining= bufferEndData_-bufferStartData_;
        if (remaining == 0) {
            bufferStartData_= 0;
            bufferEndData_= 0;
            buffer.position(0);
            return;
        }

        // two versions of shuffling data

        /*
        // this does a two copy shuffle
        byte [] tmp= new byte[remaining];
        buffer.position(bufferStartData_);
        buffer.get(tmp);
        buffer.position(0);
        buffer.put(tmp);
        */

        // this is a single copy shuffle
        ByteBuffer newBuf = ByteBuffer.allocate(bufferSize_);
        buffer.position(bufferStartData_);
        newBuf.put(buffer);
        buffer = newBuf;
        
        bufferEndData_= remaining;
        bufferStartData_= 0;
    }
    
    /** Get length of packet from data in buffer -- implicit assumption here
    about position of data*/
    short getPacketLen() {
        short pktLen= buffer.getShort(bufferStartData_+5);
        //System.err.println("READ PACKET LENGTH "+pktLen);
        return pktLen;
    }
    
    /** Read more data from channel to buffer if possible */
    void readMoreData() throws IOException {
        buffer.position(bufferEndData_);
     
        try {
            int count= getChannel().read(buffer);

            if (count == -1)
                return;
            bufferEndData_+= count;
            //System.err.println("ConnectionOverTCP: READ "+count+" bytes");
           // for (int i= 0; i < count; i++) {
          //      byte b= buffer.get(bufferStartData_+i);
         //       System.err.println ("Byte "+i+ " as char "+(char)b);
         //   }
        } catch (IOException ioe) {
             //System.err.println("Connection over TCP read error "+ioe.getMessage());
             // TODO:: THIS ERROR DOES OCCUR SOMETIMES
             return;
        }
    }
    
   
    /**
     * Close the connection.
     */
    public void close() {
        Socket socket = getSocket();

        try {
            socket.close();
        } catch (IOException ioe) {
            throw new Error("Socket: " + socket + " can't close");
        }
    }

    /**
     * Get the EndPoint of this Connection.
     */
    public EndPoint getEndPoint() {
        return endPoint;
    }

    /**
     * Get the socket.
     */
    public Socket getSocket() {
        return endPoint.getSocket();
    }

    /**
     * Get the channel.
     */
    private SocketChannel getChannel() {
        return endPoint.getSocket().getChannel();
    }

    /**
     * To String
     */
    public String toString() {
        return endPoint.toString() + " " + getSocket().toString();
    }


}
