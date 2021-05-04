package mas.JADE_VPP;

//This class provides the functionality to consume REST-services (i.e. from Node-Red)


import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

public class ConsumingRest_TU {
	
	
	public void putNodeRed(final String URL_NODERED, PutVariable INJECT, Object value)
	{
		
		//*********PUT-MEthode
		RestTemplate restTemplate = new RestTemplate();
		//InterfacePayload payload = new InterfacePayload();
		switch(INJECT)
		{
		case STARTSCHEDULING:
			restTemplate.put(URL_NODERED+"/erp/scheduling_startScheduling", value);
			break;
		case SCHEDULINGACCEPTED:
			restTemplate.put(URL_NODERED+"/erp/scheduling_accepted", value);
			break;
		case SCHEDULINGREJECTED:
			restTemplate.put(URL_NODERED+"/erp/scheduling_rejected", value);
			break;
		case SETPOINT:
			restTemplate.put(URL_NODERED+"/plc/control_newSetpoint", value);
			break;
		case LOADPROFILE:
			restTemplate.put(URL_NODERED+"/plc/control_newLoadProfile", value);
			break;
		case LOADPROFILEINFO:
			restTemplate.put(URL_NODERED+"/plc/control_newLoadProfileInfo", value);
			break;
		case BALANCING:
			restTemplate.put(URL_NODERED+"/plc/balancing_request", value);
			break;	
		case ACCOUNTINGECPRECEIVED:
			restTemplate.put(URL_NODERED+"/ems/accounting_ecpReceived", value);
			break;	
		case LOADTIMEWINDOWSINFORM:
			restTemplate.put(URL_NODERED+"/erp/loadTimeWindows_inform", value);
			break;
		case FRENABLEREQUEST:
			restTemplate.put(URL_NODERED+"/plc/freqRelay_enable", value);
			break;
		case FRBLOCKREQUEST:
			restTemplate.put(URL_NODERED+"/plc/freqRelay_block", value);
			break;
		case FRCONFIRM:
			restTemplate.put(URL_NODERED+"/plc/freqRelay_confirm", value);
			break;
		case CANCELOPERATIONCONFIRM:
			restTemplate.put(URL_NODERED+"/erp/cancelOperation_confirm", value);
			break;
		case CANCELOPERATION:
			restTemplate.put(URL_NODERED+"/erp/cancelOperation_inform", value);
			break;
		case CANCELOPERATIONFAILED:
			restTemplate.put(URL_NODERED+"/erp/cancelOperation_failure", value);
			break;
		case REQUESTINFOINFORM:
			restTemplate.put(URL_NODERED+"/erp/requestInfoData", value);
			break;
		default:
			break;
			
		}
	}
	
	public byte[] getNodeRed(final String URL_NODERED, PutVariable INJECT)
	{
		//*********GET-Methode
		RestTemplate restTemplate = new RestTemplate();
		byte[] byteResults = new byte[] {(byte)0x00};
		switch(INJECT){
		case REQUESTINFO:
			byteResults = restTemplate.getForObject(URL_NODERED+"/erp/requestInfo", byte[].class);
			break;
		default:
			break;
		}
		return byteResults;
	}
	
	//Builder that can be used to configure and create a RestTemplate
	//scope limited to the class in which we build it.
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	

}
