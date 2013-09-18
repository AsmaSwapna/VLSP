package usr.globalcontroller;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import org.simpleframework.http.Path;
import org.simpleframework.http.Query;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import usr.common.LinkInfo;
import usr.events.EndLinkEvent;
import usr.events.SetLinkWeightEvent;
import usr.events.StartLinkEvent;
import cc.clayman.console.BasicRequestHandler;

/**
 * A class to handle /link/ requests
 */
public class LinkRestHandler extends BasicRequestHandler {
    // get GlobalController
    GlobalController controller_;

    public LinkRestHandler() {
    }

    /**
     * Handle a request and send a response.
     */
    public boolean  handle(Request request, Response response) {
        // get GlobalController
        controller_ = (GlobalController)getManagementConsole().getAssociated();

        try {
            /*
               System.out.println("method: " + request.getMethod());
               System.out.println("target: " + request.getTarget());
               System.out.println("path: " + request.getPath());
               System.out.println("directory: " + request.getPath().getDirectory());
               System.out.println("name: " + request.getPath().getName());
               System.out.println("segments: " + java.util.Arrays.asList(request.getPath().getSegments()));
               System.out.println("query: " + request.getQuery());
               System.out.println("keys: " + request.getQuery().keySet());
             */

            System.out.println("REQUEST: " + request.getMethod() + " " +  request.getTarget());

            long time = System.currentTimeMillis();

            response.set("Content-Type", "application/json");
            response.set("Server", "GlobalController/1.0 (SimpleFramework 4.0)");
            response.setDate("Date", time);
            response.setDate("Last-Modified", time);

            // get the path
            Path path = request.getPath();
            String directory = path.getDirectory();
            String name = path.getName();
            String[] segments = path.getSegments();

            // Get the method
            String method = request.getMethod();

            // Get the Query
            Query query = request.getQuery();

            // and evaluate the input
            if (method.equals("POST")) {
                if (name == null) {
                    // looks like a create
                    createLink(request, response);
                } else {
                    notFound(response, "POST bad request");
                }
            } else if (method.equals("DELETE")) {
                if (segments.length == 2) {
                    // looks like a delete
                    deleteLink(request, response);
                } else {
                    notFound(response, "DELETE bad request");
                }
            } else if (method.equals("GET")) {
                if (name == null) {      // no arg, so list links
                    listLinks(request, response);
                } else if (segments.length == 2) {   // get link info
                    getLinkInfo(request, response);
                } else {
                    notFound(response, "GET bad request");
                }
            } else if (method.equals("PUT")) {
                if (segments.length == 2) {   // set link attributes
                    setLinkAttributes(request, response);
                } else {
                    notFound(response, "PUT bad request");
                }

            } else {
                badRequest(response, "Unknown method" + method);
            }



            // check if the response is closed
            response.close();

            return true;

        } catch (IOException ioe) {
            System.err.println("IOException " + ioe.getMessage());
        } catch (JSONException jse) {
            System.err.println("JSONException " + jse.getMessage());
        }

        return false;

    }

    /**
     * Create link given a request and send a response.
     */
    public void createLink(Request request, Response response) throws IOException, JSONException {
        // Args:
        // router1
        // router2
        // [weight]
        // [linkName]


        int router1;
        int router2;
        int weight = 1;
        String linkName = "";
        Scanner scanner;

        Query query = request.getQuery();

        /* process compulsory args */

        // process arg router1
        if (query.containsKey("router1")) {
            scanner = new Scanner(query.get("router1"));

            if (scanner.hasNextInt()) {
                router1 = scanner.nextInt();
            } else {
                complain(response, "arg router1 is not an Integer");
                response.close();
                return;
            }
        } else {
            complain(response, "missing arg router1");
            response.close();
            return;
        }

        // process arg router2
        if (query.containsKey("router2")) {
            scanner = new Scanner(query.get("router2"));

            if (scanner.hasNextInt()) {
                router2 = scanner.nextInt();
            } else {
                complain(response, "arg router2 is not an Integer");
                response.close();
                return;
            }
        } else {
            complain(response, "missing arg router2");
            response.close();
            return;
        }

        /* process optional args */

        // process arg weight
        if (query.containsKey("weight")) {
            scanner = new Scanner(query.get("weight"));

            if (scanner.hasNextInt()) {
                weight = scanner.nextInt();
            } else {
                complain(response, "arg weight is not an Integer");
                response.close();
                return;
            }
        }

        if (query.containsKey("linkName")) {
            linkName = query.get("linkName");
        }

        /* do work */

        // start a link, and get its ID
        // WAS int linkID = controller_.startLink(System.currentTimeMillis(), router1, router2, weight, linkName);
        StartLinkEvent sle = new StartLinkEvent(controller_.getElapsedTime(), null, router1, router2);
        sle.setWeight(weight);

        if (linkName != null) {
            sle.setName(linkName);
        }

        boolean success = true;
        String failMessage = null;
        JSONObject jsobj = null;
        try {
            jsobj = controller_.executeEvent(sle);

            if (jsobj.get("success").equals(false)) {
                success = false;
                failMessage = (String)jsobj.get("msg");
            }
        } catch (InterruptedException ie) {
            success = false;
            failMessage = "Signal interrupted in global controller";
        } catch (InstantiationException ine) {
            success = false;
            failMessage = "Unexplained failure executing start Link";
        } catch (TimeoutException to) {
            success = false;
            failMessage = "Semaphore timeout in global controller -- too busy";
        }

        if (success) {
            PrintStream out = response.getPrintStream();
            out.println(jsobj.toString());
        } else {
            badRequest(response,
                       "Error creating link: " + failMessage);
        }


        /*
        if (linkID < 0) {
            // error
            complain(response, "Error creating link");

        } else if (linkID == 0) {
            // already exists
            complain(response, "Link already exists");

        } else {
            // now lookup all the saved link info details
            JSONObject jsobj = controller_.findLinkInfoAsJSON(linkID);


            out.println(jsobj.toString());

        }
        */
    }

    /**
     * Delete a link given a request and send a response.
     */
    public void deleteLink(Request request, Response response) throws IOException, JSONException {
        // if we got here we have 2 parts
        // /link/ and another bit
        String name = request.getPath().getName();
        Scanner sc = new Scanner(name);

        if (sc.hasNextInt()) {
            int id = sc.nextInt();

            int router1, router2;

            // if it exists, stop it, otherwise complain
            if (controller_.isValidLinkID(id)) {

                // now lookup all the saved link info details
                LinkInfo li = controller_.findLinkInfo(id);
                router1 = li.getEndPoints().getFirst();
                router2 = li.getEndPoints().getSecond();
            } else {
                badRequest(response, "deleteLink arg is not valid router id: " + name);
                return;
            }

            // delete a link
            // WAS controller_.endLink(System.currentTimeMillis(), (Integer)li.getEndPoints().getFirst(), (Integer)li.getEndPoints().getSecond());

            JSONObject jsobj = null;
            boolean success = true;
            String failMessage = null;
            EndLinkEvent ele = new EndLinkEvent(controller_.getElapsedTime(), null, router1, router2);
            try {
                jsobj = controller_.executeEvent(ele);

                if (jsobj.get("success").equals(false)) {
                    success = false;
                    failMessage = (String)jsobj.get("msg");
                }
            } catch (InterruptedException ie) {
                success = false;
                failMessage = "Signal interrupted in global controller";
            } catch (InstantiationException ine) {
                success = false;
                failMessage = "Unexplained failure executing end Link";
            } catch (TimeoutException to) {
                success = false;
                failMessage = "Semaphore timeout in global controller -- too busy";
            }

            if (success) {
                PrintStream out = response.getPrintStream();

                //jsobj.put("linkID", id);
                //jsobj.put("status", "deleted");


                out.println(jsobj.toString());
            } else {
                badRequest(response,
                           "Error destroying link: " + failMessage);
            }

        } else {
            complain(response, "deleteLink arg is not Integer: " + name);
        }
    }

    /**
     * Set the attributes on a link given a request and send a response.
     * Currently can only modify a link weight.
     */
    public void setLinkAttributes(Request request, Response response) throws IOException, JSONException {
        int weight = 1;
        Scanner scanner;

        Query query = request.getQuery();

        /* process compulsory args */

        // process arg weight
        if (query.containsKey("weight")) {
            scanner = new Scanner(query.get("weight"));

            if (scanner.hasNextInt()) {
                weight = scanner.nextInt();
            } else {
                complain(response, "arg weight is not an Integer");
                response.close();
                return;
            }
        } else {
            complain(response, "missing arg weight");
            response.close();
            return;
        }

        // /link/ and another bit
        String name = request.getPath().getName();
        Scanner sc = new Scanner(name);

        if (sc.hasNextInt()) {
            int id = sc.nextInt();

            int router1, router2;

            // if it exists, stop it, otherwise complain
            if (controller_.isValidLinkID(id)) {
                // now lookup all the saved link info details
                LinkInfo li = controller_.findLinkInfo(id);

                router1 = li.getEndPoints().getFirst();
                router2 = li.getEndPoints().getSecond();
            } else {
                complain(response, "setLinkWeight arg is not valid router id: " + name);
                return;
            }

                // set link weight
                /* TODO: convert to Event
                controller_.setLinkWeight(System.currentTimeMillis(), (Integer)li.getEndPoints().getFirst(),
                                    (Integer)li.getEndPoints().getSecond(), weight);
                */

            JSONObject jsobj = null;
            boolean success = true;
            String failMessage = null;
            SetLinkWeightEvent slwe = new SetLinkWeightEvent(controller_.getElapsedTime(), null, router1, router2, weight);
            try {
                jsobj = controller_.executeEvent(slwe);

                if (jsobj.get("success").equals(false)) {
                    success = false;
                    failMessage = (String)jsobj.get("msg");
                }
            } catch (InterruptedException ie) {
                success = false;
                failMessage = "Signal interrupted in global controller";
            } catch (InstantiationException ine) {
                success = false;
                failMessage = "Unexplained failure executing setLinkWeight";
            } catch (TimeoutException to) {
                success = false;
                failMessage = "Semaphore timeout in global controller -- too busy";
            }

            if (success) {
                PrintStream out = response.getPrintStream();

                // jsobj = controller_.findLinkInfoAsJSON(id);

                out.println(jsobj.toString());
            } else {
                badRequest(response, "Error set link weight: " + failMessage);
            }



        } else {
            complain(response, "setLinkWeight arg is not Integer: " + name);
        }
    }

    /**
     * List links given a request and send a response.
     */
    public void listLinks(Request request, Response response) throws IOException, JSONException {
        // process query

        Query query = request.getQuery();

        // the attribute we want about the link
        String detail;

        if (query.containsKey("detail")) {
            detail = query.get("detail");

            // check detail
            if (detail.equals("id") ||
                detail.equals("all")) {
                // fine
            } else {
                complain(response, "Bad detail: " + detail);
            }

        } else {
            detail = "id";
        }


        // and send them back as the return value
        PrintStream out = response.getPrintStream();

        JSONObject jsobj = controller_.getAllLinkInfoAsJSON(detail);

        out.println(jsobj.toString());

    }

    /**
     * Get info on a link given a request and send a response.
     */
    public void getLinkInfo(Request request, Response response) throws IOException, JSONException {
        // if we got here we have 2 parts
        // /link/ and another bit
        String name = request.getPath().getName();
        Scanner sc = new Scanner(name);

        if (sc.hasNextInt()) {
            int linkID = sc.nextInt();

            // if it exists, get data, otherwise complain
            if (!controller_.isValidLinkID(linkID)) {
                complain(response, " arg is not valid link id: " + name);
                response.close();
                return;
            }

            // and send them back as the return value
            PrintStream out = response.getPrintStream();


            JSONObject jsobj = controller_.findLinkInfoAsJSON(linkID);

            out.println(jsobj.toString());


        } else {
            // not an Integer
            if (name.equals("count")) {
                int count = controller_.getNoLinks();

                // and send them back as the return value
                PrintStream out = response.getPrintStream();

                JSONObject jsobj = new JSONObject();

                jsobj.put("value", count);

                out.println(jsobj.toString());

            } else {
                complain(response, "getLinkInfo arg is not appropriate: " + name);
            }

        }
    }

}
