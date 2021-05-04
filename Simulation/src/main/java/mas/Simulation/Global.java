package mas.Simulation;

import java.util.ArrayList;

public class Global {

	//Synchronized object for thread handling
	public static final Object LOCK = new Object();
	
	//Scheduling
	public static int amountOffers = 0;
	public static boolean schedulingTrigger = false;
	public static ArrayList <String> OfferReferenceIDs = new ArrayList <String>();
	
	
	public static boolean accountingTrigger = false;
	public static ArrayList <String> accountingList = new ArrayList <String>();
}
