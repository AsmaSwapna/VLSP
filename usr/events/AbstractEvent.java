package usr.events;

import us.monoid.json.JSONObject;
import usr.engine.EventEngine;
import usr.globalcontroller.GlobalController;

/** Class represents a global controller event*/
public abstract class AbstractEvent implements Event {
    protected long time_;
    protected EventEngine engine_;

    /** Execute the event and return a JSON object with information*/
    public abstract JSONObject execute(GlobalController gc) throws InstantiationException;

    /** Return event as string*/
    public abstract String toString();

    /** Accessor function for time*/
    public long getTime() {
        return time_;
    }

    /** Perform logic which follows an event */
    public void followEvent(EventScheduler s, JSONObject response, GlobalController g) {
        if (engine_ != null) {
            engine_.followEvent(this, s, response, g);
        }

        // Non-engine specific follows go here
    }

    /** Perform logic which preceeds an event */
    public void preceedEvent(EventScheduler s, GlobalController g) {
        if (engine_ != null) {
            engine_.preceedEvent(this, s, g);
        }

        // Non-engine specific preceeding actions go here
    }

}
