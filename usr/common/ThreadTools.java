package usr.common;

public class ThreadTools {
    // can turn on printing with:
    // ThreadTools.on = true
    public static boolean on = false;

    public static void findAllThreads(String leadin) {
        if (!on) {
            return;
        } else {
            // Find the root thread group
            ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();
            while (root.getParent() != null) {
                root = root.getParent();
            }
        
            // Visit each thread group
            visit(root, 0, leadin);
        }
    }

    // This method recursively visits all thread groups under `group'.
    public static void visit(ThreadGroup group, int level, String leadin) {
        // Get threads in `group'
        int numThreads = group.activeCount();
        Thread[] threads = new Thread[numThreads*2];
        numThreads = group.enumerate(threads, false);

        // Enumerate each thread in `group'
        for (int i=0; i<numThreads; i++) {
            // Get thread
            Thread thread = threads[i];
            for (int sp=0; sp < level; sp++) {
                System.err.print("  ");
            }
            if (on) {
                System.err.println(leadin + " " + thread);
            }
        }

        // Get thread subgroups of `group'
        int numGroups = group.activeGroupCount();
        ThreadGroup[] groups = new ThreadGroup[numGroups*2];
        numGroups = group.enumerate(groups, false);

        // Recursively visit each subgroup
        for (int i=0; i<numGroups; i++) {
            visit(groups[i], level+1, leadin);
        }
    }
}
