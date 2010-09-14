package usr.localcontroller;

import java.lang.*;
import java.util.*;
import java.io.IOException;
import java.net.*;
import usr.console.*;
import usr.router.*;
import usr.common.LocalHostInfo;
import usr.common.BasicRouterInfo;
import usr.common.ProcessWrapper;
import usr.common.ThreadTools;
import usr.interactor.*;


/** The local controller is intended to run on every machine.  
 * Its job is to start up router processes as needed.
 * It should be started by being given a port to listen on
 * specified on the command line
 * java LocalController.java 8080
 * In its own start up the Global Controller will contact each
 * Local Controller to give it more state
*/
public class LocalController implements ComponentController {
    private LocalControllerInfo hostInfo_;
    private LocalHostInfo globalController_;
    private GlobalControllerInteractor gcInteractor_= null;
    private LocalControllerManagementConsole console_= null;
    private boolean listening_ = true;
    private ArrayList <BasicRouterInfo> routers_= null;
    private ArrayList <RouterInteractor> routerInteractors_ = null;
    private ArrayList <Router> routerList_= null;
    private HashMap<String, ProcessWrapper> childProcessWrappers_ = null;
    private HashMap<Integer, BasicRouterInfo> routerMap_ = null;
    private String routerConfig_= "";  // String contains config for routers
    private String classPath_= null;

    private String myName = "LocalController";
    
    private String routerConfigString_= "";
    
    /**
     * Main entry point.
     */
    public static void main(String[] args) {
        
        LocalController self_;
        
        if (args.length != 1) {
            System.err.println("Command line must specify "+
              "port number to listen on and nothing else.");
            System.exit(-1);
        }
        int port= 0;
        
        try {
          port= Integer.parseInt(args[0]);
          if (port < 0) 
            throw new NumberFormatException ("Port number must be > 0");
        }
        catch (NumberFormatException e) {
          System.err.println("Unable to understand port number."+
            e.getMessage());
          System.exit(-1);
        }
        self_= new LocalController(port);

    }
    
    /** Constructor for local controller starting on port */
    public LocalController (int port) {
        hostInfo_= new LocalControllerInfo(port);
        routers_= new ArrayList<BasicRouterInfo>();
        routerList_= new ArrayList<Router>();
        childProcessWrappers_ = new HashMap<String, ProcessWrapper>();
        routerMap_ = new HashMap<Integer, BasicRouterInfo>();
        routerInteractors_ = new ArrayList<RouterInteractor>();
        console_= new LocalControllerManagementConsole(this, port);
        Properties prop = System.getProperties();
        classPath_= prop.getProperty("java.class.path",null);
        console_.start();
    }
    
    /** Received shut Down data gram from global */
    public void shutDown() {

        System.out.println("Local controller got shutdown message from global controller.");

        ThreadTools.findAllThreads("LC top of shutDown:");

        System.out.println("Stopping all running routers"); 
        for (int i= 0; i < routers_.size(); i++) {

            RouterInteractor interactor = routerInteractors_.get(i);
            try {
                interactor.shutDown();
            } catch (java.io.IOException e) {
                System.err.println (leadin() + "Cannot send shut down to Router");
                System.err.println (e.getMessage()); 
            } catch (usr.interactor.MCRPException e) {
                System.err.println (leadin() + "Cannot send shut down to Router");
                System.err.println (e.getMessage());          
            }

            ThreadTools.findAllThreads("LC after router shutDown:");



        }

        System.out.println(leadin() + "Stopping process wrappers");
        Collection <ProcessWrapper> pws= (Collection<ProcessWrapper>)childProcessWrappers_.values();
        for (ProcessWrapper pw: pws) { 
            pw.stop();
        }

        System.out.println(leadin() + "Stopping global controller interactor");
        try {        
          gcInteractor_.quit();
        } catch (Exception e) {
            
            System.err.println(leadin() + "Cannot exit from global interactor");
            System.err.println(e.getMessage());
            System.exit(-1);
        }


        System.out.println(leadin() + "Stopping console");
        console_.stop();

        System.out.println(leadin()+ "Pausing.");

        try {
            Thread.sleep(10);
        } catch (Exception e) {
            System.err.println(leadin()+ e.getMessage());
            System.exit(-1);
        }


        ThreadTools.findAllThreads("LC end of shutDown:");

        
    }
    
    /**
     * Get the host info the the host this is a LocalController for.
     */
    public LocalControllerInfo getHostInfo() {
        return hostInfo_;
    }
    
    /** 
     * Received alive message from GlobalController. 
     */
    public void aliveMessage(LocalHostInfo gc) {
        globalController_= gc;
        System.out.println("Got alive message from global controller.");
        try {
            System.out.println("Sending to "+gc.getName()+":"+gc.getPort());
            gcInteractor_= new GlobalControllerInteractor(gc);
            gcInteractor_.respondToGlobalController(hostInfo_);
        } catch (java.net.UnknownHostException e) {
            System.err.println("Cannot contact global controller");
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (java.io.IOException e) {
            System.err.println("Cannot contact global controller");
            System.err.println(e.getMessage());
            System.exit(-1);
        }catch (usr.interactor.MCRPException e) {
            System.err.println("Cannot contact global controller");
            System.err.println(e.getMessage());
            System.exit(-1);
        }
       
    }
    
    /** 
     * Received start new router command
    */
    public boolean requestNewRouter (int routerId, int port1, int port2) 
    
    {

        String routerName = "Router-" + routerId;

        Process child;

        String [] cmd= new String[7];
        cmd[0] = "/usr/bin/java";
        cmd[1] = "-cp";
        cmd[2] = classPath_;
        cmd[3] = "usr.router.Router";
        cmd[4] = String.valueOf(port1);
        cmd[5] = String.valueOf(port2);
        cmd[6] = routerName;

        try {
            System.out.println(leadin() + "Starting Router on ports "+port1+" "+port2);

            child = new ProcessBuilder(cmd).start();

            childProcessWrappers_.put(routerName, new ProcessWrapper(child, routerName));

        } catch (java.io.IOException e) {
            System.err.println(leadin() + "Unable to execute command "+ Arrays.asList(cmd));
            System.err.println(e.getMessage());
            //System.exit(-1);
            return false;
        }

        /// In JVM Router
        ///Router router= new Router(maxPort_, "Router-" + routerId);
        ///router.start();
        ///routerList_.add(router);

        // Separate JVM Router
        // create a RouterInteractor
        RouterInteractor interactor = null;

        // try 50 times, with 100 millisecond gap
        int MAX_TRIES = 50;
        int tries = 0;
        int millis = 100;
        boolean isOK = false;
        for (tries = 0; tries < MAX_TRIES; tries++) {
            // sleep a bit
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ie) {
            }

            // try and connect
            try {
                interactor = new RouterInteractor("localhost", port1);
                routerInteractors_.add(interactor);
                isOK = true;
                break;
            } catch (UnknownHostException uhe) {
            } catch (IOException e) {
            }
        }

        if (! isOK) {
            // we didnt connect
            System.err.println(leadin() + "Unable to connect to Router on port " + port1);
            return false;
        } else {
            // we connected
            BasicRouterInfo br= new BasicRouterInfo(routerId,0,hostInfo_,port1);
            routers_.add(br);

            // tell the router its new name and config if available
            try {
                interactor.setName(routerName);
                if (routerConfigString_ != "") {
                    interactor.setConfigString(routerConfigString_);
                }
            } catch (IOException ioexc) {
                return false;
            } catch (MCRPException mcrpe) {
                return false;
            }
            routerMap_.put(routerId,br);
            return true;
        }
    }
    
    

    public boolean connectRouters(LocalHostInfo r1, LocalHostInfo r2) {
       System.out.println (leadin() + "Got connect request for routers");

       RouterInteractor ri= findRouterInteractor(r1.getPort());
       if (ri == null)
          return false;
       try {
           String address= r2.getName()+":"+r2.getPort();
           ri.createConnection(address,1);
       }
       catch (Exception e) {
           System.err.println("Cannot connect routers");
           System.err.println(e.getMessage());
           return false;
       }
       
       return true;
    }

    /** Local controller receives request to end a router */
    public boolean endRouter(LocalHostInfo r1) {
        System.out.println(leadin() + 
          "Got terminate request for router "+r1);
        RouterInteractor ri= findRouterInteractor(r1.getPort());
        if (ri == null)
            return false;
        try {
            ri.shutDown();
        } 
        catch (Exception e) {
            System.err.println(leadin()+"Error shutting down router");
            System.err.println(leadin()+e.getMessage());
            return false;
        }
        return true;    
    }
    
    /** Local controller receives request to end a router */
    public boolean endLink(LocalHostInfo r1, int r2) {
        System.out.println(leadin() + 
          "Got terminate request for link from"+r1+" to Id "+r2);
        RouterInteractor ri= findRouterInteractor(r1.getPort());
        if (ri == null)
            return false;
        try {
            ri.endLink(r2);
        } 
        catch (Exception e) {
            System.err.println(leadin()+"Error shutting down router");
            System.err.println(leadin()+e.getMessage());
            return false;
        }
        return true;    
    }

    /**
     * Get the name of this LocalController.
     */
    public String getName() {
        return myName + ":" + hostInfo_.getPort();
    }
    
    /** Set string which configures routers */
    public void setRouterOptions(String str) 
    {
        routerConfigString_= str;
    } 

    /**
     * Get the ManagementConsole this ComponentController interacts with.
     */
    public ManagementConsole getManagementConsole() {
        return console_;
    }

    private RouterInteractor findRouterInteractor(int port) 
    {
        for (RouterInteractor r: routerInteractors_) {
          if (port == r.getPort())
              return r;
        }
        System.err.println(leadin()+"Unable to find router interactor listening on port"+port);
        return null;
    }

    /**
     * Create the String to print out before a message
     */
    String leadin() {
        final String LC = "LC: ";

        return getName() + " " + LC;
    }



    
}



