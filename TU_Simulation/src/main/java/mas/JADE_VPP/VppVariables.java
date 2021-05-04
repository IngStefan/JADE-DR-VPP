package mas.JADE_VPP;

public class VppVariables {

	//Synchronized object for thread handling
	public static final Object LOCK = new Object();
	
	//Scheduling
	public static boolean schedulingRequestTrigger = false;
	public static boolean schedulingAcceptTrigger = false;
	public static boolean schedulingRejectTrigger = false;
	public static String schedulingStart = "1990-02-22 05:00:00.0";
	public static String schedulingEnd = "1990-02-22 07:00:00.0";
	public static String expiration = "1990-02-22 07:00:00.0";
	public static String referenceID = "0";
	public static String serviceDescriptions ="empty" ;
	public static String schedulingPlanReference ="empty";
	
	//Load Control
	public static boolean newSetpointTrigger = false;
	public static boolean newLoadProfileTrigger = false;
	public static boolean newLoadProfileTriggerUpdated = false;
	public static int newSetpoint = 0;
	public static byte[] newLoadProfile;
	//public static String agentName = "AgentName";
	public static String tuName = "Load-Identifier";
	
	//Balancing
	public static boolean balancingTrigger = false;	
	public static String balancingStart = "1990-02-22 05:00:00.0";
	public static String balancingEnd = "1990-02-22 05:00:00.0";
	public static String balancingTUName = "Load-Identifier";
	public static String balancingAgentName = "AgentName";
	public static String balancingReferenceID = "0";
	public static int balancingUpdateRate = 1000;
	
	//Accounting
	public static boolean accountingTrigger = false;	
	public static String accountingTUName = "Load-Identifier";
	public static String accountingAgentName = "AgentName";
	
	//LoadTimeWindows Sharing
	public static boolean loadTimeWindowsTrigger = false;
	public static boolean loadTimeWindowsBroadcastTrigger = false;
	public static String loadTimeWindowsReference = "noTariff";
	public static String windowHighBegin = "noTimeWindow"; //format "1990-02-22 05:00:00.0"
	public static String windowHighEnd = "noTimeWindow";
	public static String windowLowBegin = "noTimeWindow";
	public static String windowLowEnd = "noTimeWindow";
	
	
	//FrequencyRelay Actions
	public static boolean freqRelayEnableTrigger = false;
	public static boolean freqRelayDisableTrigger = false;
	public static boolean freqRelayConfirmTrigger = false;
	public static String freqRelayEnableTUName = "Load-Identifier";
	public static String freqRelayDisableTUName = "Load-Identifier";
	public static String freqRelayConfirmTUName = "Load-Identifier";
	
	//*********** operationCancel ***********
	public static boolean cancelOperationTrigger = false;
	public static boolean cancelOperationTriggerFail = false;
	public static String cancelOperationReference = "noID";
	public static String cancelOperationTuName = "noName";
	
	public static boolean sendCancelOperationTrigger = false;
	public static String sendCancelOperationReference = "noID";
	public static String sendCancelOperationTuName = "noName";
	
	//Request Infos
	public static String requestInfosTuName = "noName";
	public static boolean requestInfosTrigger = false;
		
	
	//Methods
	public static void resetScheduling(){
		 schedulingStart = "1990-02-22 05:00:00.0";
		 schedulingEnd = "1990-02-22 07:00:00.0";
		 expiration = "1990-02-22 07:00:00.0";
		 referenceID = "0";
		 serviceDescriptions = "empty";
		 schedulingPlanReference = "empty";
	}
	
	public static void resetNewSetpoint() {
		newSetpoint = 0;
		tuName = "Load-Identifier";
	}
	
	public static void resetNewLoadProfile() {
		newLoadProfile = new byte[] {(byte)0x00};
		tuName = "Load-Identifier";
	}
	
	
	public static void resetBalancing() {
		balancingStart = "1990-02-22 05:00:00.0";
		balancingEnd = "1990-02-22 05:00:00.0";
		balancingTUName = "Load-Identifier";
		balancingAgentName = "AgentName";
		balancingReferenceID = "0";
		balancingUpdateRate = 1000;
	}
	
	public static void resetloadTimeWindows() {
		loadTimeWindowsReference = "noTariff";
		windowHighBegin = "noTimeWindow";
		windowHighEnd = "noTimeWindow";
		windowLowBegin = "noTimeWindow";
		windowLowEnd = "noTimeWindow";
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
