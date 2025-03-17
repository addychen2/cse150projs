package nachos.threads;

import java.util.LinkedList;
import nachos.machine.Lib;


/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>, and multiple
 * threads can be waiting to <i>listen</i>. But there should never be a time
 * when both a speaker and a listener are waiting, because the two threads can
 * be paired off at this point.
 */
public class Communicator {
		/**
		 * Allocate a new communicator.
		 */
		private nachos.threads.Lock communicatorLock;
		private nachos.threads.Condition speakerCondition;
		private nachos.threads.Condition listenerCondition;
		private boolean messageAvailable;
		private int currentMessage;

		public Communicator() {
				communicatorLock = new nachos.threads.Lock();
				speakerCondition = new nachos.threads.Condition(communicatorLock);
				listenerCondition = new nachos.threads.Condition(communicatorLock);
				messageAvailable = false;
		}

		/**
		 * Wait for a thread to listen through this communicator, and then transfer
		 * <i>word</i> to the listener.
		 * 
		 * <p>
		 * Does not return until this thread is paired up with a listening thread.
		 * Exactly one listener should receive <i>word</i>.
		 * 
		 * @param word the integer to transfer.
		 */
		public void speak(int word) {
				communicatorLock.acquire();

				while (messageAvailable) {
						speakerCondition.sleep();
				}
				currentMessage = word;
				messageAvailable = true;

				listenerCondition.wake();

				while (messageAvailable) {
						speakerCondition.sleep();
				}
				communicatorLock.release();
		}

		/**
		 * Wait for a thread to speak through this communicator, and then return the
		 * <i>word</i> that thread passed to <tt>speak()</tt>.
		 * 
		 * @return the integer transferred.
		 */
		public int listen() {
				communicatorLock.acquire();

				while (!messageAvailable) {
						listenerCondition.sleep();
				}
				int message = currentMessage;
				messageAvailable = false;

				speakerCondition.wake();

				communicatorLock.release();
				return message;
		}

    public static void selfTest() {
        CommSelfTester.selfTestAll();
    }
}

// usage: 1. copy this file to Communicator.java. It should be outside of communicator class.
//        2. To use selfTest1, copy "CommSelfTester.selfTest1();" into selfTest() function in ThreadedKernel.java.
//        3. run it.
class CommSelfTester {

		/**
		 * Run all tests one after another
		 */
		public static void selfTestAll() {
				System.out.println("===== Starting Communicator Test 1 =====");
				selfTest1();
				System.out.println("===== Test 1 Completed =====\n");

				System.out.println("===== Starting Communicator Test 2 =====");
				selfTest2();
				System.out.println("===== Test 2 Completed =====\n");

				System.out.println("===== Starting Communicator Test 3 =====");
				selfTest3();
				System.out.println("===== Test 3 Completed =====\n");

				System.out.println("===== Starting Communicator Test 4 =====");
				selfTest4();
				System.out.println("===== Test 4 Completed =====\n");

				System.out.println("===== Starting Communicator Test 5 (Small) =====");
				selfTest5Small();
				System.out.println("===== Test 5 (Small) Completed =====\n");
		}

	/**
	 * Test with 1 listener then 1 speaker.
	 */
	public static void selfTest1() {
		System.out.println("Creating and forking listener1...");
		KThread listener1 = new KThread(listenRun);
		listener1.setName("listener1");
		listener1.fork();

		System.out.println("Creating and forking speaker1...");
		KThread speaker1 = new KThread(speakerRun);
		speaker1.setName("speaker1");
		speaker1.fork();

				// Wait for both threads to complete
				listener1.join();
				speaker1.join();

				System.out.println("Test 1: Communication completed successfully!");
	}

	/**
	 * Test with 1 speaker then 1 listener.
	 */
	public static void selfTest2() {
		System.out.println("Creating and forking speaker1...");
		KThread speaker1 = new KThread(speakerRun);
		speaker1.setName("speaker1");
		speaker1.fork();

		System.out.println("Creating and forking listener1...");
		KThread listener1 = new KThread(listenRun);
		listener1.setName("listener1");
		listener1.fork();

				// Wait for both threads to complete
				speaker1.join();
				listener1.join();

				System.out.println("Test 2: Communication completed successfully!");
	}

	/**
	 * Test with 2 speakers and 2 listeners intermixed.
	 */
	public static void selfTest3() {
		System.out.println("Creating and forking speaker1...");
		KThread speaker1 = new KThread(speakerRun);
		speaker1.setName("speaker1");
		speaker1.fork();

		System.out.println("Creating and forking listener1...");
		KThread listener1 = new KThread(listenRun);
		listener1.setName("listener1");
		listener1.fork();

		System.out.println("Creating and forking speaker2...");
		KThread speaker2 = new KThread(speakerRun);
		speaker2.setName("speaker2");
		speaker2.fork();

		System.out.println("Creating and forking listener2...");
		KThread listener2 = new KThread(listenRun);
		listener2.setName("listener2");
		listener2.fork();

				// Wait for all threads to complete
				speaker1.join();
				listener1.join();
				speaker2.join();
				listener2.join();

				System.out.println("Test 3: Communication completed successfully!");
	}

	/**
	 * Second test with 2 speakers then 2 listeners.
	 */
	public static void selfTest4() {
		System.out.println("Creating and forking speaker1...");
		KThread speaker1 = new KThread(speakerRun);
		speaker1.setName("speaker1");
		speaker1.fork();

		System.out.println("Creating and forking speaker2...");
		KThread speaker2 = new KThread(speakerRun);
		speaker2.setName("speaker2");
		speaker2.fork();

		System.out.println("Creating and forking listener1...");
		KThread listener1 = new KThread(listenRun);
		listener1.setName("listener1");
		listener1.fork();

		System.out.println("Creating and forking listener2...");
		KThread listener2 = new KThread(listenRun);
		listener2.setName("listener2");
		listener2.fork();

				// Wait for all threads to complete
				speaker1.join();
				speaker2.join();
				listener1.join();
				listener2.join();

				System.out.println("Test 4: Communication completed successfully!");
	}

	/**
	 * Stress test with 100 speakers and 100 listeners intermixed.
	 */
	public static void selfTest5() {
				KThread[] speakers = new KThread[100];
				KThread[] listeners = new KThread[100];

		for (int i = 0; i < 100; i++) {
			speakers[i] = new KThread(speakerRun);
						speakers[i].setName("Speaker-" + i);
						speakers[i].fork();

			listeners[i] = new KThread(listenRun);
						listeners[i].setName("Listener-" + i);
						listeners[i].fork();
		}

				// Wait for all threads to complete
				for (int i = 0; i < 100; i++) {
						speakers[i].join();
						listeners[i].join();
				}

				System.out.println("Test 5: All 100 communications completed successfully!");
	}

		/**
		 * Smaller stress test with 5 speakers and 5 listeners
		 */
		public static void selfTest5Small() {
				KThread[] speakers = new KThread[5];
				KThread[] listeners = new KThread[5];

				for (int i = 0; i < 5; i++) {
						System.out.println("Creating and forking Speaker-" + i);
						speakers[i] = new KThread(speakerRun);
						speakers[i].setName("Speaker-" + i);
						speakers[i].fork();

						System.out.println("Creating and forking Listener-" + i);
						listeners[i] = new KThread(listenRun);
						listeners[i].setName("Listener-" + i);
						listeners[i].fork();
				}

				// Wait for all threads to complete
				for (int i = 0; i < 5; i++) {
						speakers[i].join();
						listeners[i].join();
				}

				System.out.println("Test 5 (Small): All 5 communications completed successfully!");
		}

	/**
	 * Function to run inside Runnable object listenRun. Uses the function listen on
	 * static object myComm inside this class, allowing the threads inside the
	 * respective selfTests above to call the runnable variables below and test
	 * functionality for listen. Needs to run with debug flags enabled. See NACHOS
	 * README for info on how to run in debug mode.
	 */
	static void listenFunction() {
		Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() + " is about to listen");
				System.out.println("Thread " + KThread.currentThread().getName() + " is about to listen");

		int value = myComm.listen();

		Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() + " got value " + value);
				System.out.println("Thread " + KThread.currentThread().getName() + " got value " + value);
	}

	/**
	 * Function to run inside Runnable object speakerRun. Uses the function listen
	 * on static object myComm inside this class, allowing the threads inside the
	 * respective selfTests above to call the runnable variables below and test
	 * functionality for speak. Needs to run with debug flags enabled. See NACHOS
	 * README for info on how to run in debug mode.
	 */
	static void speakFunction() {
				int value = myWordCount++;

		Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() + " is about to speak value " + value);
				System.out.println("Thread " + KThread.currentThread().getName() + " is about to speak value " + value);

		myComm.speak(value);

		Lib.debug(dbgThread, "Thread " + KThread.currentThread().getName() + " has spoken value " + (value));
				System.out.println("Thread " + KThread.currentThread().getName() + " has spoken value " + (value));
	}

	/**
	 * Wraps listenFunction inside a Runnable object so threads can be generated for
	 * testing.
	 */
	private static Runnable listenRun = new Runnable() {
		public void run() {
			listenFunction();
		}
	};

	/**
	 * Wraps speakFunction inside a Runnable object so threads can be generated for
	 * testing.
	 */
	private static Runnable speakerRun = new Runnable() {
		public void run() {
			speakFunction();
		}
	};

	// dbgThread = 't' variable needed for debug output
	private static final char dbgThread = 't';
	// myComm is a shared object that tests Communicator functionality
	private static Communicator myComm = new Communicator();
	// myWordCount is used for selfTest5 when spawning listening/speaking threads
	private static int myWordCount = 0;
}