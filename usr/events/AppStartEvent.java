package usr.events;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import usr.common.BasicRouterInfo;
import usr.common.Pair;
import usr.engine.EventEngine;
import usr.globalcontroller.GlobalController;
import usr.interactor.LocalControllerInteractor;
import usr.logging.Logger;
import usr.logging.USR;

/** Class represents a global controller event*/
public class AppStartEvent extends AbstractEvent {
    int routerNo_ = 0;
    String className_ = null;
    String [] command_ = null;
    String address_ = null;
    boolean routerNumSet_ = true;

    public AppStartEvent(long time, EventEngine eng, int rNo, String cname, String [] args) {
        time_ = time;
        engine_ = eng;
        routerNo_ = rNo;
        className_ = cname;
        command_ = args;
    }

    public AppStartEvent(long time, EventEngine eng, String addr, String cname, String [] args) {
        time_ = time;
        engine_ = eng;
        className_ = cname;
        address_ = addr;
        command_ = args;
        routerNumSet_ = false;
    }

    public AppStartEvent(long time, EventEngine eng, String addr, String cname, String [] args, GlobalController gc) throws InstantiationException {
        time_ = time;
        engine_ = eng;
        className_ = cname;
        command_ = args;
        address_ = addr;
        setRouterNo(addr, gc);
    }

    @Override
	public String toString() {
        String str = "AppStart " + time_ + getName();

        return str;
    }

    private String getName() {
        String str = "";

        if (address_ == null) {
            str += (routerNo_ + " ");
        } else {
            str += (address_ + " ");
        }

        str += className_ + " Command:";

        for (String a : command_) {
            str += " " + a;
        }

        return str;
    }

    private void setRouterNo(String addr, GlobalController gc) throws InstantiationException {
        BasicRouterInfo rInfo = gc.findRouterInfo(addr);

        if (rInfo == null) {
            throw new InstantiationException("Cannot find address " + addr);
        }

        routerNo_ = rInfo.getId();
    }

    @Override
	public JSONObject execute(GlobalController gc) throws InstantiationException {
        if (!routerNumSet_) {
            setRouterNo(address_, gc);
        }


        JSONObject jsobj = appStart(routerNo_, className_, command_, gc);

        return jsobj;

    }


    protected JSONObject appStart(int routerID, String className, String[] args, GlobalController gc) {

        // Try and start the app
        int appID = appStartTry(routerNo_, className_, command_, gc);

        // register the app
        JSONObject json = new JSONObject();
        try {
            if (appID >= 0) {
                json.put("success", true);
                BasicRouterInfo bri = gc.findAppInfo(appID);
                String appName = bri.getAppName(appID);
                Map<String, Object> data = bri.getApplicationData(appName);
                json.put("id", appID);
                json.put("aid", data.get("aid"));
                json.put("name", appName);
                json.put("routerID", bri.getId());
                json.put("msg", "Started Application on router " + getName());
            } else {
                json.put("success", (Boolean)false);
                json.put("msg", "Unable to start application on router " + getName());
            }
        } catch (JSONException e) {
            Logger.getLogger("log").logln(
                USR.ERROR,
                "JSONException in CreateTrafficEvent should not occur");
        }

        return json;
    }

    /**
     * Run an application on a Router.
     * Returns the app ID
     */
    protected int appStartTry(int routerID, String className, String[] args, GlobalController gc) {
        // return +ve no for valid id
        // return -1 for no start - cant find LocalController
        BasicRouterInfo br = gc.findRouterInfo(routerID);

        if (br == null) {
            System.err.println ("Router "+routerID+" does not exist when trying to start app");
            return -1;
        }

        LocalControllerInteractor lci = gc.getLocalController(br);

        if (lci == null) {
            System.err.println ("LocalControllerInteractor does not exisit when trying to start app on Router "+routerID);
            return -1;
        }

        int i;
        int MAX_TRIES = 5;
        Integer appID = -1;

        for (i = 0; i < MAX_TRIES; i++) {
            try {
                // appStart returns a JSONObject
                // something like: {"aid":1,"startTime":1340614768099,
                // "name":"/R4/App/usr.applications.RecvDataRate/1"}

                JSONObject response = lci.appStart(routerID, className, args);

                // consturct an ID from the routerID and the appID
                Pair<Integer, Integer> idPair = new Pair<Integer, Integer>(routerID, (Integer)response.get("aid"));
                appID = idPair.hashCode();
                String appName = (String)response.get("name");

                // Add app to BasicRouterInfo
                br.addApplication(appID, appName);

                // and set info as
                // ["id": 46346535, "time" : "00:14:52", "aid" : 1,
                // "startime" : 1331119233159, "state": "RUNNING",
                // "classname" : "usr.applications.Send", "args" : "[4,
                // 3000, 250000, -d, 250, -i, 10]" ]
                Map<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put("time", "00:00:00");
                dataMap.put("id", appID);
                dataMap.put("aid", response.get("aid"));
                dataMap.put("startime", response.get("startTime"));
                dataMap.put("runtime", 0);
                dataMap.put("classname", className);
                dataMap.put("args", Arrays.asList(args).toString());
                dataMap.put("state", "STARTED");

                br.setApplicationData(appName, dataMap);

                gc.// register app info
                registerApp(appID, routerID);

                return appID;
            } catch (JSONException je) {
                Logger.getLogger("log").logln(USR.ERROR, leadin() + " failed to start app " + className + " on "
                                              + routerID + " try " + i + " with Exception " + je);
            } catch (IOException io) {
                Logger.getLogger("log").logln(USR.ERROR, leadin() + " failed to start app " + className + " on "
                                              + routerID + " try " + i + " with Exception " + io);
            }
        }

        Logger.getLogger("log").logln(USR.ERROR, leadin() + " failed to start app " + className
                                      + " on " + routerID + " giving up ");
        return -1;
    }

    private String leadin() {
        return "GC(AppSE):";
    }


}
