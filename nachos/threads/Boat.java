package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat {
	//Test cases: 
	public static void selfTest() { 
		BoatGrader b = new BoatGrader();

		
		System.out.println("\n ***Testing Boats with only 2 children***");
		begin(0, 2, b);

		System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		begin(1, 2, b);

		System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		begin(3, 3, b);
    }

	//Initializing variables
    static BoatGrader bg;

    private static int childrenOnOahu;
    private static int childrenOnMolokai;
    private static int adultsOnOahu;
    private static int adultsOnMolokai;
    private static int childrenReady;
    private static int boatPassengers;

    private static boolean boatAtOahu;

	//Locks and conditions to ensure that only people on a particular island can board the boat
    private static Lock oahuLock = new Lock();
    private static Lock molokaiLock = new Lock();

	//Conditions to make thread wait until certain conditions are met. So long as conditions aren't met, thread waits to prevent race conditions
    private static Condition adultsWaiting = new Condition(oahuLock); //Adults waiting until all children have crossed
    private static Condition childWaitingOnMolokai = new Condition(molokaiLock); //Children has crossed and must signal to other children
    private static Condition childWaitingOnOahu = new Condition(oahuLock); //Children waitting to cross, 
    private static Condition childBoarding = new Condition(oahuLock); //Children waiting to board the boat

    private static Semaphore doneSem = new Semaphore(0); //Semaphore to signal that all threads have finished, no thread can proceed until another thread releases it

    public static void begin(int adults, int children, BoatGrader b) {
        bg = b;
        childrenOnOahu = children;
        childrenOnMolokai = 0;
        adultsOnOahu = adults;
        adultsOnMolokai = 0;
        childrenReady = 0;
        boatPassengers = 0;
        boatAtOahu = true;

		//executing threads for children and adults
        for (int i = 0; i < children; i++) {
            new KThread(() -> ChildItinerary()).setName("Child " + i).fork();
        }

        for (int i = 0; i < adults; i++) {
            new KThread(() -> AdultItinerary()).setName("Adult " + i).fork();
        }
        doneSem.P(); //no more threads
    }

    static void AdultItinerary() {
        oahuLock.acquire();
        while (childrenOnOahu > 1 || !boatAtOahu) { //Adults will wait to cross until there is only 1 or 0 children on the island
            adultsWaiting.sleep(); //automatically release the lock 
        }
		//Once awakened by boat adult regains lock & crosses
        adultsOnOahu--;
        boatAtOahu = false;
        oahuLock.release();

        bg.AdultRowToMolokai();

        molokaiLock.acquire();
        adultsOnMolokai++;
        childWaitingOnMolokai.wake(); //wake the first child waiting on Molokai to row back, since a child must always row back
        molokaiLock.release();
    }

    static void ChildItinerary() {
        while (childrenOnOahu + adultsOnOahu > 1) { //loop until only 1 person left 
            oahuLock.acquire();
            if (childrenOnOahu == 1) { //1 child left, they can take an adult
                adultsWaiting.wake();
            }
            while (childrenReady >= 2 || !boatAtOahu) { //Children wait for boat to cross
                childWaitingOnOahu.sleep();
            }
            if (childrenReady == 0) {  //If no children in line, one wakes themselves up
                childrenReady++;
                childWaitingOnOahu.wake();
                childBoarding.sleep();
                bg.ChildRideToMolokai();
                childBoarding.wake();
            } else {
                childrenReady++;
                childBoarding.wake();
                bg.ChildRowToMolokai();
                childBoarding.sleep();
            }
            childrenReady--;
            childrenOnOahu--;
            boatAtOahu = false;
            oahuLock.release();

            molokaiLock.acquire();
            childrenOnMolokai++;
            boatPassengers++;

			boolean everyoneOnMolokai = false;
			oahuLock.acquire();
			if (childrenOnOahu == 0 && adultsOnOahu == 0) {
				everyoneOnMolokai = true;
			}
			oahuLock.release();

			if (everyoneOnMolokai) {
				if (boatPassengers == 1) {
					childWaitingOnMolokai.wake();
				}
				molokaiLock.release();
				doneSem.V();
				return;
			}
			if (boatPassengers == 1) {
				childWaitingOnMolokai.sleep();
			}
			childrenOnMolokai--;
			boatPassengers = 0;
			molokaiLock.release();
			
			bg.ChildRowToOahu();
			oahuLock.acquire();
			childrenOnOahu++;
			boatAtOahu = true;
			oahuLock.release();
        }

		//Final person must always be a child which is guaranteed, since adults will always sail over if there is only 1 child,
        //and the children sail over if there are adults.
		oahuLock.acquire();
		childrenOnOahu--;
		oahuLock.release();
		bg.ChildRowToMolokai();
		molokaiLock.acquire();
		childrenOnMolokai++;
		molokaiLock.release();
		doneSem.V();
    }
}
