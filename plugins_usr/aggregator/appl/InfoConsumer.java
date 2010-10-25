// InfoConsumer.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Oct 2010

package plugins_usr.aggregator.appl;

import usr.applications.*;
import usr.router.Router;
import usr.net.*;
import usr.logging.*;
import usr.net.GIDAddress;
import plugins_usr.monitoring.distribution.USRDataPlaneConsumerWithNames;
import eu.reservoir.monitoring.core.*;
import eu.reservoir.monitoring.core.plane.DataPlane;
import eu.reservoir.monitoring.distribution.*;
import java.util.Scanner;
import java.util.Properties;
import java.io.File;
import java.io.Serializable;
import com.timeindexing.index.*;
import com.timeindexing.time.MillisecondTimestamp;
import com.timeindexing.data.SerializableItem;

/**
 * An InfoConsumer that can receive measurements.
 *
 * Has args:
 * input addr
 * logpath - the path to log into
 */
public class InfoConsumer implements Reporter, Application {
    // The address for input
    SocketAddress inputDataAddress;

    /*
     * The receiver for the data domain.
     * This receives measurements.
     */
    DataConsumer dataDomain;

    // The name
    String name = "info-consumer";

    // The place to store the collected data
    String collectorPath = "/tmp/";

    /*
     * The Time Index that holds the sent data
     */
    IndexView dataIndex;


    /**
     * InfoConsumer constructor.
     */
    public InfoConsumer() {
    }

    /**
     * init
     * Args are:
     * -i input address
     * -l log path, (default: /tmp/)
     * -n name (default: "info-consumer")
     */
    public ApplicationResponse init(String[] args) {
	// process args
	int argc = args.length;

	for (int arg=0; arg < argc; arg++) {
	    String thisArg = args[arg];

	    // check if its a flag
	    if (thisArg.charAt(0) == '-') {
		// get option
		char option = thisArg.charAt(1);

		// gwet next arg
		String argValue = args[++arg];

		switch (option) {

		case 'i': {
		    String[] parts = argValue.split("/");
		    Scanner sc = new Scanner(parts[0]);
		    int addr = sc.nextInt();
                    sc = new Scanner(parts[1]);
		    int port = sc.nextInt();
                    Address gidAddr = new GIDAddress(addr);

                    SocketAddress newInputAddr = new SocketAddress(gidAddr, port);
		    setInputAddress(newInputAddr);
		    break;
		}

		case 'l': {
		    // assume a file name
		    File potentialPath = new File(argValue);
		    // check if directory part exists
		    if (potentialPath.isDirectory() && potentialPath.canWrite()) {
			setCollectionPath(argValue);
		    } else {
			Logger.getLogger("log").logln(USR.ERROR, "InfoConsumer: cannot write file in directory " + argValue);
			System.exit(1);
		    }
		    break;
		}

		case 'n': {
		    setName(argValue);
		    break;
		}



		default:
		    Logger.getLogger("log").logln(USR.ERROR, "InfoConsumer: unknown option " + option);
		    break;
		}
		
	    }
	}

        // check inputDataAddress
        if (inputDataAddress == null) {
	    return new ApplicationResponse(false, "No Input Address has been set");
        }


        return new ApplicationResponse(true, "");
    }

    /**
     * Start the InfoConsumer.
     */
    public ApplicationResponse start() {
        File dataIndexPath = null;

	try {
	    // create a TimeIndexFactory
	    TimeIndexFactory factory = new TimeIndexFactory();
	    Properties indexProperties = new Properties();

	    String realName;

	    // create forwardIndex
	    realName = name+"-log";
	    dataIndexPath = new File(collectorPath, realName);
	    indexProperties.setProperty("indexpath",  dataIndexPath.getPath());
	    indexProperties.setProperty("name", realName);

	    dataIndex = factory.create(IndexType.EXTERNAL, indexProperties);
            dataIndex.setAutoCommit(true);
	} catch (TimeIndexException tie) {
	    tie.printStackTrace();
	    return new ApplicationResponse(false, "Cannot create TimeIndex " + dataIndexPath) ;
	}

        try {
            // Set up the data listener
            // this is the handler
            dataDomain = new DataConsumer(this);

            Logger.getLogger("log").logln(USR.ERROR, "InfoConsumer connect to " + inputDataAddress);

            DataPlane inputDataPlane = new USRDataPlaneConsumerWithNames(inputDataAddress);

            dataDomain.setDataPlane(inputDataPlane);

            dataDomain.connect();
    
            return new ApplicationResponse(true, "");
    } catch (Exception e) {
            return new ApplicationResponse(false, e.getMessage());
        }

    }

    /**
     * Stop
     */
    public ApplicationResponse stop() {
        dataDomain.disconnect();

        synchronized (this) {
            notifyAll();
        }

        return new ApplicationResponse(true, "");

    }

    /**
     * Run
     */
    public void run() {
        // A DataConsumer already runs in itws own thread
        // so this one can wait and do nothing.
        try {
            synchronized (this) {
                wait();
            }
        } catch (InterruptedException ie) {
        }
    }



    /**
     * Receiver of a measurment.
     */
    public void report(Measurement m) {
	Logger.getLogger("log").logln(USR.STDOUT, m.toString());

	try {
	    Serializable object = (Serializable)m;
	    dataIndex.addItem(new SerializableItem(object), new MillisecondTimestamp());
	} catch (TimeIndexException tie) {
	    Logger.getLogger("log").logln(USR.ERROR, "Can't add data to time index log " + dataIndex.getName());
	}
    }


    /**
     * Get the  address for inpput traffic.
     */
    public SocketAddress getInputAddress() {
	return inputDataAddress;
    }

    /**
     * Set the  address for input traffic.
     */
    public SocketAddress setInputAddress(SocketAddress in) {
	SocketAddress old = inputDataAddress;
	inputDataAddress = in;
	return old;
    }

    /**
     * Get the path where raw data is collected into.
     */
    public String getCollectionPath() {
	return collectorPath;
    }

    /**
     * Set the path where raw data is collected into.
     */
    public String setCollectionPath(String path) {
	String old = collectorPath;
	collectorPath = path;
	return old;
    }

    /**
     * Get the name of the AggPoint.
     */
    public String getName() {
	return name;
    }

    /**
     * Set the name of the AggPoint.
     */
    public String setName(String str) {
	String old = name;
	name = str;
	return old;
    }


    /**
     * Simple DataConsumer
     */
    class DataConsumer extends AbstractDataConsumer  {

        /**
         * Construct a BasicConsumer.
         */
        public DataConsumer(InfoConsumer info) {
            addReporter(info);
        }

    }

}
