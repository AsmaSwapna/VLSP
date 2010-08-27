package usr.router.command;

import usr.console.MCRP;
import usr.router.RouterManagementConsole;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * The GET_CONNECTION_PORT command.
 */
public class GetConnectionPortCommand extends RouterCommand {
    /**
     * Construct a GetConnectionPortCommand
     */
    public GetConnectionPortCommand() {
        super(MCRP.GET_CONNECTION_PORT.CMD, MCRP.GET_CONNECTION_PORT.CODE, MCRP.ERROR.CODE);
    }

    /**
     * Evaluate the Command.
     */
    public boolean evaluate(String req) {
        int port = controller.getConnectionPort();
        
        boolean result = success(""+port);

        if (!result) {
            System.err.println("MC: GET_CONNECTION_PORT failed");
        }

        return result;
    }

}
