package usr.events;

import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import usr.common.BasicRouterInfo;
import usr.engine.EventEngine;
import usr.globalcontroller.GlobalController;
import usr.logging.Logger;
import usr.logging.USR;

/** Class represents a global controller event*/
public class ListAppsEvent extends AbstractEvent {
    int router_;

    public ListAppsEvent(long time, EventEngine eng, int router) {
        time_ = time;
        engine_ = eng;
        router_ = router;
    }

    public String toString() {
        String str;

        str = "ListApps: " + time_ + " " + router_;
        return str;
    }

    public JSONObject execute(GlobalController gc)
    throws InstantiationException {
        JSONObject jsobj = new JSONObject();
        JSONArray array = new JSONArray();
        BasicRouterInfo bri = gc.findRouterInfo(router_);

        if (bri == null) {
            try {
                jsobj.put("success", false);
                jsobj.put("msg", "Cannot find router with ID " + router_);
            } catch (JSONException js) {
                Logger.getLogger("log").logln(
                    USR.ERROR,
                    "   JSONException in ListAppsEvent should not occur");
            }

            return jsobj;
        }

        for (Integer id : bri.getApplicationIDs()) {
            array.put(id);
        }

        try {
            jsobj.put("success", true);
            jsobj.put("type", "app");
            jsobj.put("msg", "Returning applications for router " +
                      router_);
            jsobj.put("list", array);
        } catch (JSONException js) {
            Logger.getLogger("log").logln(
                USR.ERROR,
                "JSONException in ListAppsEvent should not occur");
        }

        return jsobj;
    }

}