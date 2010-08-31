package usr.router;

import usr.net.Address;
import usr.net.IPV4Address;
import usr.console.*;
import usr.router.command.*;
import java.util.*;
import java.util.concurrent.*;
import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.nio.*;
import java.nio.charset.Charset;

/**
 * A ManagementConsole listens for connections
 * for doing router management.
 * <p>
 * It implements the MCRP (Management Console Router Protocol).
 */
public class RouterManagementConsole extends AbstractManagementConsole implements Runnable {

    private RouterController _routerController;
    public RouterManagementConsole(RouterController rc, int port) {
       
       _routerController= rc;
       initialise(port);
    }

    /**
     * Get the ComponentController this ManagementConsole
     * interacts with.
     */
    public ComponentController getComponentController() {
       return _routerController;
    }

    public void registerCommands() {
        register(new UnknownCommand());
        register(new QuitCommand());
        register(new GetNameCommand());
        register(new SetNameCommand());
        register(new GetConnectionPortCommand());
        register(new GetAddressCommand());
        register(new SetAddressCommand());
        register(new ListConnectionsCommand());
        register(new IncomingConnectionCommand());
        register(new CreateConnectionCommand());
        register(new ShutDownCommand());
    }
    
}
