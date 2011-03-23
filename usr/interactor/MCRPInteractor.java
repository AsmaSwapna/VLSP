// MCRPInteractor.java

package usr.interactor;

import usr.logging.*;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.*;
import java.util.List;
import java.util.ArrayList;

/**
 * This class implements the MCRP protocol for interacting
 * with the ManagementConsole of a Component.
 * It has methods for most requests in the MCRP protocol.
 * Most of the methods will throw an MCRPException if
 * somethnig is wrong.
 * Ones to look for are:
 *
 * <ul>
 * <li> MCRPNotReadyException: if this object is not ready to
 * accept a new request </li>
 * <li> MCRPNoConnectionException, if this object is not connected to
 * a component, probably after a quit(). </li>
 * </ul>
 */
public abstract class MCRPInteractor {

    // The socket to the server
    Socket socket = null;
    Reader input = null;
    PrintWriter output = null;
    int port_;

    // The handler for socket input.
    // It runs in a separate Thread
    // and does callbacks to this object
    // to inform about what is happening 
    // on the input.
    InputHandler inputHandler = null;

    // The last response from the component
    MCRPResponse lastResponse = null;

    // An exception thrown when trying to get data from the server
    Exception serverException = null;

    // This is the finite state machine
    FSMState fsm = FSMState.FSM_WAITING;


    /* The event listeners. */
    ArrayList <MCRPEventListener> eventListeners = new ArrayList <MCRPEventListener>();

    // debug
    public static boolean debug = false;
	




    /* Initialize the connection */

    /**
     * Initialize a connection to the ManagementConsole of a component.
     * @param addr the address of the server
     * @param port the port the server is listening on
     */
    protected void initialize(InetAddress addr, int port) throws UnknownHostException, IOException  {
        port_= port;
	// set the fsm to FSM_READY
	fsm = FSMState.FSM_READY;

	// open the socket
	socket = new Socket(addr, port); 

	// get the input
	input = new InputStreamReader(socket.getInputStream());
	// get the output
	output = new PrintWriter(socket.getOutputStream(), true);

	// create a handler for the input
	inputHandler = new InputHandler(this, input);
    }

    /**
     * Terminate this MCRPInteractor
     */
  /*  public void terminate() {
        //Logger.getLogger("log").logln(USR.STDOUT, getClass().getName() + ": terminate");
        if (inputHandler != null) {
            inputHandler.stop();
        }
    }*/


    /* Methods for managing event listeners. */

    /**
     * Add an event listener.
     * @param l an object that implements MCRPEventListener
     */
    public void addMCRPEventListener(MCRPEventListener l) {
	eventListeners.add(l);
    }
      
    /**
     * Remove an event listener.
     * @param l an object that implements MCRPEventListener
     */
    public void removeMCRPEventListener(MCRPEventListener l) {
	eventListeners.remove(l);
    }
      

    /**
     * Get the number of listeners.
     */
    public int getMCRPEventListenerCount() {
	return eventListeners.size();
    }

    /**
     * Get the actual listener objects.
     */
    public MCRPEventListener[] getMCRPEventListeners() {
	return (MCRPEventListener[])eventListeners.toArray(new MCRPEventListener[0]);
    }

    /**
     * Pass the event to the all of listeners.
     */
    private void generateEvent(MCRPEvent evt) {
	for (MCRPEventListener l : getMCRPEventListeners()) {
	    l.ssipEvent(evt);
	}
    }

    /*
     * Internal methods.
     */

    /**
     * The callback for the SocketInputHandler, with a response.
     * @param response the response from the server
     */
    protected synchronized void gotResponse(MCRPResponse response) {
	lastResponse = response;

	if (debug) {
	    Logger.getLogger("log").logln(USR.STDOUT, "MCRPInteractor: response code = " + lastResponse.getCode());
	}

	fsm = FSMState.FSM_READY;

	notifyAll();
    }
     
    /**
     * The callback for the SocketInputHandler, with an event.
     * The response is converted into an event object.
     * @param response the response from the server
     * @throws IllegalArgumentException if a response is passed in that does not have an expected event code.
     */
    protected void gotEvent(MCRPResponse response) throws IllegalArgumentException {
	String code = response.getCode();

	MCRPEvent event;

       // System.out.println(getClass().getName() + ": got event " + code);

	if (code.equals("700")) {
            String connectionName = response.get(0)[1];
            // LINK_DIED event
	    event = new MCRPEvent(this, MCRPEvent.MCRPEventType.LINK_DIED, connectionName);

        } else if (code.equals("701")) {
            String connectionName = response.get(0)[1];
            // LINK_RESTARTED event
	    event = new MCRPEvent(this, MCRPEvent.MCRPEventType.LINK_RESTARTED, connectionName);

	} else if (code.equals("702")) {
            // now we convert the replies in the response
            // into a list of connections

            // get no of netifs
            int routerReplies = response.getReplies() - 1;

            // create a list for the names
            List<String> stats = new ArrayList<String>();

            for (int r=0; r < routerReplies; r++) {
                // pick out the r-th connection
                stats.add(response.get(r)[1]);
            }


	    // ROUTER_STATS event
	    event = new MCRPEvent(this, MCRPEvent.MCRPEventType.ROUTER_STATS, stats);

	} else {
	    // not a valid event code
	    throw new IllegalArgumentException("MCRPInteractor: unknown event code from server: " + code);
	}

	Logger.getLogger("log").logln(USR.ERROR, "Event = " + event);

	// pass the event to the listeners, if there are any
	if (getMCRPEventListenerCount() > 0) {
	    // pass the event
	    generateEvent(event);
	}

    }
     
    /**
     * The callback for the InputHandler saying EOF.
     */
    protected synchronized void gotEOF() {
	// we got an EOF notification from the InputHandler
	// set the fsm to the end state.
	fsm = FSMState.FSM_END;

	if (debug) {
	    Logger.getLogger("log").logln(USR.ERROR, "Got EOF");
	}

	// shut the socket and cleanup.
	try {
	    socket.close();
	} catch (IOException ioe) {
	    ;
	} finally {
	    // free up SocketInputHandler
	    inputHandler = null;
	    input = null;
	    output = null;
	    socket = null;
	}
    }
     
    /**
     * The callback from the InputHandler saying there 
     * has been an exception.
     */
    protected synchronized void gotException(Exception e) {
	// we got an Exception notification from the SocketInputHandler
	// set the fsm to the end state.
        Logger.getLogger("log").logln(USR.STDOUT, getClass().getName() + ": gotException " + e);

	fsm = FSMState.FSM_END;

	// save the exception to throw a bit later
	serverException = e;

	// shut the socket and cleanup.
	try {
	    socket.close();
	} catch (IOException ioe) {
	} finally {
	    // free up SocketInputHandler
	    inputHandler = null;
	    input = null;
	    output = null;
	    socket = null;


	    // now notifyAll to get out of the wait
	    notifyAll();
	}
    }
     
    /**
     * Interact with the server.
     * This sends a string and waits for a response.
     * @param str The string to pass to the server as the request
     * @return the response
     */
    protected synchronized MCRPResponse interact(String str) throws MCRPException {

	// check if we are ready to interact with the server
	if (fsm == FSMState.FSM_READY) {
	    // yes, we are ready
	    // so send the string
	    send(str);

	    // set the finite state machine to state: waiting
	    fsm = FSMState.FSM_WAITING;

	    // sit and wait for a response
	   
            try {
                wait(5000);
            } catch (InterruptedException ie) {
                //Logger.getLogger("log").logln(USR.ERROR, "Got InterruptedException " + ie);
	    }

	    if (fsm == FSMState.FSM_WAITING) {
	        Logger.getLogger("log").logln(USR.ERROR,"MCRP Interactor Got timeout: command was:"+str);
	        fsm= FSMState.FSM_READY;
	        throw new MCRPException("MCRPInteractor: wait timeout: ");
	    }

	    // we got here because there was a notifyAll.
	    // this happens after gotResponse()
	    // or gotException()

	    if (fsm == FSMState.FSM_READY) {
		// that's good
		return lastResponse;

	    } else if (fsm == FSMState.FSM_END) {
		// the fsm was set to the end state.
		// probably EOF
		return lastResponse;
	    } else {
		// an unknown state
		throw new MCRPException("MCRPInteractor: finite state machine in unknown state: " + fsm);
	    }

	} else {
	    // we're not ready to interact with the server
	    if (fsm == FSMState.FSM_WAITING) {
		// we're waiting for a response
		throw new MCRPNotReadyException("MCRPInteractor: not ready to interact with the server.  Awaiting a response from " + str);

	    } else if (fsm == FSMState.FSM_END) {
		// we're at eof
		throw new MCRPNoConnectionException("MCRPInteractor: no connection to server");

	    } else {
		// an unknown state
		throw new MCRPException("MCRPInteractor: finite state machine in unknown state: " + fsm);
	    }
	}
    }

    /**
     * Send the string to the socket
     * @param str The string to pass to the server as the request
     */
    private void send(String str) {
	if (debug) {
	    System.out.print(">> " + str);
	    System.out.print(" ");
	}
  //System.err.print(">>" + str+"\n");
  if (str.indexOf("\n") != -1) {
      System.err.println("Cannot send console messages with \\n character");
  }
	output.print(str);
	output.print("\n");
	output.flush();
	if (output.checkError()) {
	    System.err.println("Error in writing to stream in MCRPInteractor");
	}   
    }

    /**
     * Check the code in the last response
     * is the one we expect.
     * @param expected the response code we expect.
     */
    protected void expect(int expected) throws MCRPException {
        expect(Integer.toString(expected));
    }

    /**
     * Check the code in the last response
     * is the one we expect.
     * @param expected the response code we expect.
     */
    protected void expect(String expected) throws MCRPException {
	String actual = lastResponse.getCode();
	if (expected.equals(actual)) {
	    // everything is OK, the actual code and
	    // the expected code are the same
	    return;
	} else {
	    String message = lastResponse.get(lastResponse.getReplies() - 1)[1];
	    // we got a different code from the expected one
	    throw new MCRPException("Expected return code: " + expected + 
				    " Got: " + actual +
				    " Message is: " + message);
	}

    }
    
    public int getPort() {
        return port_;
    }


    /**
     * The states of the FSM
     */
    enum FSMState {
        FSM_WAITING,   // waiting for a response
        FSM_READY,     // ready to accept a request
        FSM_END        // we have reached the end state, the connection is closed
    }

}
