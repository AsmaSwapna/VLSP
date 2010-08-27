package usr.console;

/**
 * A ComponentController is the controller for a component
 * in the UserSpaceRouting system.
 * It interacts with a ManagementConsole.
 */
public interface ComponentController {
    /**
     * Get the ManagementConsole this ComponentController interacts with.
     */
    public ManagementConsole getManagementConsole();
}
