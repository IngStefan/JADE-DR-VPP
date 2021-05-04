package mas.JADE_VPP;

//import org.springframework.web.bind.annotation.RequestParam;

public class TuVariables {

	//Synchronized object for thread handling
	public static final Object LOCK = new Object();
	
	//************Scheduling Variables***********
	public static byte[]  schedulingPlan;
	public static boolean schedulingPlanTrigger = false;
	public static boolean schedulingTrigger = false;			//true = scheduling plan has been transferred to the TU Agent
	public static boolean refuseSchedulingTrigger = false;	
	public static String referenceID = "empty";	
	public static String schedulingTUName = "noName";	
	public static boolean schedulingFailureTrigger = false;			
	public static boolean schedulingInformTrigger = false;	

	
	//************ TU Control Sequence Variables ***********
	public static boolean requestFailureTrigger = false;
	public static boolean requestDoneTrigger = false;
	public static String tuName = "noName";	

	
	//************ TU Balancing Sequence Variables ***********
	public static boolean balancingRefuseTrigger = false;
	public static boolean balancingAgreeTrigger = false;
	public static boolean balancingInformTrigger = false;
	public static boolean balancingFailureTrigger = false;
	public static String balancingTuName = "noName";	
	public static int feedIn; 									//Einspeisung
	public static int operatingPoint; 							//Arbeitspunkt
	public static int leadingOperatingPoint;					//vorauseilender Arbeitspunkt
	public static int currentValueFR;							//Regelleistungsistwert
	public static int assignedPool;								//Poolzuordnung
	public static int status;									//Status (Meldung)
	public static int frequency;								//Frequenz
	public static int aFRRsetpoint;								//aFRR-Soll (ÜNB -> POOL)
	public static int aFRRsetpointEcho;							//aFRR-Soll-Echo (Pool -> ÜNB)
	public static int setpointFR;								//Regelleistungs-Soll
	public static int aFRRGradientPOS;							//aFRR-Gradient POS
	public static int aFRRGradientNEG;							//aFRR-Gradient NEG
	public static int capacityPOS;								//Arbeitsvermögen POS (bei begrenztem Energiespeicher)
	public static int capacityNEG;								//Arbeitsvermögen NEG (bei begrenzten Energiespeicher)
	public static int holdingCapacityPOS;						//Aktuelle Vorhalteleistung POS
	public static int holdingCapacityNEG;						//Aktuelle Vorhalteleistung NEG
	public static int controlBandPOS;							//Regelband POS
	public static int controlBandNEG;							//Regelband NEG
	
	
	//************ EMS Accounting Variables ***********
	public static boolean accountingTriggerECP = false;
	public static boolean accountingTriggerReference = false;
	public static String accountingTuName = "noName";
	public static String accountingReferenceID = "noIDSet";
	public static byte[]  energyConsumptionProfile;

	
	
	//*********** LoadTimeWindows ***********
	public static boolean loadTimeWindowsTrigger = false;
	public static String loadTimeWindowsReference = "noReferenceSet";
	
	//*********** operationCancel ***********
	public static boolean cancelOperationTrigger = false;
	public static String cancelOperationReference = "noID";
	public static String cancelOperationTuName = "noName";
	
	public static boolean receiveCancelOperationTrigger = false;
	public static boolean receiveCancelOperationTriggerFail = false;
	public static String receiveCancelOperationReference = "noID";
	public static String receiveCancelOperationTuName = "noName";
	
	//FrequencyRelay Actions
	public static boolean freqRelayEnableInformTrigger = false;
	public static boolean freqRelayEnableFailureTrigger = false;
	public static boolean freqRelayDisableInformTrigger = false;
	public static boolean freqRelayDisableFailureTrigger = false;
	public static boolean freqRelayConfirmTrigger = false;
	public static String freqRelayEnableTUName = "Load-Identifier";
	public static String freqRelayDisableTUName = "Load-Identifier";
	public static String freqRelayConfirmTUName = "Load-Identifier";
	
	//requestInfos Actions
	public static boolean requestInfosTrigger = false;
	
	
	
	public static void resetScheduling() {
		schedulingTUName = "noName";	
		referenceID = "empty";	
		schedulingPlan = new byte[] {(byte)0x00};
	}
	
	public static void resetBalancing() {
		feedIn = 0; 			
		operatingPoint = 0; 		
		leadingOperatingPoint = 0;	
		currentValueFR = 0;		
		assignedPool = 0;		
		status = 0;					
		frequency = 0;			
		aFRRsetpoint = 0;			
		aFRRsetpointEcho = 0;		
		setpointFR = 0;				
		aFRRGradientPOS = 0;		
		aFRRGradientNEG = 0;		
		capacityPOS = 0;			
		capacityNEG = 0;			
		holdingCapacityPOS = 0;
		holdingCapacityNEG = 0;		
		controlBandPOS = 0;			
		controlBandNEG = 0;	
	}
	
	public static void resetLoadTimeWindows() {
		loadTimeWindowsReference = "noReferenceSet";
	}
	
	public static void resetfreqRelayEnable() {
		freqRelayEnableTUName = "Load-Identifier";
	}
	
	public static void resetfreqRelayDisable() {
		freqRelayDisableTUName = "Load-Identifier";
	}
	
	public static void resetfreqRelayConfirm() {
		freqRelayConfirmTUName = "Load-Identifier";
	}
	
	
}
