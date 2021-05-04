package mas.JADE_VPP;

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
		case STARTSCHEDULING:
			restTemplate.put(URL_NODERED, value);
			break;
		case SCHEDULINGPLANINFOS:
			restTemplate.put(URL_NODERED+"/vpp/scheduling_schedulingPlanInfos", value);
			break;
		case SCHEDULINGPLAN:
			restTemplate.put(URL_NODERED+"/vpp/scheduling_schedulingPlan", value);
			break;
		case SCHEDULINGREFUSE:
			restTemplate.put(URL_NODERED+"/vpp/scheduling_refuse", value);
			break;
		case SCHEDULINGDONE:
			restTemplate.put(URL_NODERED+"/vpp/scheduling_done", value);
			break;
		case SCHEDULINGFAILURE:
			restTemplate.put(URL_NODERED+"/vpp/scheduling_failure", value);
			break;
		case CONTROLFAILURE:
			restTemplate.put(URL_NODERED+"/vpp/control_failure", value);
			break;
		case BALANCINGREFUSE:
			restTemplate.put(URL_NODERED+"/vpp/balancing_refuse", value);
			break;
		case BALANCINGAGREE:
			restTemplate.put(URL_NODERED+"/vpp/balancing_agree", value);
			break;
		case BALANCINGINFORM:
			restTemplate.put(URL_NODERED+"/vpp/balancing_inform", value);
			break;
		case BALANCINGFAILURE:
			restTemplate.put(URL_NODERED+"/vpp/balancing_failure", value);
			break;
		case ENERGYCONSUMPTIONPROFILE:
			restTemplate.put(URL_NODERED+"/vpp/accounting_energyConsumptionProfile", value);
			break;
		case ACCOUNTINGECPREFERENCE:
			restTemplate.put(URL_NODERED+"/vpp/accounting_energyConsumptionProfileReference", value);
			break;
		case LOADTIMEWINDOWSREQUEST:
			restTemplate.put(URL_NODERED+"/vpp/loadTimeWindows_request", value);
			break;
		case FRENABLEFAILURE:
			restTemplate.put(URL_NODERED+"/vpp/freqRelay_enableFailure", value);
			break;
		case FRENABLEINFORM:
			restTemplate.put(URL_NODERED+"/vpp/freqRelay_enableInform", value);
			break;
		case FRBLOCKFAILURE:
			restTemplate.put(URL_NODERED+"/vpp/freqRelay_blockFailure", value);
			break;
		case FRBLOCKINFORM:
			restTemplate.put(URL_NODERED+"/vpp/freqRelay_blockInform", value);
			break;
		case FRTRIGGERED:
			restTemplate.put(URL_NODERED+"/vpp/freqRelay_triggered", value);
			break;
		case AGENTDEAD:
			restTemplate.put(URL_NODERED+"/vpp/ams_agentDead", value);
			break;
		case AGENTBORN:
			restTemplate.put(URL_NODERED+"/vpp/ams_agentBorn", value);
			break;
		case CANCELOPERATION:
			restTemplate.put(URL_NODERED+"/vpp/cancelOperation_inform", value);
			break;
		case CANCELOPERATIONCONFIRM:
			restTemplate.put(URL_NODERED+"/vpp/cancelOperation_confirm", value);
			break;
		case CANCELOPERATIONFAILED:
			restTemplate.put(URL_NODERED+"/vpp/cancelOperation_failure", value);
			break;
		case REQUESTINFOINFORM:
			restTemplate.put(URL_NODERED+"/vpp/requestInfoData", value);
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
			byteResults = restTemplate.getForObject(URL_NODERED+"/vpp/requestInfo", byte[].class);
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
