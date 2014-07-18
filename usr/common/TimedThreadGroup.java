package usr.common;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.lang.management.ThreadInfo;

/**
 * A timed thread group keeps its own start time
 * so it is possible to work out the elapsed time
 * a ThreadGroup a run for.
 */
public class TimedThreadGroup extends ThreadGroup {
    // the start time - in millis
    long startTime;

    // ThreadMXBean
    ThreadMXBean mxBean;

    /**
     * Construct a TimedThreadGroup
     */
    public TimedThreadGroup(String name) {
        super(name);
        startTime = System.currentTimeMillis();

        mxBean = ManagementFactory.getThreadMXBean();
    }

    /**
     * Get the start time.
     * As milliseconds
     */
    public long getStartTime() {
        return startTime;
    }


    /**
     * Get the elapsed time.
     * In milliseconds.
     */
    public long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }


    /**
     * Get cpu usage info
     */
    public long[] getCpuUsage() {
        long cpu = 0;
        long user = 0;
        long sys = 0;

        // Get threads in `group'
        int numThreads = this.activeCount();
        Thread[] threads = new Thread[numThreads*2];
        numThreads = this.enumerate(threads, false);


        // Enumerate each thread in `group'
        for (int i = 0; i<numThreads; i++) {
            // Get thread
            TimedThread thread = (TimedThread)threads[i];

            long id = thread.getId();

            ThreadInfo info = mxBean.getThreadInfo(id);

            long threadCPU = mxBean.getThreadCpuTime(id);
            long threadUser = mxBean.getThreadUserTime(id);

            cpu += threadCPU;
            user += threadUser;
            sys += (threadCPU - threadUser);

        }

        long [] result =  { cpu, user, sys };
        return result;

    }

}
