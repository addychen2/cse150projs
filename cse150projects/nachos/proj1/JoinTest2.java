private static class PingTest implements Runnable {
    PingTest(int which) {
        this.which = which;
    }

    public void run() {
        for (int i=0; i<50000; i++) { // You may adjust the number of iterations
            System.out.println("*** thread " + which + " looped " + i + " times");
            currentThread.yield();
        }
        //ThreadedKernel.alarm.waitUntil(2000);
    }

    private int which;
}

/**
* Tests whether this module is working.
*/
public static void selfTest() {
    Lib.debug(dbgThread, "Enter KThread.selfTest");
    /**
     * Allocate a new thread setting the target to point to a 
     * PingTest object whose run method will be called when the 
     * newly created thread executes. 
     */
    KThread th1 = new KThread(new PingTest(1));
    /**
     * Set the name of the new thread to "forked thread 1" 
     */
    th1.setName("forked thread 1");
    /**
     * Execute fork() to begin execution of the new thread 
     * (putting it in the ready queue). This new thread will 
     * eventually execute the PingTest object's run() method when
     * scheduled. The current thread (that created this new thread) 
     * will return from the fork() method call resuming its own 
     * execution, thus, giving us 2 concurrent thread.
     */
    th1.fork();
    /**
     * The current thread that returned from fork() (creating the 
     * new thread th1 above) will then create its own PingTest 
     * object and execute the run method. So now we have two 
     * threads running the PingTest's run() method.
     */
    new PingTest(0).run();
    /**
     * Current thread calls join() on the newly created thread 
     * thus going to sleep till th1 finishes.
     */
    th1.join(); 
    /**
     * Try to join with th1 again. This should immediately return
     * as th1 should have already finished.
     */
    th1.join();
}
