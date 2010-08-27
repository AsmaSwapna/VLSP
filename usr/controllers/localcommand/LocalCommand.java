package usr.controllers.localcommand;

import usr.controllers.*;
import usr.console.*;

/**
 * A Command processes a command handled by the ManagementConsole
 * of a LocalController
 */
public abstract class LocalCommand extends AbstractCommand {
    // The ManagementConsole
    LocalControllerManagementConsole managementConsole;

    // The RouterController
    LocalController controller;


    /**
     * Construct a Command given a name, a success code, an error code.
     */
    LocalCommand(String name, int succCode, int errCode) {
        super(name, succCode, errCode);
    }

    /**
     * Set the ManagementConsole this is a command for.
     */
    public void setManagementConsole(ManagementConsole mc) {
        managementConsole = (LocalControllerManagementConsole)mc;
        controller = (LocalController)managementConsole.getComponentController();
    }
                

}
