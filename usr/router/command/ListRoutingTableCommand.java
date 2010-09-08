package usr.router.command;

import usr.protocol.MCRP;
import usr.router.RouterManagementConsole;
import usr.router.RoutingTable;
import usr.router.RoutingTableEntry;
import usr.router.NetIF;
import usr.net.Address;
import java.util.List;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * The LIST_ROUTING_TABLE command.
 */
public class ListRoutingTableCommand extends RouterCommand {
    /**
     * Construct a ListRoutingTableCommand
     */
    public ListRoutingTableCommand() {
        super(MCRP.LIST_ROUTING_TABLE.CMD, MCRP.LIST_ROUTING_TABLE.CODE, 
          MCRP.LIST_ROUTING_TABLE.CODE);
    }

    /**
     * Evaluate the Command.
     */
    public boolean evaluate(String req) {
        RoutingTable table = controller.getRoutingTable();

        for (RoutingTableEntry e : table.getEntries()) {
            list(e.getEntryAsString());
        }

        boolean result = success("END " + table.size());

        if (!result) {
            System.err.println(leadin() + "LIST_ROUTING_TABLE response failed");
        }

        return result;

    }

}
