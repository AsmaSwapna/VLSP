package usr.localcontroller.command;

import usr.protocol.MCRP;
import usr.logging.*;
import usr.router.RouterManagementConsole;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * The command to execute if the incoming command is unknown.
 */
public class UnknownCommand extends LocalCommand {
    /**
     * Construct a UnknownCommand
     */
    public UnknownCommand() {
        super("__UNKNOWN__", MCRP.ERROR.CODE, MCRP.ERROR.CODE);
    }

    /**
     * Evaluate the Command.
     */
    public boolean evaluate(String req) {
        boolean result = error("UNKNOWN " + req);

        if (!result) {
            Logger.getLogger("log").logln(USR.ERROR, leadin() + "UNKNOWN failed");
        }

        return result;
    }

}
