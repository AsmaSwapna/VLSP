package usr.output;

import usr.globalcontroller.*;
import usr.logging.*;
import java.io.PrintStream;

/** This class produces output from the simulation */

public class OutputType {

    public static final int AT_TIME= 1;
    public static final int AT_START= 2;
    public static final int AT_INTERVAL= 3;
    public static final int AT_END= 4;

    Logger mylog= null;
    private String fileName_="";
    private OutputFunction outputType_= null;
    private boolean clear_= true;
    private int outputTimeType_ = 0;  // Repeated or at time?
    private int outputTime_= 0;   // Time parameter
    private boolean firstOutput_= true;
    String parameter_="";

    public OutputType() {
    }

    /** Set type from string*/
    public void setType(String t) throws java.lang.IllegalArgumentException {

        //  These names are defined for legacy reasons -- please use
        // class name in future
        if (t.equals("Network")) {
            outputType_= new OutputNetwork();
            return;
        }
        if (t.equals("Summary")) {
            outputType_= new OutputSummary();
            return;
        }
        if (t.equals("Traffic")) {
            outputType_= new OutputTraffic();
            return;
        }
        try {
            java.lang.Class <?> func;
            func=  java.lang.Class.forName(t);
        } catch (ClassCastException e) {
            throw new java.lang.IllegalArgumentException("Class name "+
                    t+" must be valid class name implementing OutputFunction");
        }
        catch (ClassNotFoundException e) {
            throw new java.lang.IllegalArgumentException("Class name "+t+" must be valid class name implementing OutputFunction");
        }
        throw new java.lang.IllegalArgumentException("Cannot parse Type "+t);
    }

    /** Accessor for output time type */
    public int getTimeType() {
        return outputTimeType_;
    }

    /** Set time type from string*/
    public void setTimeType(String tt) throws java.lang.IllegalArgumentException {
        if (tt.equals("Start")) {
            outputTimeType_= AT_START;
            return;
        }
        if (tt.equals("End")) {
            outputTimeType_= AT_END;
            return;
        }
        if (tt.equals("Interval")) {
            outputTimeType_= AT_INTERVAL;
            return;
        }
        if (tt.equals("Time")) {
            outputTimeType_= AT_TIME;
            return;
        }
        throw new java.lang.IllegalArgumentException("Cannot parse Time Type "+tt);
    }

    /** Clear output file at start of run */
    public boolean clearOutputFile() {
        return clear_;
    }

    /** Accessor for time parameter */
    public int getTime() {
        return outputTime_;
    }

    /** Setter for time */
    public void setTime(int t) {
        outputTime_= t;
    }

    /** Accessor for file name */
    public String getFileName ()
    {
        return fileName_;
    }

    /** Set file name */
    public void setFileName (String name)
    {
        fileName_= name;
    }


    /** Setter function for string parameter */
    public void setParameter(String parm)
    {
        parameter_= parm;
    }
    /** Accessor function for string parameter*/
    public String getParameter()
    {
        return parameter_;
    }


    /** Is this the first time output produced by this type*/
    public boolean isFirst() {
        return firstOutput_;
    }

    /** Set whether this is first time output produced by this type*/
    public void setFirst(boolean f) {
        firstOutput_= f;
    }
    
    /** Get output function */
    public OutputFunction getOutputClass() {
        return outputType_;
    }
    
    /** Create the required output */
    public void makeOutput(long time, PrintStream s, GlobalController gc) {
      outputType_.makeOutput(time, s, this, gc);
    }
 
    /**
     * To String
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append(outputType_.toString());

        builder.append(" ");

        switch (outputTimeType_) {
        case AT_START:
            builder.append("AT_START");
            break;
        case AT_END:
            builder.append("AT_END");
            break;
        case AT_INTERVAL:
            builder.append("AT_INTERVAL");
            break;
        case AT_TIME:
            builder.append("AT_TIME");
            break;
        default:
            break;
        }

        builder.append(" ");
        builder.append(outputTime_);
        builder.append(" "+parameter_);
        return builder.toString();
    }



}
