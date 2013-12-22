package usr.events;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import usr.logging.Logger;
import usr.logging.USR;

/** Class represents a global controller event*/
public class StartSimulationEvent extends AbstractEvent {
    @Override
    public String toString() {
        String str;

        str = "StartSimulation: " + time_;
        return str;
    }

    public StartSimulationEvent(long time) {
        time_ = time;
    }

    @Override
    public JSONObject execute(EventDelegate ed) throws InstantiationException {
        ed.onEventSchedulerStart(time_);
        JSONObject json = new JSONObject();
        try {
            json.put("success", true);
            json.put("msg", "Simulation started");
        } catch (JSONException je) {
            Logger.getLogger("log").logln(USR.ERROR, "JSONException in StartLinkEvent should not occur");
        }

        return json;
    }

}
