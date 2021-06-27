package mas.JADE_VPP;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProducingRest_TU {

    //****************************  INTERFACE TO NODE-RED (ERP) *******************************
	
	//********************** Scheduling **********************
	
    //Receiving XML Data
    @RequestMapping (method=RequestMethod.PUT, value = "/erp-agent/scheduling_schedulingPlan", produces= "application/octet-stream", consumes = "application/octet-stream")
    public byte[] scheduling(InputStream  xmldata)  throws FileNotFoundException, IOException{
    	byte[] buffer = xmldata.readAllBytes();
    	//Saving the File to the Desktop -- TEsting
//    	try (FileOutputStream fos = new FileOutputStream("C:/Users/Woltmann/Desktop/newFile/pom.xml")) {
//    		   fos.write(buffer);
//    	}
    	TuVariables.schedulingPlan = buffer;
    	TuVariables.schedulingPlanTrigger = true;
   	   	return buffer;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/erp-agent/scheduling_schedulingPlanUploaded")
    public String schedulingPlanUploaded(
    		@RequestParam(name = "referenceID") String _referenceID,
    		@RequestParam(name = "tuName") String _tuName) {
    	TuVariables.referenceID = _referenceID;
    	TuVariables.schedulingTUName = _tuName;
    	if(TuVariables.schedulingPlanTrigger) {
    		TuVariables.schedulingPlanTrigger = false;
    		TuVariables.schedulingTrigger = true;
    	}
    	return _referenceID;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/erp-agent/scheduling_refuseScheduling")
    public String refuseScheduling(
    		@RequestParam(name = "referenceID") String _referenceID, 
    	@RequestParam(name = "tuName") String _tuName){
    	TuVariables.referenceID = _referenceID;
    	TuVariables.schedulingTUName = _tuName;
    	TuVariables.refuseSchedulingTrigger = true;
    	return _referenceID;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/erp-agent/scheduling_inform")
    public String schedulingInform(
    		@RequestParam(name = "referenceID") String _referenceID,
    		@RequestParam(name = "tuName") String _tuName){
    	TuVariables.referenceID = _referenceID;
    	TuVariables.schedulingTUName = _tuName;
    	TuVariables.schedulingInformTrigger = true;
    	return _referenceID;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/erp-agent/scheduling_failure")
    public String schedulingFailure(
    		@RequestParam(name = "referenceID") String _referenceID,
    		@RequestParam(name = "tuName") String _tuName) {
    	TuVariables.referenceID = _referenceID;
    	TuVariables.schedulingTUName = _tuName;
    	TuVariables.schedulingFailureTrigger = true;
    	return _referenceID;
    }
    
    //********************** LoadTimeWindows Sharing **********************
    
    @RequestMapping (method=RequestMethod.PUT, value = "/erp-agent/loadTimeWindows_request")
    public String loadTimeWindowsRequest(
    		@RequestParam(name = "loadTimeWindowsReference") String _loadTimeWindowsReference) {
    	TuVariables.loadTimeWindowsReference = _loadTimeWindowsReference;
    	TuVariables.loadTimeWindowsTrigger = true;
    	synchronized(TuVariables.LOCK) 
    	{
    		TuVariables.LOCK.notify();	
    	}
    	return _loadTimeWindowsReference;
    }
    
    //********************** Cancel Operation **********************
    
    @RequestMapping (method=RequestMethod.PUT, value = "/erp-agent/cancelOperation_inform")
    public String operationCancelInform(
    		@RequestParam(name = "referenceID") String _referenceID,
    		@RequestParam(name = "tuName") String _tuName) {
    	TuVariables.cancelOperationReference = _referenceID;
    	TuVariables.cancelOperationTuName = _tuName;
    	TuVariables.cancelOperationTrigger = true;
    	synchronized(TuVariables.LOCK) 
    	{
    		TuVariables.LOCK.notify();	
    	}
    	return _referenceID;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/erp-agent/cancelOperation_confirm")
    public String operationCancelConfirm(
    		@RequestParam(name = "referenceID") String _referenceID,
    		@RequestParam(name = "tuName") String _tuName) {
    	TuVariables.receiveCancelOperationReference = _referenceID;
    	TuVariables.receiveCancelOperationTuName = _tuName;
    	TuVariables.receiveCancelOperationTrigger = true;
    	return _referenceID;
    }
    
    
    @RequestMapping (method=RequestMethod.PUT, value = "/erp-agent/cancelOperation_failure")
    public String operationCancelFail(
    		@RequestParam(name = "referenceID") String _referenceID,
    		@RequestParam(name = "tuName") String _tuName) {
    	TuVariables.receiveCancelOperationReference = _referenceID;
    	TuVariables.receiveCancelOperationTuName = _tuName;
    	TuVariables.receiveCancelOperationTriggerFail = true;
    	return _referenceID;
    }
    
  
    //****************************  INTERFACE TO NODE-RED (TU) *******************************
    //************************ TU Control **************************
    
    @RequestMapping (method=RequestMethod.PUT, value = "/tu-agent/control_newSetpointFailed")
    public InterfacePayloadNewSetpoint newSetpointFailed(@RequestParam(name = "tuName") String _tuName)
    {
    	InterfacePayloadNewSetpoint payload = new InterfacePayloadNewSetpoint(_tuName);
    	TuVariables.tuName = _tuName;
    	TuVariables.requestFailureTrigger = true;
      	return payload;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/tu-agent/control_newSetpointDone")
    public InterfacePayloadNewSetpoint newSetpointDone(@RequestParam(name = "tuName") String _tuName)
    {
    	InterfacePayloadNewSetpoint payload = new InterfacePayloadNewSetpoint(_tuName);
    	TuVariables.tuName = _tuName;
    	TuVariables.requestDoneTrigger = true;
      	return payload;
    }
    
    //************************ TU Balancing **************************
    
    @RequestMapping (method=RequestMethod.PUT, value = "/tu-agent/balancing_agree")
    public InterfacePayloadBalancing newBalancingAgree(@RequestParam(name = "tuName") String _tuName){
    	InterfacePayloadBalancing payload = new InterfacePayloadBalancing(_tuName);
    	TuVariables.balancingTuName = _tuName;
    	TuVariables.balancingAgreeTrigger = true;
      	return payload;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/tu-agent/balancing_refuse")
    public InterfacePayloadBalancing newBalancingRefuse(@RequestParam(name = "tuName") String _tuName){
    	InterfacePayloadBalancing payload = new InterfacePayloadBalancing(_tuName);
    	TuVariables.balancingTuName = _tuName;
    	TuVariables.balancingRefuseTrigger = true;
      	return payload;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/tu-agent/balancing_informInstant")
    public InterfacePayloadBalancing newBalancingInformInstant(
    		@RequestParam(name = "tuName") String _tuName,
    		@RequestParam(name = "feedIn") int _feedIn, 
    		@RequestParam(name = "operatingPoint") int _operatingPoint,
    		@RequestParam(name = "leadingOperatingPoint") int _leadingOperatingPoint,
    		@RequestParam(name = "currentValueFR") int _currentValueFR,
    		@RequestParam(name = "assignedPool") int _assignedPool,
    		@RequestParam(name = "status") int _status, 
    		@RequestParam(name = "frequency") int _frequency,
    		@RequestParam(name = "aFRRsetpoint") int _aFRRsetpoint,
    		@RequestParam(name = "aFRRsetpointEcho") int _aFRRsetpointEcho,
    		@RequestParam(name = "setpointFR") int _setpointFR,
    		@RequestParam(name = "aFRRGradientPOS") int _aFRRGradientPOS,
    		@RequestParam(name = "aFRRGradientNEG") int _aFRRGradientNEG, 
    		@RequestParam(name = "capacityPOS") int _capacityPOS,
    		@RequestParam(name = "capacityNEG") int _capacityNEG,
    		@RequestParam(name = "holdingCapacityPOS") int _holdingCapacityPOS, 
    		@RequestParam(name = "holdingCapacityNEG") int _holdingCapacityNEG,
    		@RequestParam(name = "controlBandPOS") int _controlBandPOS,
    		@RequestParam(name = "controlBandNEG") int _controlBandNEG) {
    	InterfacePayloadBalancing payload = new InterfacePayloadBalancing(_tuName);
    	TuVariables.balancingTuName = _tuName;
    	TuVariables.feedIn = _feedIn;											//Einspeisung
    	TuVariables.operatingPoint = _operatingPoint; 							//Arbeitspunkt
    	TuVariables.leadingOperatingPoint = _leadingOperatingPoint;				//vorauseilender Arbeitspunkt
    	TuVariables.currentValueFR = _currentValueFR;							//Regelleistungsistwert
    	TuVariables.assignedPool = _assignedPool;								//Poolzuordnung
    	TuVariables.status = _status;											//Status (Meldung)
    	TuVariables.frequency = _frequency;										//Frequenz
    	TuVariables.aFRRsetpoint = _aFRRsetpoint;								//aFRR-Soll (ÜNB -> POOL)
    	TuVariables.aFRRsetpointEcho = _aFRRsetpointEcho;						//aFRR-Soll-Echo (Pool -> ÜNB)
    	TuVariables.setpointFR = _setpointFR;									//Regelleistungs-Soll
    	TuVariables.aFRRGradientPOS = _aFRRGradientPOS;							//aFRR-Gradient POS
    	TuVariables.aFRRGradientNEG = _aFRRGradientNEG;							//aFRR-Gradient NEG
    	TuVariables.capacityPOS = _capacityPOS;									//Arbeitsvermögen POS (bei begrenztem Energiespeicher)
    	TuVariables.capacityNEG = _capacityNEG;									//Arbeitsvermögen NEG (bei begrenzten Energiespeicher)
    	TuVariables.holdingCapacityPOS = _holdingCapacityPOS;					//Aktuelle Vorhalteleistung POS
    	TuVariables.holdingCapacityNEG = _holdingCapacityNEG;					//Aktuelle Vorhalteleistung NEG
    	TuVariables.controlBandPOS = _controlBandPOS;							//Regelband POS
    	TuVariables.controlBandNEG = _controlBandNEG;							//Regelband NEG
    	TuVariables.balancingInformInstantTrigger = true;
      	return payload;
    }
       
        
    @RequestMapping (method=RequestMethod.PUT, value = "/tu-agent/balancing_inform")
    public InterfacePayloadBalancing newBalancingInform(
    		@RequestParam(name = "tuName") String _tuName,
    		@RequestParam(name = "feedIn") int _feedIn, 
    		@RequestParam(name = "operatingPoint") int _operatingPoint,
    		@RequestParam(name = "leadingOperatingPoint") int _leadingOperatingPoint,
    		@RequestParam(name = "currentValueFR") int _currentValueFR,
    		@RequestParam(name = "assignedPool") int _assignedPool,
    		@RequestParam(name = "status") int _status, 
    		@RequestParam(name = "frequency") int _frequency,
    		@RequestParam(name = "aFRRsetpoint") int _aFRRsetpoint,
    		@RequestParam(name = "aFRRsetpointEcho") int _aFRRsetpointEcho,
    		@RequestParam(name = "setpointFR") int _setpointFR,
    		@RequestParam(name = "aFRRGradientPOS") int _aFRRGradientPOS,
    		@RequestParam(name = "aFRRGradientNEG") int _aFRRGradientNEG, 
    		@RequestParam(name = "capacityPOS") int _capacityPOS,
    		@RequestParam(name = "capacityNEG") int _capacityNEG,
    		@RequestParam(name = "holdingCapacityPOS") int _holdingCapacityPOS, 
    		@RequestParam(name = "holdingCapacityNEG") int _holdingCapacityNEG,
    		@RequestParam(name = "controlBandPOS") int _controlBandPOS,
    		@RequestParam(name = "controlBandNEG") int _controlBandNEG) {
    	InterfacePayloadBalancing payload = new InterfacePayloadBalancing(_tuName);
    	TuVariables.balancingTuName = _tuName;
    	TuVariables.feedIn = _feedIn;											//Einspeisung
    	TuVariables.operatingPoint = _operatingPoint; 							//Arbeitspunkt
    	TuVariables.leadingOperatingPoint = _leadingOperatingPoint;				//vorauseilender Arbeitspunkt
    	TuVariables.currentValueFR = _currentValueFR;							//Regelleistungsistwert
    	TuVariables.assignedPool = _assignedPool;								//Poolzuordnung
    	TuVariables.status = _status;											//Status (Meldung)
    	TuVariables.frequency = _frequency;										//Frequenz
    	TuVariables.aFRRsetpoint = _aFRRsetpoint;								//aFRR-Soll (ÜNB -> POOL)
    	TuVariables.aFRRsetpointEcho = _aFRRsetpointEcho;						//aFRR-Soll-Echo (Pool -> ÜNB)
    	TuVariables.setpointFR = _setpointFR;									//Regelleistungs-Soll
    	TuVariables.aFRRGradientPOS = _aFRRGradientPOS;							//aFRR-Gradient POS
    	TuVariables.aFRRGradientNEG = _aFRRGradientNEG;							//aFRR-Gradient NEG
    	TuVariables.capacityPOS = _capacityPOS;									//Arbeitsvermögen POS (bei begrenztem Energiespeicher)
    	TuVariables.capacityNEG = _capacityNEG;									//Arbeitsvermögen NEG (bei begrenzten Energiespeicher)
    	TuVariables.holdingCapacityPOS = _holdingCapacityPOS;					//Aktuelle Vorhalteleistung POS
    	TuVariables.holdingCapacityNEG = _holdingCapacityNEG;					//Aktuelle Vorhalteleistung NEG
    	TuVariables.controlBandPOS = _controlBandPOS;							//Regelband POS
    	TuVariables.controlBandNEG = _controlBandNEG;							//Regelband NEG
    	TuVariables.balancingInformTrigger = true;
      	return payload;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/tu-agent/balancing_failure")
    public InterfacePayloadBalancing newBalancingFailure(@RequestParam(name = "tuName") String _tuName){
    	InterfacePayloadBalancing payload = new InterfacePayloadBalancing(_tuName);
    	TuVariables.balancingTuName = _tuName;
    	TuVariables.balancingFailureTrigger = true;
      	return payload;
    }
    
    //********************** FREQUENCY RELAY ACTIONS **********************
 
    @RequestMapping (method=RequestMethod.PUT, value = "/tu-agent/freqRelay_enableInform")
    public String freqRelay_enableInform(@RequestParam(name = "tuName") String _tuName){
       	TuVariables.freqRelayEnableTUName = _tuName;
    	TuVariables.freqRelayEnableInformTrigger = true;
      	return _tuName;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/tu-agent/freqRelay_enableFailure")
    public String freqRelay_enableFailure(@RequestParam(name = "tuName") String _tuName){
    	TuVariables.freqRelayEnableTUName = _tuName;
    	TuVariables.freqRelayEnableFailureTrigger = true;
      	return _tuName;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/tu-agent/freqRelay_blockInform")
    public String freqRelay_blockInform(@RequestParam(name = "tuName") String _tuName){
    	TuVariables.freqRelayDisableTUName = _tuName;
    	TuVariables.freqRelayDisableInformTrigger = true;
      	return _tuName;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/tu-agent/freqRelay_blockFailure")
    public String freqRelay_blockFailure(@RequestParam(name = "tuName") String _tuName){
    	TuVariables.freqRelayDisableTUName = _tuName;
    	TuVariables.freqRelayDisableFailureTrigger = true;
      	return _tuName;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/tu-agent/freqRelay_triggered")
    public String freqRelay_triggered(@RequestParam(name = "tuName") String _tuName){
       	TuVariables.freqRelayConfirmTUName = _tuName;
    	TuVariables.freqRelayConfirmTrigger = true;
    	synchronized(TuVariables.LOCK) 
    	{
    		TuVariables.LOCK.notify();	
    	}
      	return _tuName;
    }
    
    
    //****************************  INTERFACE TO NODE-RED (EMS) *******************************
   
    //********************** Accounting **********************
    
    @RequestMapping (method=RequestMethod.PUT, value = "/ems-agent/accounting_energyConsumptionProfile", produces= "application/octet-stream", consumes = "application/octet-stream")
    public byte[] accounting(InputStream  xmldata)  throws FileNotFoundException, IOException{
    	byte[] buffer = xmldata.readAllBytes();

    	TuVariables.energyConsumptionProfile = buffer;
    	TuVariables.accountingTriggerECP = true;
   	   	return buffer;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/ems-agent/accounting_energyConsumptionProfileUploaded")
    public String energyConsumptionProfileUploaded(
    		@RequestParam(name = "referenceID") String _referenceID,
    		@RequestParam(name = "tuName") String _tuName) {
    	TuVariables.accountingTuName = _tuName;
    	TuVariables.accountingReferenceID = _referenceID;
    	TuVariables.accountingTriggerReference = true;
    	//Object LOCK = new Object();
    	//TuVariables.notifya();
    	synchronized(TuVariables.LOCK) 
    	{
    		TuVariables.LOCK.notify();	
    	}
    	return _tuName;
    }
    
    
    //****************************  INTERFACE TO NODE-RED (from any system) *******************************
    
    //********************** RequestInfos **********************
    @RequestMapping (method=RequestMethod.PUT, value = "/erp-agent/requestInfo")
    public String requestInfos() {
    	TuVariables.requestInfosTrigger = true;
           	synchronized(TuVariables.LOCK) {
        		TuVariables.LOCK.notify();	
        	}
           	return "searchingForVPPAgent";	
    }
    
    
}