package mas.Simulation;

//This class provides the functionality to consume REST-services (i.e. from Node-Red)


import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

public class ConsumingRest_VPP {
	
	
	public void putNodeRed(final String URL_NODERED, PutVariable INJECT, Object value)
	{
		
		//*********PUT-MEthode
		RestTemplate restTemplate = new RestTemplate();

		
		switch(INJECT)
		{
		case SETPOINT:
			restTemplate.put(URL_NODERED+"/vpp-agent/control_newSetpoint", value);
			break;
		case LOADPROFILE:
			restTemplate.put(URL_NODERED+"/vpp-agent/control_newLoadProfile", value);
			restTemplate.put(URL_NODERED+"/vpp-agent/control_newLoadProfileUploaded", value);
			break;
		case STARTSCHEDULING:
			restTemplate.put(URL_NODERED+"/vpp-agent/scheduling_request", value);
			break;
		case BALANCING:
			restTemplate.put(URL_NODERED+"/vpp-agent/balancing_subscribe", value);
			break;	
		case FRENABLEREQUEST:
			restTemplate.put(URL_NODERED+"/vpp-agent/freqRelay_enable", value);
			break;
		case FRBLOCKREQUEST:
			restTemplate.put(URL_NODERED+"/vpp-agent/freqRelay_block", value);
			break;
		case SCHEDULINGACCEPTED:
			restTemplate.put(URL_NODERED+"/vpp-agent/scheduling_accept-proposal", value);
			break;
		case SCHEDULINGREJECTED:
			restTemplate.put(URL_NODERED+"/vpp-agent/scheduling_reject-proposal", value);
			break;
		case ACCOUNTINGECPRECEIVED:
			restTemplate.put(URL_NODERED+"/vpp-agent/accounting_ecpReceived", value);
			break;	
		case LOADTIMEWINDOWSINFORM:
			restTemplate.put(URL_NODERED+"/vpp-agent/loadTimeWindows_broadcast", value);
			break;	
		default:
			break;
			
		}
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	//Builder that can be used to configure and create a RestTemplate
	//scope limited to the class in which we build it.
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	

}
