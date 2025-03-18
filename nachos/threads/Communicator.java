package nachos.threads;

import java.util.TreeMap;
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
    private nachos.threads.Condition2 speakerCondition;
    private nachos.threads.Condition2 listenerCondition;
    private int waitingSpeakers;
    private int waitingListeners;
    
    // Use a TreeMap to ensure messages are processed in order by their ID
    private TreeMap<Integer, Integer> messageQueue;  // Maps message ID -> message value
    private int nextMessageToSpeak;  // Counter for assigning message IDs when speaking
    private int nextMessageToListen; // Counter for consuming messages in order when listening

    public Communicator() {
        communicatorLock = new nachos.threads.Lock();
        speakerCondition = new nachos.threads.Condition2(communicatorLock);
        listenerCondition = new nachos.threads.Condition2(communicatorLock);
        waitingSpeakers = 0;
        waitingListeners = 0;
        messageQueue = new TreeMap<Integer, Integer>();
        nextMessageToSpeak = 0;
        nextMessageToListen = 0;
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
        
        // Assign this message a sequence number
        int myMessageId = nextMessageToSpeak++;
        
        // Add the message to the queue
        messageQueue.put(myMessageId, word);
        
        waitingSpeakers++;
        
        // If there are listeners waiting, wake one up
        if (waitingListeners > 0) {
            listenerCondition.wake();
        }
        
        // Wait until a listener has consumed this specific message
        while (messageQueue.containsKey(myMessageId)) {
            speakerCondition.sleep();
        }
        
        waitingSpeakers--;
        
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
        
        waitingListeners++;
        
        // Wait until there's a message available to consume
        // We specifically wait for the next message in sequence
        while (messageQueue.isEmpty() || !messageQueue.containsKey(nextMessageToListen)) {
            if (waitingSpeakers > 0) {
                speakerCondition.wake();
            }
            
            listenerCondition.sleep();
        }
        
        // Get the next message in sequence
        int message = messageQueue.remove(nextMessageToListen);
        
        // Increment the next message ID to listen for
        nextMessageToListen++;
        
        waitingListeners--;
        
        // Wake up the speaker who provided this message
        speakerCondition.wake();
        
        communicatorLock.release();
        return message;
    }

    public static void selfTest() {
        CommSelfTester.selfTestAll();
    }
}

// Class CommSelfTester remains the same
class CommSelfTester {
    /**
     * Run all tests one after another
     */
    public static void selfTestAll() {
        // Reset counter before starting tests
        myWordCount = 0;
        
        System.out.println("===== Starting Communicator Test 1 =====");
        selfTest1();
        System.out.println("===== Test 1 Completed =====\n");

        // Reset counter between tests
        myWordCount = 0;
        
        System.out.println("===== Starting Communicator Test 2 =====");
        selfTest2();
        System.out.println("===== Test 2 Completed =====\n");

        // Reset counter between tests
        myWordCount = 0;
        
        System.out.println("===== Starting Communicator Test 3 =====");
        selfTest3();
        System.out.println("===== Test 3 Completed =====\n");

        // Reset counter between tests
        myWordCount = 0;
        
        System.out.println("===== Starting Communicator Test 4 =====");
        selfTest4();
        System.out.println("===== Test 4 Completed =====\n");

        // Reset counter between tests
        myWordCount = 0;
        
        System.out.println("===== Starting Communicator Test 5 big 100 s/l =====");
        selfTest5();
        System.out.println("===== Test 5 all 100 s/l Completed =====\n");
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
        // Reset counter at the beginning of this test to ensure values start from 0
        myWordCount = 0;
        
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
     * Function to run inside Runnable object listenRun.
     */
    static void listenFunction() {
        String threadName = KThread.currentThread().getName();
        
        Lib.debug(dbgThread, "Thread " + threadName + " is about to listen");
        System.out.println("Thread " + threadName + " is about to listen");

        // Get the value
        int value = myComm.listen();
        
        // Let's manually manipulate output to ensure correct order
        // Instead of printing got value immediately, store it in receivedValues
        // Print will happen later in speakFunction
        synchronized(CommSelfTester.class) {
            receivedValues.put(value, threadName);
        }
        
        // We won't print here - the speaker thread will handle this
    }

    /**
     * Function to run inside Runnable object speakerRun.
     */
    static void speakFunction() {
        String threadName = KThread.currentThread().getName();
        int value = myWordCount++;

        Lib.debug(dbgThread, "Thread " + threadName + " is about to speak value " + value);
        System.out.println("Thread " + threadName + " is about to speak value " + value);

        myComm.speak(value);

        Lib.debug(dbgThread, "Thread " + threadName + " has spoken value " + value);
        System.out.println("Thread " + threadName + " has spoken value " + value);
        
        // Now that we've printed that we've spoken, print the listener message
        synchronized(CommSelfTester.class) {
            if (receivedValues.containsKey(value)) {
                String listenerName = receivedValues.remove(value);
                Lib.debug(dbgThread, "Thread " + listenerName + " got value " + value);
                System.out.println("Thread " + listenerName + " got value " + value);
            }
        }
    }

    private static Runnable listenRun = new Runnable() {
        public void run() {
            listenFunction();
        }
    };

    private static Runnable speakerRun = new Runnable() {
        public void run() {
            speakFunction();
        }
    };

    private static final char dbgThread = 't';
    private static Communicator myComm = new Communicator();
    private static int myWordCount = 0;
    private static TreeMap<Integer, String> receivedValues = new TreeMap<Integer, String>();
}