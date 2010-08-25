package usr.controllers.localcommand;

import usr.interactor.*;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * The QUIT command.
 */
public class ShutDownCommand extends AbstractCommand {
    /**
     * Construct a QuitCommand.
     */
    public ShutDownCommand() {
        super(MCRP.SHUT_DOWN.CMD, MCRP.SHUT_DOWN.CODE, MCRP.ERROR.CODE);
    }

    /**
     * Evaluate the Command.
     */
    public boolean evaluate(String req) {
        success("BYE");
        managementConsole.localController_.shutDown();
        return true;
    }

}
