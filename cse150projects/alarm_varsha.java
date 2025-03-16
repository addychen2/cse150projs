package nachos.threads;

import nachos.machine.*;
import java.util.PriorityQueue;

/**
 * Uses the hardware timer to provide preemption and allows threads to sleep
 * until a certain time.
 */
public class Alarm {

    // Priority queue to store sleeping threads, ordered by wake-up time
    private PriorityQueue<WaitingThread> waitQueue = new PriorityQueue<>();

    public Alarm() {
        // Set the timer interrupt handler to call timerInterrupt() periodically
        Machine.timer().setInterruptHandler(new Runnable() {
            public void run() {
                timerInterrupt();
            }
        });
    }

    /**
     * The timer interrupt handler. This is called periodically by the hardware timer.
     * It checks for threads whose wait time has expired and wakes them up.
     */
    public void timerInterrupt() {
        boolean intStatus = Machine.interrupt().disable(); // alwasy to disable interrupts
        long currentTime = Machine.timer().getTime();

        // Wake up all threads whose wake time has expired
        while (!waitQueue.isEmpty() && waitQueue.peek().wakeTime <= currentTime) {
            WaitingThread waitingThread = waitQueue.poll();
            waitingThread.thread.ready(); // you move the thread to the ready queue 
        }

        Machine.interrupt().restore(intStatus); // you need to restore interrupts
        KThread.yield(); // allows all threads to run 
    }

    /**
     * Put the current thread to sleep for at least x ticks, waking it up
     * in the timer interrupt handler.
     * 
     * The minimum number of clock ticks to wait.
     */
    public void waitUntil(long x) {
        if (x <=  0) return; // return if x is 0 or negative. 

        long wakeTime = Machine.timer().getTime() + x; //x needs counter variable 

        boolean intStatus = Machine.interrupt().disable(); // will have to disable interrupts

        WaitingThread waitRecord = new WaitingThread(KThread.currentThread(), wakeTime);
        waitQueue.add(waitRecord); // now adding 

        KThread.sleep(); // Put one thread to sleep

        Machine.interrupt().restore(intStatus); // Restore all the interrupts
    }

    /**
     * Helper class to store waiting threads.
     */
    private class WaitingThread implements Comparable<WaitingThread> {
        KThread thread;
        long wakeTime;

        WaitingThread(KThread thread, long wakeTime) {
            this.thread = thread;
            this.wakeTime = wakeTime;
        }

        @Override //is overwriting the method
        public int compareTo(WaitingThread other) {
            return Long.compare(this.wakeTime, other.wakeTime); 
        }
    }
}


/*long wakeTime = Machine.timer().getTime() + x;
if (x <= 0) { 
return; // Do not sleep if x is zero or negative 
}
boolean intStatus = Machine.interrupt().disable();


Thread Queueing
 Copy
WaitingThread waitRecord = new WaitingThread(
    KThread.currentThread(),
    wakeTime,
    ThreadState.saveState()
);
waitQueue.add(waitRecord);
KThread.currentThread().sleep();


Timer Interrupt Handling
Wake-up Processing
 Copy
boolean intStatus = Machine.interrupt().disable();
long currentTime = Machine.timer().getTime();

while (!waitQueue.isEmpty() && 
       waitQueue.peek().wakeTime <= currentTime) {
    WaitingThread thread = waitQueue.poll();
    thread.thread.ready();
}

rough code for understanding purposes
*/
