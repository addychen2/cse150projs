package nachos.threads;

import java.util.LinkedList;

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
}