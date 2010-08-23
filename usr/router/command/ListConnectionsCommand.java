package usr.router.command;

import usr.interactor.*;
import usr.router.RouterManagementConsole;
import usr.router.*;
import usr.net.Address;
import java.util.List;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * The LIST_CONNECTIONS command.
 */
public class ListConnectionsCommand extends AbstractCommand {
    /**
     * Construct a ListConnectionsCommand.
     */
    public ListConnectionsCommand() {
        super(MCRP.LIST_CONNECTIONS.CMD, MCRP.LIST_CONNECTIONS.CODE, MCRP.ERROR.CODE);
    }

    /**
     * Evaluate the Command.
     */
    public boolean evaluate(String req) {
        List<RouterPort> ports = controller.listPorts();
        int count = 0;
        for (RouterPort rp : ports) {
            if (rp.equals(RouterPort.EMPTY)) {
                continue;
            } else {
                NetIF netIF = rp.getNetIF();
                Address address = netIF.getAddress();
                String portString = "port" + rp.getPortNo() + " " + netIF.getName() + " " + netIF.getRemoteRouterName() + " " + netIF.getWeight() + " " + (address == null ? "No_Address" : address.toString());
                list(portString);
                count++;
            }               
        }             

        boolean result = success("END " + count);

        if (!result) {
            System.err.println("MC: LIST_CONNECTIONS response failed");
        }

        return result;

    }

}
