package usr.events.globalcontroller;

import usr.events.Event;
import usr.events.EventDelegate;
import usr.events.EventScheduler;
import us.monoid.json.JSONObject;
import usr.APcontroller.APController;
import usr.engine.EventEngine;
import usr.globalcontroller.GlobalController;
import usr.logging.Logger;
import usr.logging.USR;

/** Class represents a global controller event*/
public class QueryAPEvent extends AbstractGlobalControllerEvent {
    APController apc_;

    public QueryAPEvent(long time, EventEngine eng, APController ap) {
        time_ = time;
        engine_ = eng;
        apc_ = ap;
    }

    @Override
    public String toString() {
        return new String("QueryAPController " + time_);
    }

    @Override
    public JSONObject execute(GlobalController gc) throws
        InstantiationException {
        apc_.controllerUpdate(time_, gc);
        JSONObject jsobj = new JSONObject();
        try {
            jsobj.put("success", (Boolean)true);
            jsobj.put("msg", "APEvent Queried");
        } catch (Exception e) {
            Logger.getLogger("log").logln(
                                          USR.ERROR,
                                          "JSONException in QueryAPEvent should not occur");
        }

        return jsobj;
    }

    /** Perform logic which follows an event */
    @Override
    public void followEvent(JSONObject response, GlobalController g) {
        super.followEvent(response, g);
        long newTime = time_ + g.getAPControllerConsiderTime();
        QueryAPEvent e = new QueryAPEvent(newTime, engine_, apc_);
        getEventScheduler().addEvent(e);
    }

}
