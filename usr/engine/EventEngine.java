/** Interface for Engine which adds events to the event list
 */

package usr.engine;

import usr.globalcontroller.*;
import usr.logging.*;

public interface EventEngine {


    /** Initial events to add to schedule */
    public void startStopEvents(EventScheduler s, GlobalController g);

    /** Initial events to add to schedule */
    public void initialEvents(EventScheduler s, GlobalController g);

    /** Add or remove events following a simulation event */
    public void preceedEvent(SimEvent e, EventScheduler s, GlobalController g);

    /** Add or remove events following a simulation event */
    public void followEvent(SimEvent e, EventScheduler s, GlobalController g,
                            Object o);

}
