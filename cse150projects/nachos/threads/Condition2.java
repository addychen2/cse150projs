package nachos.threads;

import nachos.machine.*;

import nachos.threads.*;

import java.util.LinkedList;
 
/**
 * An implementation of condition variables that disables interrupt()s for
 * synchronization.
 * 
 * <p>
 * You must implement this.
 * 
 * @see nachos.threads.Condition
 */
public class Condition2 {
	//the main function 
	/**
	 * Allocate a new condition variable.
	 * 
	 * @param conditionLock the lock associated with this condition variable.
	 * The current thread must hold this lock whenever it uses <tt>sleep()</tt>,
	 * <tt>wake()</tt>, or <tt>wakeAll()</tt>.
	 */
	public Condition2(Lock conditionLock) {
		this.conditionLock = conditionLock; 
		waitQueue = new LinkedList<KThread>(); 
		//waitQueue = new LinkedList<KThread>(); //this is the same idea as Condition.java. We've make a linkedList data structue to 
		//function as the queue for stored threads
	}

	/**
	 * Atomically release the associated lock and go to sleep on this condition
	 * variable until another thread wakes it using <tt>wake()</tt>. The current
	 * thread must hold the associated lock. The thread will automatically
	 * reacquire the lock before <tt>sleep()</tt> returns.
	 */

	/* This puts the thread to sleep. When the thread is put to sleep, it will be deactivated. 
	Only threads which are awake can be put to sleep. 
	This is the critical section, because if any of the processes are interrupted, it wouldn't make sense
		-lock is released - it will not be able to sleep since the lock is not held by current thread
		-kthread added to waitQueue may be duplicated
		-kthread is put to sleep: meaning it waits until it is called again
	*/
	public void sleep() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread()); //double checks the that current thread is holding the the Lock 

		boolean intStatus = Machine.interrupt().disable(); //assign the status to be false, and disable interrupts in the proxy hardware
		conditionLock.release(); //release the Locks
		waitQueue.add(KThread.currentThread()); //
		KThread.sleep();
		Machine.interrupt().restore(intStatus);
		conditionLock.acquire(); //the thread will automatically obtain the lock after waking. 
	}

	/**
	 * Wake up at most one thread sleeping on this condition variable. The
	 * current thread must hold the associated lock.
	 */
	public void wake() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread()); //automatic check
		boolean intStatus = Machine.interrupt().disable(); //entering critical section 
		if (!waitQueue.isEmpty()) { //ensures the waitingQueue has space
			KThread thread = waitQueue.removeFirst(); //taking out the value from the waitingQueue
			thread.ready(); //adds the thread to the ready queue
		}
		Machine.interrupt().restore(intStatus);
	}

	/**
	 * Wake up all threads sleeping on this condition variable. The current
	 * thread must hold the associated lock.
	 */
	public void wakeAll() {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
		boolean intStatus = Machine.interrupt().disable();
		while (!waitQueue.isEmpty()) { //Same as waking a single thread, however there is a loop to wake multiple 
			KThread thread = waitQueue.removeFirst();
			thread.ready();
		}
		Machine.interrupt().restore(intStatus);
	}

        /**
	 * Atomically release the associated lock and go to sleep on
	 * this condition variable until either (1) another thread
	 * wakes it using <tt>wake()</tt>, or (2) the specified
	 * <i>timeout</i> elapses.  The current thread must hold the
	 * associated lock.  The thread will automatically reacquire
	 * the lock before <tt>sleep()</tt> returns.
	 */
    public void sleepFor(long timeout) {
		Lib.assertTrue(conditionLock.isHeldByCurrentThread());
	}

    private Lock conditionLock;

	private LinkedList<KThread> waitQueue; //declare it as a member variable in Condition2 class

	//Test Phase Begin 
    public static void cvTest5() {
        final Lock lock = new Lock();
        // final Condition empty = new Condition(lock);
        final Condition2 empty = new Condition2(lock);
        final LinkedList<Integer> list = new LinkedList<>();

        KThread consumer = new KThread( new Runnable () {
                public void run() {
                    lock.acquire();
                    while(list.isEmpty()){
                        empty.sleep();
                    }
                    Lib.assertTrue(list.size() == 5, "List should have 5 values.");
                    while(!list.isEmpty()) {
                        // context swith for the fun of it
                        KThread.currentThread().yield();
                        System.out.println("Removed " + list.removeFirst());
                    }
                    lock.release();
                }
            });

        KThread producer = new KThread( new Runnable () {
                public void run() {
                    lock.acquire();
                    for (int i = 0; i < 5; i++) {
                        list.add(i);
                        System.out.println("Added " + i);
                        // context swith for the fun of it
                        KThread.currentThread().yield();
                    }
                    empty.wake();
                    lock.release();
                }
            });

        consumer.setName("Consumer");
        producer.setName("Producer");
        consumer.fork();
        producer.fork();

        // We need to wait for the consumer and producer to finish,
        // and the proper way to do so is to join on them.  For this
        // to work, join must be implemented.  If you have not
        // implemented join yet, then comment out the calls to join
        // and instead uncomment the loop with yield; the loop has the
        // same effect, but is a kludgy way to do it.
        //consumer.join();
        //producer.join();
        for (int i = 0; i < 50; i++) { KThread.currentThread().yield(); }
    }

}