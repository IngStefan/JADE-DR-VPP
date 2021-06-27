package mas.JADE_VPP;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

//This class provides the functionality to offer REST-services (i.e. for Node-Red Interface)

//import java.util.concurrent.atomic.AtomicLong;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ProducingRest_VPP {

	//****************************  INTERFACE TO NODE-RED (VPP) *******************************
	//********************** Scheduling **********************
	
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/scheduling_request")
    public InterfacePayloadVPP schedulingRequestValues(
    		@RequestParam(name = "referenceID") String _referenceID,
    		@RequestParam(name = "serviceDescriptions") String _serviceDescriptions,
    		@RequestParam(name = "schedulingStart") String _schedulingStart, 
    		@RequestParam(name = "schedulingEnd") String _schedulingEnd,
    		@RequestParam(name = "expiration") String expiration) 
    {
    	InterfacePayloadVPP payload = new InterfacePayloadVPP(_referenceID, _serviceDescriptions, _schedulingStart, _schedulingEnd, expiration);
    	VppVariables.schedulingStart = _schedulingStart;
    	VppVariables.schedulingEnd = _schedulingEnd;
    	VppVariables.referenceID = _referenceID;
    	VppVariables.serviceDescriptions = _serviceDescriptions;
    	VppVariables.expiration = expiration;
    	System.out.println(payload.toString());
    	VppVariables.schedulingRequestTrigger = true;
    	synchronized(VppVariables.LOCK) 
    	{
    		VppVariables.LOCK.notify();	
    	}
    	return payload;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/scheduling_accept-proposal")
    public InterfacePayloadVPP acceptProposalValues(
    		@RequestParam(name = "schedulingPlanReference") String _schedulingPlanReference,
    		@RequestParam(name = "schedulingStart") String _schedulingStart, 
    		@RequestParam(name = "schedulingEnd") String _schedulingEnd)
    {
    	InterfacePayloadVPP payload = new InterfacePayloadVPP(_schedulingPlanReference, _schedulingStart, _schedulingEnd);
    	VppVariables.schedulingStart = _schedulingStart;
    	VppVariables.schedulingEnd = _schedulingEnd;
    	VppVariables.schedulingPlanReference = _schedulingPlanReference;
    	VppVariables.schedulingAcceptTrigger = true;
    	return payload;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/scheduling_reject-proposal")
    public InterfacePayloadVPP rejectProposalValues(
    		@RequestParam(name = "schedulingPlanReference") String _schedulingPlanReference)
    {
    	InterfacePayloadVPP payload = new InterfacePayloadVPP(_schedulingPlanReference);
    	VppVariables.schedulingPlanReference = _schedulingPlanReference;
    	VppVariables.schedulingRejectTrigger = true;
    	return payload;
    }
    
    
    
    //********************** Load Control **********************
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/control_newSetpoint")
    public InterfacePayloadVPP tuControl(
//    		@RequestParam(name = "agentName") String _agentName,
    		@RequestParam(name = "tuName") String _tuName,
    		@RequestParam(name = "newSetpoint") int _newSetpoint){
    	InterfacePayloadVPP payload = new InterfacePayloadVPP("default", _tuName, _newSetpoint);
    	VppVariables.tuName = _tuName;
    	//VppVariables.newSetpoint = Integer.valueOf(_newSetpoint);
    	VppVariables.newSetpoint = _newSetpoint;
    	VppVariables.newSetpointTrigger = true;
    	synchronized(VppVariables.LOCK) {
    		VppVariables.LOCK.notify();	
    	}
    	return payload;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/control_newLoadProfile", produces= "application/octet-stream", consumes = "application/octet-stream")
    public byte[] scheduling(InputStream  xmldata)  throws FileNotFoundException, IOException{
    	byte[] buffer = xmldata.readAllBytes();
    	VppVariables.newLoadProfile = buffer;
    	VppVariables.newLoadProfileTrigger = true;
   	   	return buffer;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/control_newLoadProfileUploaded")
    public String schedulingPlanUploaded(
    		@RequestParam(name = "tuName") String _tuName) {
    	VppVariables.tuName = _tuName;
    	if(VppVariables.newLoadProfileTrigger) {
    		VppVariables.newLoadProfileTrigger = false;
    		VppVariables.newLoadProfileTriggerUpdated = true;
        	synchronized(VppVariables.LOCK) {
        		VppVariables.LOCK.notify();	
        	}
    	}
    	return _tuName;
    }
    
    
    
    
    //********************** Balancing **********************
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/balancing_subscribe")
    public InterfacePayloadVPP tuControl(
    		@RequestParam(name = "balancingTUName") String _balancingTUName,
    		@RequestParam(name = "balancingStart") String _balancingStart,
    		@RequestParam(name = "balancingEnd") String _balancingEnd,
    		@RequestParam(name = "balancingUpdateRate") int _balancingUpdateRate,
    		@RequestParam(name = "balancingReferenceID") String _balancingReferenceID
    		)
    {
    	InterfacePayloadVPP payload = new InterfacePayloadVPP(_balancingTUName, _balancingStart, _balancingEnd, _balancingUpdateRate, _balancingReferenceID );
    	VppVariables.balancingStart = _balancingStart;
    	VppVariables.balancingEnd = _balancingEnd;
    	VppVariables.balancingTUName = _balancingTUName;
    	VppVariables.balancingReferenceID = _balancingReferenceID;
    	VppVariables.balancingUpdateRate = _balancingUpdateRate;
    	VppVariables.balancingTrigger = true;
    	synchronized(VppVariables.LOCK) 
    	{
    		VppVariables.LOCK.notify();	
    	}
    	return payload;
    }
    
    //********************** Accounting **********************
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/accounting_ECPReceived")
    public String ecpReceived(
    		@RequestParam(name = "tuName") String _tuName) {
    	VppVariables.accountingTUName = _tuName;
    	VppVariables.accountingTrigger = true;
    	return _tuName;
    }
    
    
    
  //********************** Operation Cancel **********************
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/cancelOperation_confirm")
    public String operationCancelInform(
    		@RequestParam(name = "referenceID") String _referenceID,
    		@RequestParam(name = "tuName") String _tuName) {
    	VppVariables.cancelOperationReference = _referenceID;
    	VppVariables.cancelOperationTuName = _tuName;
    	VppVariables.cancelOperationTrigger = true;
    	return _referenceID;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/cancelOperation_failure")
    public String operationCancelInformFail(
    		@RequestParam(name = "referenceID") String _referenceID,
    		@RequestParam(name = "tuName") String _tuName) {
    	VppVariables.cancelOperationReference = _referenceID;
    	VppVariables.cancelOperationTuName = _tuName;
    	VppVariables.cancelOperationTriggerFail = true;
    	return _referenceID;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/cancelOperation_inform")
    public String operationCancelSend(
    		@RequestParam(name = "referenceID") String _referenceID,
    		@RequestParam(name = "tuName") String _tuName) {
    	VppVariables.sendCancelOperationReference = _referenceID;
    	VppVariables.sendCancelOperationTuName = _tuName;
    	VppVariables.sendCancelOperationTrigger = true;
    	synchronized(VppVariables.LOCK) 
    	{
    		VppVariables.LOCK.notify();	
    	}
    	return _referenceID;
    }
    
    
    
    //********************** LoadTimeWindowsShare **********************
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/loadTimeWindows_inform")
    public InterfacePayloadLoadTimeWindows loadTimeWindows(
    		@RequestParam(name = "loadTimeWindowsReference") String _loadTimeWindowsReference,
    		@RequestParam(name = "windowHighBegin") String _windowHighBegin,
    		@RequestParam(name = "windowHighEnd") String _windowHighEnd,
    		@RequestParam(name = "windowLowBegin") String _windowLowBegin,
    		@RequestParam(name = "windowLowEnd") String _windowLowEnd) {
    	InterfacePayloadLoadTimeWindows payload = new InterfacePayloadLoadTimeWindows(_loadTimeWindowsReference, _windowHighBegin, _windowHighEnd, _windowLowBegin, _windowLowEnd);
    	VppVariables.loadTimeWindowsTrigger = true;
    	VppVariables.loadTimeWindowsReference = _loadTimeWindowsReference;
    	VppVariables.windowHighBegin = _windowHighBegin;
    	VppVariables.windowHighEnd = _windowHighEnd;
    	VppVariables.windowLowBegin = _windowLowBegin;
    	VppVariables.windowLowEnd = _windowLowEnd;
    	return payload;
    }
    
    
    //********************** LoadTimeWindowsShare Broadcast**********************
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/loadTimeWindows_broadcast")
    public InterfacePayloadLoadTimeWindows loadTimeWindowsBroadcast(
    		@RequestParam(name = "loadTimeWindowsReference") String _loadTimeWindowsReference,
    		@RequestParam(name = "windowHighBegin") String _windowHighBegin,
    		@RequestParam(name = "windowHighEnd") String _windowHighEnd,
    		@RequestParam(name = "windowLowBegin") String _windowLowBegin,
    		@RequestParam(name = "windowLowEnd") String _windowLowEnd) {
    	InterfacePayloadLoadTimeWindows payload = new InterfacePayloadLoadTimeWindows(_loadTimeWindowsReference, _windowHighBegin, _windowHighEnd, _windowLowBegin, _windowLowEnd);
    	VppVariables.loadTimeWindowsBroadcastTrigger = true;
    	VppVariables.loadTimeWindowsReference = _loadTimeWindowsReference;
    	VppVariables.windowHighBegin = _windowHighBegin;
    	VppVariables.windowHighEnd = _windowHighEnd;
    	VppVariables.windowLowBegin = _windowLowBegin;
    	VppVariables.windowLowEnd = _windowLowEnd;
    	synchronized(VppVariables.LOCK) 
    	{
    		VppVariables.LOCK.notify();	
    	}
    	return payload;
    }
    
    //********************** FrequRelayEnable **********************
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/freqRelay_enable")
    public String enableTuName(
    		@RequestParam(name = "tuName") String _tuName) {
    	VppVariables.freqRelayEnableTUName = _tuName;
    	VppVariables.freqRelayEnableTrigger = true;
           	synchronized(VppVariables.LOCK) {
        		VppVariables.LOCK.notify();	
        	}
           	return _tuName;	
    	}

   
    //********************** FrequRelayBlock **********************
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/freqRelay_block")
    public String blockTuName(
    		@RequestParam(name = "tuName") String _tuName) {
    	VppVariables.freqRelayDisableTUName = _tuName;
    	VppVariables.freqRelayDisableTrigger = true;
           	synchronized(VppVariables.LOCK) {
        		VppVariables.LOCK.notify();	
        	}
           	return _tuName;	
    	}
    
    //********************** FrequRelayConfirm **********************
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/freqRelay_confirm")
    public String confirmTuName(
    		@RequestParam(name = "tuName") String _tuName) {
    	VppVariables.freqRelayConfirmTUName = _tuName;
    	VppVariables.freqRelayConfirmTrigger = true;
       	return _tuName;	
    	}

    
    
    //********************** RequestInfos **********************
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp-agent/requestInfo")
    public String requestInfos(
    		@RequestParam(name = "tuName") String _tuName) {
    	VppVariables.requestInfosTuName = _tuName;
    	VppVariables.requestInfosTrigger = true;
           	synchronized(VppVariables.LOCK) {
        		VppVariables.LOCK.notify();	
        	}
           	return _tuName;	
    	}
    
    
    
}