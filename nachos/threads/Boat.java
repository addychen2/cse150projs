package nachos.threads;

import nachos.ag.BoatGrader;

public class Boat {
	static BoatGrader bg;

	public static void selfTest() {
		BoatGrader b = new BoatGrader();

		System.out.println("\n ***Testing Boats with only 2 children***");
		begin(0, 2, b);

		// System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
		// begin(1, 2, b);

		// System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
		// begin(3, 3, b);
	}

	private static Lock boatLock;
	private static Condition2 adultCondition;
	private static Condition2 childCondition;
	private static int adultsOnOahu;
	private static int childrenOnOahu;
	private static boolean boatOnOahu;
	private static boolean pilotReady;

	public static void begin(int adults, int children, BoatGrader b) {
		bg = b;

		boatLock = new Lock();
		adultCondition = new Condition2(boatLock);
		childCondition = new Condition2(boatLock);
		adultsOnOahu = adults;
		childrenOnOahu = children;
		boatOnOahu = true;
		pilotReady = false;

		for (int i = 0; i < adults; i++) {
			KThread t = new KThread(new Runnable() {
				public void run() {
					AdultItinerary();
				}
			});
			t.setName("Adult " + i);
			t.fork();
		}

		for (int i = 0; i < children; i++) {
			KThread t = new KThread(new Runnable() {
				public void run() {
					ChildItinerary();
				}
			});
			t.setName("Child " + i);
			t.fork();
		}
	}

	static void AdultItinerary() {
		boatLock.acquire();
		while (true) {
			while (!boatOnOahu || childrenOnOahu > 1) {
				adultCondition.sleep();
			}
			if (adultsOnOahu > 0 && childrenOnOahu <= 1) {
				adultsOnOahu--;
				boatOnOahu = false;
				bg.AdultRowToMolokai();
				childCondition.wake();
				boatLock.release();
				break;
			}
			adultCondition.sleep();
		}
	}

	static void ChildItinerary() {
		boatLock.acquire();
		while (true) {
			while (!boatOnOahu || pilotReady) {
				childCondition.sleep();
			}

			if (childrenOnOahu > 1) {
				pilotReady = true;
				childrenOnOahu--;
				childCondition.wake();
				while (pilotReady) {
					childCondition.sleep();
				}
				bg.ChildRideToMolokai();
				boatLock.release();
				break;
			} else if (childrenOnOahu == 1) {
				if (!pilotReady) {
					childrenOnOahu--;
					boatOnOahu = false;
					bg.ChildRowToMolokai();
					adultCondition.wake();
					boatLock.release();
					break;
				}
			} else {
				childCondition.sleep();
			}
		}
	}

	static void SampleItinerary() {
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
	}

}
