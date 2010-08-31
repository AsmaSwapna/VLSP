package usr.interactor;

import usr.protocol.MCRP;
import java.net.Socket;
import usr.common.LocalHostInfo;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.*;
import java.util.List;
import java.util.ArrayList; 

/**
 * This class implements the MCRP protocol and acts as a client
 * for interacting with the ManagementConsole of a GlobalController.
 */
public class GlobalControllerInteractor extends MCRPInteractor
{
    /**
     * Constructor for a MCRP connection
     * to the ManagementConsole of a GlobalController.
     * @param addr the name of the host
     * @param port the port the server is listening on
     */
    public GlobalControllerInteractor(String addr, int port) throws UnknownHostException, IOException  {
	initialize(InetAddress.getByName(addr), port);
    }

    /**
     * Constructor for a MCRP connection
     * to the ManagementConsole of a GlobalController.
     * @param addr the InetAddress of the host
     * @param port the port the server is listening on
     */
    public GlobalControllerInteractor(InetAddress addr, int port) throws UnknownHostException, IOException  {
	initialize(addr, port);
    }
    
    /**
     * Constructor for a MCRP connection
     * to the ManagementConsole of a GlobalController.
     * @param lh the LocalHostInfo description
     */
    public GlobalControllerInteractor(LocalHostInfo lh) throws UnknownHostException, IOException  {
	initialize(lh.getIp(), lh.getPort());
    }
    
    /* Calls for ManagementConsole */

    /**
     * Responds to the GlobalController.
     */
    public MCRPInteractor respondToGlobalController(LocalHostInfo lc) throws IOException, MCRPException {
      String command= MCRP.OK_LOCAL_CONTROLLER.CMD+" "+lc.getName()+" "+ lc.getPort();
	    interact(command);
	    expect(MCRP.OK_LOCAL_CONTROLLER.CODE);
	    return this;
    }

}