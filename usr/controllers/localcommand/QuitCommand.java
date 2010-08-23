package usr.controllers.localcommand;

import usr.interactor.*;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * The QUIT command.
 */
public class QuitCommand extends AbstractCommand {
    /**
     * Construct a QuitCommand.
     */
    public QuitCommand() {
        super(MCRP.QUIT.CMD, MCRP.QUIT.CODE, MCRP.ERROR.CODE);
    }

    /**
     * Evaluate the Command.
     */
    public boolean evaluate(String req) {
        success("BYE");
        managementConsole.endConnection(getChannel());

        return true;
    }

}
