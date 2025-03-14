    // Place Join test code in the KThread class and invoke test methods
    // from KThread.selfTest().
    
    // Simple test for the situation where the child finishes before
    // the parent calls join on it.
    
    private static void joinTest1 () {
	KThread child1 = new KThread( new Runnable () {
		public void run() {
		    System.out.println("I (heart) Nachos!");
		}
	    });
	child1.setName("child1").fork();

	// We want the child to finish before we call join.  Although
	// our solutions to the problems cannot busy wait, our test
	// programs can!

	for (int i = 0; i < 5; i++) {
	    System.out.println ("busy...");
	    KThread.currentThread().yield();
	}

	child1.join();
	System.out.println("After joining, child1 should be finished.");
	System.out.println("is it? " + (child1.status == statusFinished));
	Lib.assertTrue((child1.status == statusFinished), " Expected child1 to be finished.");
    }
