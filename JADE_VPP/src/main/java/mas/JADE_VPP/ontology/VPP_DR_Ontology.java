package mas.JADE_VPP.ontology;

import jade.content.onto.*;
import jade.content.schema.*;

public class VPP_DR_Ontology extends Ontology{

	private static final long serialVersionUID = 1L;

	public static final String ONTOLOGY_NAME = "VPP-DR-Ontology";
	
	//VOCABULARY
	//General
	public static final String AGENTNAME = "agentName";
	public static final String TUNAME = "tuName";
	
	//Concepts
	public static final String TUDATASET = "TUDataSet";
	public static final String TUDATASET_FEEDIN = "feedIn"; 								//Einspeisung
	public static final String TUDATASET_OPERATINGPOINT = "operatingPoint"; 				//Arbeitspunkt
	public static final String TUDATASET_LEADINGOPERATINGPOINT ="leadingOperatingPoint";	//vorauseilender Arbeitspunkt
	public static final String TUDATASET_CURRENTVALUEFR ="currentValueFR";					//Regelleistungsistwert
	public static final String TUDATASET_ASSIGNEDPOOL ="assignedPool";						//Poolzuordnung
	public static final String TUDATASET_STATUS ="status";									//Status (Meldung)
	public static final String TUDATASET_FREQUENCY ="frequency";							//Frequenz
	public static final String TUDATASET_AFRRSETPOINT ="aFRRsetpoint";						//aFRR-Soll (ÜNB -> POOL)
	public static final String TUDATASET_AFRRSETPOINTECHO ="aFRRsetpointEcho";				//aFRR-Soll-Echo (Pool -> ÜNB)
	public static final String TUDATASET_SETPOINTFR ="setpointFR";							//Regelleistungs-Soll
	public static final String TUDATASET_AFRRGRADIENTPOS ="aFRRGradientPOS";				//aFRR-Gradient POS
	public static final String TUDATASET_AFRRGRADIENTNEG ="aFRRGradientNEG";				//aFRR-Gradient NEG
	public static final String TUDATASET_CAPACITYPOS ="capacityPOS";						//Arbeitsvermögen POS (bei begrenztem Energiespeicher)
	public static final String TUDATASET_CAPACITYNEG ="capacityNEG";						//Arbeitsvermögen NEG (bei begrenzten Energiespeicher)
	public static final String TUDATASET_HOLDINGCAPACITYPOS ="holdingCapacityPOS";			//Aktuelle Vorhalteleistung POS
	public static final String TUDATASET_HOLDINGCAPACITYNEG ="holdingCapacityNEG";			//Aktuelle Vorhalteleistung NEG
	public static final String TUDATASET_CONTROLBANDPOS ="controlBandPOS";					//Regelband POS
	public static final String TUDATASET_CONTROLBANDNEG ="controlBandNEG";					//Regelband NEG
	
	
	//Predicates
	public static final String CFPSCHEDULINGSEQUENCE_REFUSE = "CFPSchedulingSequenceRefuse";
	public static final String CFPSCHEDULINGSEQUENCE_PROPOSE = "CFPSchedulingSequencePropose";
	public static final String CFPSCHEDULINGSEQUENCE_SCHEDULINGPLAN = "schedulingPlan";
	public static final String CFPSCHEDULINGSEQUENCE_ACCEPTPROPOSAL = "CFPSchedulingSequenceAccept";
	public static final String CFPSCHEDULINGSEQUENCE_REJECTPROPOSAL = "CFPSchedulingSequenceReject";
	public static final String CFPSCHEDULINGSEQUENCE_SCHEDULINGSTART = "schedulingStart";
	public static final String CFPSCHEDULINGSEQUENCE_SCHEDULINGEND = "schedulingEnd";
	public static final String CFPSCHEDULINGSEQUENCE_SCHEDULINGDONE = "CFPSchedulingSequenceDone";
	public static final String CFPSCHEDULINGSEQUENCE_SCHEDULINGFAILURE = "CFPSchedulingSequenceFailure";
	
	public static final String TUCONTROLSEQUENCE_FAILURE = "TUControlSequenceFailure";
	
	public static final String BALANCINGSEQUENCE_AGREE = "BalancingSequenceAgree";
	public static final String BALANCINGSEQUENCE_REFUSE = "BalancingSequenceRefuse";
	public static final String BALANCINGSEQUENCE_INFORM = "BalancingSequenceInform";
	public static final String BALANCINGSEQUENCE_FAILURE = "BalancingSequenceFailure";
	
	public static final String ACCOUNTINGSEQUENCE_INFORM = "AccountingSequenceInform";
	public static final String ACCOUNTINGSEQUENCE_INFORMRECEIVED = "AccountingSequenceInformReceived";
	public static final String ACCOUNTINGSEQUENCE_ECP = "energyConsumptionProfile";
	
	public static final String LOADTIMEWINDOWSSHARE_INFORM = "LoadTimeWindowsShareInform";
	public static final String LOADTIMEWINDOWSSHARE_WINDOWHIGHBEGIN = "windowHighBegin";
	public static final String LOADTIMEWINDOWSSHARE_WINDOWHIGHEND = "windowHighEnd";
	public static final String LOADTIMEWINDOWSSHARE_WINDOWLOWBEGIN = "windowLowBegin";
	public static final String LOADTIMEWINDOWSSHARE_WINDOWLOWEND = "windowLowEnd";
	
	public static final String FREQRELAY_ENABLEINFORM = "FreqRelayEnableInform";
	public static final String FREQRELAY_BLOCKINFORM = "FreqRelayBlockInform";
	public static final String FREQRELAY_ENABLEFAILURE = "FreqRelayEnableFailure";
	public static final String FREQRELAY_BLOCKFAILURE = "FreqRelayBlockFailure";
	public static final String FREQRELAY_STATUSINFORM = "FreqRelayStatusInform";
	public static final String FREQRELAY_STATUSCONFIRM = "FreqRelayStatusConfirm";
	
	public static final String CANCELOPERATION_CANCEL = "CancelOperationCancel";
	public static final String CANCELOPERATION_CONFIRM = "CancelOperationConfirm";
	public static final String CANCELOPERATION_FAILURE = "CancelOperationFailure";
	public static final String CANCELOPERATION_REFERENCE = "operationReference";
	
	public static final String REQUESTINFO_INFORM = "RequestInfoInform";
	public static final String REQUESTINFO_DATASET = "infoSet";
	
	//Agent Actions
	public static final String CFPSCHEDULINGSEQUENCE = "CFPSchedulingSequence";
	public static final String CFPSCHEDULINGSEQUENCE_ITEM = "item";
	public static final String CFPSCHEDULINGSEQUENCE_TIME_BEGIN = "timeBegin";
	public static final String CFPSCHEDULINGSEQUENCE_TIME_END = "timeEnd";

	public static final String TUCONTROLSEQUENCE_REQUESTSETPOINT = "TUControlSequenceRequestSetpoint";
	public static final String TUCONTROLSEQUENCE_NEWSETPOINT = "newSetpoint";
	
	public static final String TUCONTROLSEQUENCE_REQUESTLOADPROFILE = "TUControlSequenceRequestLoadProfile";
	public static final String TUCONTROLSEQUENCE_NEWLOADPROFILE = "newLoadProfile";

	
	public static final String BALANCINGSEQUENCE_SUBSCRIBE = "BalancingSequenceSubscribe";
	public static final String BALANCINGSEQUENCE_BALANCINGSTART = "balancingStart";
	public static final String BALANCINGSEQUENCE_BALANCINGEND = "balancingEnd";
	public static final String BALANCINGSEQUENCE_UPDATERATE = "updateRate";
	
	public static final String LOADTIMEWINDOWSSHARE_REQUEST = "LoadTimeWindowsShareRequest";
	public static final String LOADTIMEWINDOWSSHARE_REFERENCE = "loadTimeWindowsReference";
	
	public static final String FREQRELAY_REQUESTENABLE = "FreqRelayEnableRequest";
	public static final String FREQRELAY_REQUESTBLOCK = "FreqRelayBlockRequest";
	
	public static final String REQUESTINFO_REQUEST = "RequestInfoRequest";

	//The singleton instance of this ontology
	private static Ontology theInstance = new VPP_DR_Ontology();
	
	//Retrieve the singleton VPP DR ontology instance
	public static Ontology getInstance() {
		return theInstance;
	}
	
	//Private constructor
	private VPP_DR_Ontology() {
		//the VPP DR ontology extends the basic ontology
		super(ONTOLOGY_NAME, BasicOntology.getInstance());
		
		
		try {
			add(new ConceptSchema(TUDATASET),TUDataSet.class); 
			add(new PredicateSchema(CFPSCHEDULINGSEQUENCE_REFUSE), CFPSchedulingSequenceRefuse.class);
			add(new PredicateSchema (CFPSCHEDULINGSEQUENCE_PROPOSE),CFPSchedulingSequencePropose.class);
			add(new PredicateSchema(CFPSCHEDULINGSEQUENCE_ACCEPTPROPOSAL), CFPSchedulingSequenceAccept.class);
			add(new PredicateSchema (CFPSCHEDULINGSEQUENCE_REJECTPROPOSAL),CFPSchedulingSequenceReject.class);
			add(new PredicateSchema(CFPSCHEDULINGSEQUENCE_SCHEDULINGDONE), CFPSchedulingSequenceDone.class);
			add(new PredicateSchema (CFPSCHEDULINGSEQUENCE_SCHEDULINGFAILURE),CFPSchedulingSequenceFailure.class);
			add(new PredicateSchema (TUCONTROLSEQUENCE_FAILURE),TUControlSequenceFailure.class);
			add(new PredicateSchema (BALANCINGSEQUENCE_AGREE),BalancingSequenceAgree.class);
			add(new PredicateSchema (BALANCINGSEQUENCE_REFUSE),BalancingSequenceRefuse.class);
			add(new PredicateSchema (BALANCINGSEQUENCE_INFORM),BalancingSequenceInform.class);
			add(new PredicateSchema (BALANCINGSEQUENCE_FAILURE),BalancingSequenceFailure.class);
			add(new PredicateSchema (ACCOUNTINGSEQUENCE_INFORM),AccountingSequenceInform.class);
			add(new PredicateSchema (ACCOUNTINGSEQUENCE_INFORMRECEIVED),AccountingSequenceInformReceived.class);
			add(new PredicateSchema (LOADTIMEWINDOWSSHARE_INFORM),LoadTimeWindowsShareInform.class);
			add(new PredicateSchema (FREQRELAY_ENABLEINFORM),FreqRelayEnableInform.class);
			add(new PredicateSchema (FREQRELAY_BLOCKINFORM),FreqRelayBlockInform.class);
			add(new PredicateSchema (FREQRELAY_ENABLEFAILURE),FreqRelayEnableFailure.class);
			add(new PredicateSchema (FREQRELAY_BLOCKFAILURE),FreqRelayBlockFailure.class);
			add(new PredicateSchema (FREQRELAY_STATUSINFORM),FreqRelayStatusInform.class);
			add(new PredicateSchema (FREQRELAY_STATUSCONFIRM),FreqRelayStatusConfirm.class);
			add(new PredicateSchema (CANCELOPERATION_CANCEL),CancelOperationCancel.class);
			add(new PredicateSchema (CANCELOPERATION_CONFIRM),CancelOperationConfirm.class);
			add(new PredicateSchema (CANCELOPERATION_FAILURE),CancelOperationFailure.class);
			add(new PredicateSchema (REQUESTINFO_INFORM),RequestInfoInform.class);
			
			
			add(new AgentActionSchema (CFPSCHEDULINGSEQUENCE),CFPSchedulingSequence.class);
			add(new AgentActionSchema (TUCONTROLSEQUENCE_REQUESTSETPOINT),TUControlSequenceRequestSetpoint.class);
			add(new AgentActionSchema (TUCONTROLSEQUENCE_REQUESTLOADPROFILE),TUControlSequenceRequestLoadProfile.class);
			add(new AgentActionSchema (BALANCINGSEQUENCE_SUBSCRIBE),BalancingSequenceSubscribe.class);
			add(new AgentActionSchema (LOADTIMEWINDOWSSHARE_REQUEST),LoadTimeWindowsShareRequest.class);
			add(new AgentActionSchema (FREQRELAY_REQUESTENABLE),FreqRelayEnableRequest.class);
			add(new AgentActionSchema (FREQRELAY_REQUESTBLOCK),FreqRelayBlockRequest.class);
			add(new AgentActionSchema (REQUESTINFO_REQUEST),RequestInfoRequest.class);
			
			// *************** CONCEPTS
			ConceptSchema cs = (ConceptSchema) getSchema(TUDATASET);
			cs.add(TUDATASET_FEEDIN, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_OPERATINGPOINT, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_LEADINGOPERATINGPOINT, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_CURRENTVALUEFR, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_ASSIGNEDPOOL, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_STATUS, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_FREQUENCY, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_AFRRSETPOINT, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_AFRRSETPOINTECHO, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_SETPOINTFR, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_AFRRGRADIENTPOS, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_AFRRGRADIENTNEG, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_CAPACITYPOS, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_CAPACITYNEG, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_HOLDINGCAPACITYPOS, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_HOLDINGCAPACITYNEG, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_CONTROLBANDPOS, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			cs.add(TUDATASET_CONTROLBANDNEG, (PrimitiveSchema) getSchema(BasicOntology.INTEGER));
			

			
			// *************** PREDICATES
			
			PredicateSchema ps = (PredicateSchema) (getSchema(CFPSCHEDULINGSEQUENCE_REFUSE));
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(CFPSCHEDULINGSEQUENCE_PROPOSE));
			ps.add(CFPSCHEDULINGSEQUENCE_SCHEDULINGPLAN, (PrimitiveSchema) getSchema(BasicOntology.BYTE_SEQUENCE),  ObjectSchema.MANDATORY);
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(CFPSCHEDULINGSEQUENCE_ACCEPTPROPOSAL));
			ps.add(CFPSCHEDULINGSEQUENCE_SCHEDULINGSTART, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(CFPSCHEDULINGSEQUENCE_SCHEDULINGEND, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(CFPSCHEDULINGSEQUENCE_REJECTPROPOSAL));
						
			ps = (PredicateSchema) (getSchema(CFPSCHEDULINGSEQUENCE_SCHEDULINGDONE));
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(CFPSCHEDULINGSEQUENCE_SCHEDULINGFAILURE));
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(TUCONTROLSEQUENCE_FAILURE));
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
					
			ps = (PredicateSchema) (getSchema(BALANCINGSEQUENCE_AGREE));
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);

			ps = (PredicateSchema) (getSchema(BALANCINGSEQUENCE_REFUSE));
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(BALANCINGSEQUENCE_FAILURE));
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(BALANCINGSEQUENCE_INFORM));
			ps.add(TUDATASET, (ConceptSchema) getSchema(TUDATASET),  ObjectSchema.MANDATORY);
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);

			ps = (PredicateSchema) (getSchema(CANCELOPERATION_CANCEL));
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(CANCELOPERATION_REFERENCE, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(CANCELOPERATION_CONFIRM));
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(CANCELOPERATION_REFERENCE, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(CANCELOPERATION_FAILURE));
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(CANCELOPERATION_REFERENCE, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(ACCOUNTINGSEQUENCE_INFORM));
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(ACCOUNTINGSEQUENCE_ECP, (PrimitiveSchema) getSchema(BasicOntology.BYTE_SEQUENCE),  ObjectSchema.MANDATORY);
			
			
			ps = (PredicateSchema) (getSchema(ACCOUNTINGSEQUENCE_INFORMRECEIVED));
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(LOADTIMEWINDOWSSHARE_INFORM));
			ps.add(LOADTIMEWINDOWSSHARE_REFERENCE, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(LOADTIMEWINDOWSSHARE_WINDOWHIGHBEGIN, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(LOADTIMEWINDOWSSHARE_WINDOWHIGHEND, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(LOADTIMEWINDOWSSHARE_WINDOWLOWBEGIN, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(LOADTIMEWINDOWSSHARE_WINDOWLOWEND, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(FREQRELAY_ENABLEINFORM));
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(FREQRELAY_BLOCKINFORM));
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(FREQRELAY_ENABLEFAILURE));
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(FREQRELAY_BLOCKFAILURE));
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(FREQRELAY_STATUSINFORM));
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(FREQRELAY_STATUSCONFIRM));
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			ps = (PredicateSchema) (getSchema(REQUESTINFO_INFORM));
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			ps.add(REQUESTINFO_DATASET, (PrimitiveSchema) getSchema(BasicOntology.BYTE_SEQUENCE),  ObjectSchema.MANDATORY);
			
			// *************** AGENT ACTIONS
			AgentActionSchema as = (AgentActionSchema) (getSchema(CFPSCHEDULINGSEQUENCE));
			as.add(CFPSCHEDULINGSEQUENCE_TIME_BEGIN, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			as.add(CFPSCHEDULINGSEQUENCE_TIME_END, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			as = (AgentActionSchema) (getSchema(TUCONTROLSEQUENCE_REQUESTSETPOINT));
			as.add(TUCONTROLSEQUENCE_NEWSETPOINT, (PrimitiveSchema) getSchema(BasicOntology.INTEGER),  ObjectSchema.MANDATORY);
			as.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);

			
			as = (AgentActionSchema) (getSchema(TUCONTROLSEQUENCE_REQUESTLOADPROFILE));
			as.add(TUCONTROLSEQUENCE_NEWLOADPROFILE, (PrimitiveSchema) getSchema(BasicOntology.BYTE_SEQUENCE),  ObjectSchema.MANDATORY);
			as.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			
			as = (AgentActionSchema) (getSchema(BALANCINGSEQUENCE_SUBSCRIBE));
			as.add(BALANCINGSEQUENCE_BALANCINGSTART, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			as.add(BALANCINGSEQUENCE_BALANCINGEND, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			as.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			as.add(BALANCINGSEQUENCE_UPDATERATE, (PrimitiveSchema) getSchema(BasicOntology.INTEGER),  ObjectSchema.OPTIONAL);
			
			as = (AgentActionSchema) (getSchema(LOADTIMEWINDOWSSHARE_REQUEST));
			as.add(LOADTIMEWINDOWSSHARE_REFERENCE, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
	
			
			as = (AgentActionSchema) (getSchema(FREQRELAY_REQUESTENABLE));
			as.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			as = (AgentActionSchema) (getSchema(FREQRELAY_REQUESTBLOCK));
			as.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			
			as = (AgentActionSchema) (getSchema(REQUESTINFO_REQUEST));
			ps.add(AGENTNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
			as.add(TUNAME, (PrimitiveSchema) getSchema(BasicOntology.STRING),  ObjectSchema.MANDATORY);
	
			
		}
		catch(OntologyException oe){
			oe.printStackTrace();
			
		}
	
	}
}
