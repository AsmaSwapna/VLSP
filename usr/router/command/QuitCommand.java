package usr.router.command;

import usr.router.Command;
import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * The QUIT command.
 */
public class QuitCommand extends AbstractCommand {
    /**
     * Construct a QuitCommand.
     */
    public QuitCommand(int succCode, int errCode, SocketChannel sc) {
        super("QUIT", succCode, errCode, sc);
    }

    /**
     * Evaluate the Command.
     */
    public void evaluate() {
        try {
            respond("500 BYE");
            managementConsole.endConnection(channel);
        } catch (IOException ioe) {
            System.err.println("MC: QUIT failed");
        }

    }

}
