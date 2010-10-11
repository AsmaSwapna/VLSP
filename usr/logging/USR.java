// USR.java

package usr.logging;
import usr.logging.*;

/**
 * This holds some constants for use with logging.
 * <p>
 * In the calls to logger.log() we need to specify
 * which bits the value needs to log as.
 * In the logger.addOutput() we specify which bits
 * a particular output will accept.
 */
public class USR {
    // general stdout stuff
    public static int STDOUT = 1 << 0;

    // general error
    public static int ERROR = 1 << 1;
    
    // default output file
    public static int DEFAULT_FILE = 1 << 2;
}
