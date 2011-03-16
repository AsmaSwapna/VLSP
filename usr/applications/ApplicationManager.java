package usr.applications;

import usr.logging.*;
import usr.router.*;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;
import java.util.concurrent.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * The ApplicationManager is reponsible for starting and stopping
 * applications.
 */
public class ApplicationManager {
    // A static instance
    private final static ApplicationManager singleton = new ApplicationManager();
    // The router
    Router router;

    // A pool of Executors
    //ThreadPoolExecutor pool;
    ApplicationThreadManager pool;

    // A map of all the Applications, name -> ApplicationHandle object
    HashMap<String, ApplicationHandle> appMap;

    /**
     * ApplicationManager Constructor.
     */
    public ApplicationManager() {
        // create an Executor pool 
        //WAS pool =  new ApplicationThreadPoolExecutor(this);
        pool = new ApplicationThreadManager(this);

        appMap = new HashMap<String, ApplicationHandle>();
        router = RouterDirectory.getRouter();
    }

    /**
     * Execute an object with ClassName and args
     */
    synchronized ApplicationResponse execute(String className, String[] args) {

        Logger.getLogger("log").logln(USR.STDOUT, leadin() + "execute: " + className + " args: " + Arrays.asList(args));

        // Class
        Class<?> clazz;
        Constructor<? extends Application> cons0;

        try { 
            // get Class object
            clazz = (Class<?>)Class.forName(className);

            // check if the class implements the right interface
            // it is an Application
            try {
                Class<? extends Application> appClazz = clazz.asSubclass(Application.class);
                cons0 = (Constructor<? extends Application>)appClazz.getDeclaredConstructor();
            } catch (ClassCastException cce) {
                // it is not an Application, so we cant run it
                Logger.getLogger("log").logln(USR.ERROR, leadin() + "class " + className + " is not at Application"); 
                return new ApplicationResponse(false, "class " + className + " is not at Application"); 
            }

            // create an instance of the Application
            Application app =  (Application)cons0.newInstance();

            // set app name
            String appName = "/" + router.getName() + "/App/" + className + "/" + app.hashCode();

            // initialize it
            ApplicationResponse initR = app.init(args);

            // if init fails, return
            if (!initR.isSuccess()) {
                return initR;
            }

            // otherwise create an ApplicationHandle for the app
            ApplicationHandle handle = new ApplicationHandle(appName, app);

            // try and start the app
            ApplicationResponse startR;

            synchronized (app) {
                startR = app.start();
            }

            // if start succeeded then go onto run()
            if (startR.isSuccess()) {
                // now add details to Application map
                appMap.put(appName, handle);

                handle.setState(ApplicationHandle.AppState.RUNNING);

                pool.execute(handle);

                // Logger.getLogger("log").logln(USR.ERROR, leadin() + "pool = " + pool.getActiveCount() + "/" + pool.getTaskCount() + "/" + pool.getPoolSize());

                return new ApplicationResponse(true, appName);

            } else {
                // the app did not start properly
                handle.setState(ApplicationHandle.AppState.STOPPED);

                return startR;
            }

        } catch (ClassNotFoundException cnfe) {
            Logger.getLogger("log").logln(USR.ERROR, leadin() + "ClassNotFoundException " + cnfe); 
            return new ApplicationResponse(false, "ClassNotFoundException " + cnfe); 

        } catch (NoClassDefFoundError ncdfe) {
            Logger.getLogger("log").logln(USR.ERROR, leadin() + "NoClassDefFoundError " + ncdfe); 
            return new ApplicationResponse(false, "NoClassDefFoundError " + ncdfe); 

        } catch (NoSuchMethodException nsme) {
            Logger.getLogger("log").logln(USR.ERROR, leadin() + "NoSuchMethodException " + nsme); 
            return new ApplicationResponse(false,  "NoSuchMethodException " + nsme); 

        } catch (InstantiationException ie) {
            Logger.getLogger("log").logln(USR.ERROR, leadin() + "InstantiationException " + ie); 
            return new ApplicationResponse(false,  "InstantiationException " + ie); 

        } catch (IllegalAccessException iae) {
            Logger.getLogger("log").logln(USR.ERROR, leadin() + "IllegalAccessException " + iae); 
            return new ApplicationResponse(false,  "IllegalAccessException " + iae); 

        } catch (InvocationTargetException ite) {
            Logger.getLogger("log").logln(USR.ERROR, leadin() + "InvocationTargetException " + ite); 
            return new ApplicationResponse(false,  "InvocationTargetException " + ite); 

        }

    }

    /**
     * Stop an Application
     */
    ApplicationResponse terminate(String appName) {
        ApplicationHandle appH = appMap.get(appName);
        if (appH == null) {
            // no app with that name
            return new ApplicationResponse(false, "No Application called " + appName);
        } else {
            synchronized(appH) {   // This must not be called twice for the same appH simultaneously


                if (appH.getState() == ApplicationHandle.AppState.STOPPED) {

                    // wait for the thread to actually end
                    //pool.waitFor(appH);

                    // and remove from the app map
                    appMap.remove(appName);

                    return new ApplicationResponse(false, "Application called " + appName + " already stopped");
                } else {               

                    try {

                        Application app = appH.getApplication();

                        Logger.getLogger("log").logln(USR.STDOUT, leadin() + "stopping " + appName);
                    

                        if (appH.getState() == ApplicationHandle.AppState.RUNNING) {
                            appH.setState(ApplicationHandle.AppState.STOPPING);
                            synchronized (app) {
                                app.stop();
                            }


                            // wait for the thread to actually end
                            pool.waitFor(appH);
                        } else  if (appH.getState() == ApplicationHandle.AppState.APP_POST_RUN) {
                            // the app had already exited the run loop
                            //Logger.getLogger("log").logln(USR.STDOUT, leadin() + "Cleanup app after exiting run() " + appName);
                        }

                        appH.setState(ApplicationHandle.AppState.STOPPED);

                        // and remove from the app map
                        appMap.remove(appName);

                        return new ApplicationResponse(true, "");
                    } catch (Exception e) {
                        return new ApplicationResponse(false, "Application called " + appName + " failed to stop with Exception " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Shutdown the ApplicationManager
     */
    synchronized void shutdown() {
        Logger.getLogger("log").logln(USR.STDOUT, leadin() + "shutdown ");

        Collection<ApplicationHandle> apps = new java.util.LinkedList<ApplicationHandle>(appMap.values());
        
        for (ApplicationHandle appH : apps) {
            Logger.getLogger("log").logln(USR.STDOUT, leadin() + "Attempting to terminate "+appH); 
            terminate(appH.getName());
        }

        //Logger.getLogger("log").logln(USR.STDOUT, leadin() + "pool shutdown ");
        //pool.shutdown();
    }

    /**
     * Static entry point to start an Application.
     */
    public synchronized static ApplicationResponse startApp(String className, String[] args) {
        // args should be class name + args for class

        ApplicationResponse result = singleton.execute(className, args);

        return result;
    }

    /**
     * Static entry point to stop an Application.
     * Application name is passed in.
     */
    public synchronized static ApplicationResponse stopApp(String appName) {
        return singleton.terminate(appName);
    }

    /**
     * Static entry point to stop all Application.
     * Application name is passed in.
     */
    public synchronized static void stopAll() {
        //System.err.println("Stopping all apps");
        singleton.shutdown();
        //System.err.println("Stopped all apps");
    }

    /**
     * Static entry point to list Applications.
     */
    public synchronized static Collection<ApplicationHandle> listApps() {
        return singleton.appMap.values();
    }

    String leadin() {
        final String AM = "AppM: ";

        return router.getName() + " " + AM;
    }



}
