package mas.Simulation;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;






@RestController
public class ProducingRest_TU {

    //****************************  INTERFACE TO NODE-RED (ERP) *******************************
    //Receiving XML Data
   
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp/scheduling_schedulingPlanInfos")
    public String schedulingPlanUploaded(
    		@RequestParam(name = "schedulingPlanReference") String _schedulingPlanReference,
    		@RequestParam(name = "tuName") String _tuName) {
    	Global.OfferReferenceIDs.add(_schedulingPlanReference);
    	Global.schedulingTrigger = true;
    	System.out.println( "MSG received" );
    	return _schedulingPlanReference;
    }
    
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp/scheduling_done")
    public String schedulingPlanDone(
    		@RequestParam(name = "schedulingPlanReference") String _schedulingPlanReference,
    		@RequestParam(name = "tuName") String _tuName) {
    	System.out.println("Planning Done");
    	return _schedulingPlanReference;
    }
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp/scheduling_failed")
    public String schedulingPlanFailed(
    		@RequestParam(name = "schedulingPlanReference") String _schedulingPlanReference,
    		@RequestParam(name = "tuName") String _tuName) {
    	System.out.println("Planning Done");
    	return _schedulingPlanReference;
    }
    
    
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp/balancing_inform")
    public String balancingInform(
    		@RequestParam(name = "setpointFR") String _setpointFR,
    		@RequestParam(name = "tuName") String _tuName) {
    	System.out.println("Balancing values received from "+_tuName+" with current setpoint: "+ _setpointFR);
    	return _tuName;
    }
    
    
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp/freqRelay_enableInform")
    public String freqRelay_enableInform(
    		@RequestParam(name = "agentName") String _setpointFR,
    		@RequestParam(name = "tuName") String _tuName) {
    	System.out.println("Answer received from "+_tuName);
    	return _tuName;
    }
    
        
    @RequestMapping (method=RequestMethod.PUT, value = "/vpp/accounting_energyConsumptionProfileReference")
    public String accountingInform(
    	 	@RequestParam(name = "tuName") String _tuName) {
    	Global.accountingList.add(_tuName);
    	Global.accountingTrigger = true;
    		System.out.println("Accounting received");
    	return _tuName;
    }
    

 
    
    
}