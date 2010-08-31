package usr.router.command;

import usr.protocol.MCRP;
import usr.router.RouterManagementConsole;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * The SHUT_DOWN command.
 */
public class ShutDownCommand extends RouterCommand {
    /**
     * Construct a ShutDownCommand.
     */
    public ShutDownCommand() {
        super(MCRP.SHUT_DOWN.CMD, MCRP.SHUT_DOWN.CODE, MCRP.ERROR.CODE);
    }

    /**
     * Evaluate the Command.
     */
    public boolean evaluate(String req) {
        success("SHUTDOWN");
        controller.shutDown();
        return true;
    }

}
