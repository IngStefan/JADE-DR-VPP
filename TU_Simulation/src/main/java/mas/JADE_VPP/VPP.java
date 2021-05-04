package mas.JADE_VPP;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import jade.content.ContentElement;
import jade.content.ContentElementList;
import jade.content.ContentManager;
import jade.content.Predicate;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.ThreadedBehaviourFactory;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.introspection.BornAgent;
import jade.domain.introspection.DeadAgent;
import jade.domain.introspection.Event;
import jade.domain.introspection.IntrospectionVocabulary;
import jade.domain.introspection.AMSSubscriber;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.JADE_VPP.ontology.*;

public class VPP extends Agent{
	private static final long serialVersionUID = 1L;
	private static long idCounter = 0;			//ID Counter for the messages (64 bit from: -9 223 372 036 854 775 808  to: +9 223 372 036 854 775 807)
					
	private Codec codec = new SLCodec();									//adds a 
	//private Codec codec2 = new LEAPCodec();								//required for data transfer via bytesequences (probalby not needed)
	private Ontology ontology = VPP_DR_Ontology.getInstance();
	private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory(); //tbf is needed for extra thread generation, which is needed to prevent a cpu usage of 100%
	private HashMap<String,String> idPairsScheduling = new HashMap<String,String>();
	private HashMap<String,String> idPairsBalancing = new HashMap<String,String>();
	//private ArrayList <String> agentList = new ArrayList<String>();			//manages the agents that have sent a scheduling plan
	
	//*********** Agent startup ***********
	protected void setup() {
		//Printout a Welcome Message
		System.out.println("********* VPP-Agent online: " + getAID().getLocalName()+ " *********"); 		
		//register agent with its content manager to be able to use the ontology and content language
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		//setting up and register / publish own service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		dfd.addLanguages(codec.getName());
		dfd.addOntologies(ontology.getName());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getAID().getLocalName());
		sd.setType("VPP");
		dfd.addServices(sd);
		try{
			DFService.register(this, dfd);
		}
		catch(FIPAException fe){
			fe.printStackTrace();
		}	
		//receiving arguments from the Startup.java
//		Object[] args = getArguments();
//		if (args != null && args.length > 0){
//			int randomNum = ThreadLocalRandom.current().nextInt(0, 13 + 1);
//			requestedEnergy = (Integer) args[randomNum];
//			System.out.println("Requested Energy is "+requestedEnergy);
//		}
//		else{
//			System.out.println("VPP Agent shutting down");
//			doDelete();
//		}
		
		//start the agent behaviours
		addBehaviour(new VPPManager());
		addBehaviour(tbf.wrap(new VPPInputListener()));		//tbf.wrap starts an extra task for the behaviour added (so this task can go into wait later on)	
		//subscribe to AMS Platform Events
		addBehaviour(new myAMSSubscriber());
	
	}
	
	//*********** Agent shutdown ***********
	protected void takdeDown(){
		System.out.println("VPP Agent " +getAID().getName() +"terminating.");
		tbf.interrupt();
		try {
			DFService.deregister(this);
			}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	
	//********************** 0. AMS Subscriber **********************
	//Informs the VPP about new agents and agents that are leaving the platform
	private class myAMSSubscriber extends AMSSubscriber{
		private static final long serialVersionUID = 1L;
		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected void installHandlers(Map handlers) {
			EventHandler creationsHandler = new EventHandler() {
				private static final long serialVersionUID = 1L;
				public void handle(Event ev) {
					BornAgent ba = (BornAgent) ev;
					System.out.println("Born agent "+ba.getAgent().getName());
					ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
					putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.AGENTBORN,ba.getAgent().getName());
				}
			};
			handlers.put( IntrospectionVocabulary.BORNAGENT, creationsHandler);			
			EventHandler terminationsHandler = new EventHandler() {
				private static final long serialVersionUID = 1L;
				public void handle(Event ev) {
					DeadAgent da = (DeadAgent) ev;
					System.out.println("Dead agent "+da.getAgent().getName());
					ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
					putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.AGENTDEAD,da.getAgent().getName());
				}
			};
			handlers.put(IntrospectionVocabulary.DEADAGENT, terminationsHandler);
		}
	}

	//********************** 0. VPP Manager (Main behaviour) **********************
	//Manages incoming agent messages
	private class VPPManager extends CyclicBehaviour{
		private static final long serialVersionUID = 1L;

		public void action(){
			//******************* Collecting Messages from other Agents ************
			//******** Handling unknown messages: ****************
			MessageTemplate mt = MessageTemplate.not(MessageTemplate.MatchOntology(ontology.getName())); //filter for messages that dont use the VPP_DR_Ontology
			ACLMessage msg = receive(mt); 	//returns the first message of the message queue with the corresponding template
			if (msg != null){				//if a proper message can be found
				//********** every Message that does not use the VPP_DR_Ontology can not be understood ********
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Message Received that cannot be understood *******");
				ACLMessage reply = msg.createReply();	
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
				myAgent.send(reply);
			}
			
			//******** Handling messages of the TU control sequence ************
			mt = MessageTemplate.and(
					MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
					MessageTemplate.MatchConversationId("tuControlSequenceFailed"),MessageTemplate.and(
					MessageTemplate.MatchLanguage(codec.getName()),	MessageTemplate.MatchPerformative(ACLMessage.FAILURE))));				
			msg = receive(mt);		
			if (msg != null){	
				System.out.println(this.getAgent().getAID().getLocalName()+"******* FAILURE Message Received *******");
				try {
					ContentElement ce = getContentManager().extractContent(msg);
					Predicate _pc = (Predicate) ce;
					if(_pc instanceof TUControlSequenceFailure){
						TUControlSequenceFailure _csf = (TUControlSequenceFailure)_pc;
						InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_csf.getAgentName(),_csf.getTuName());
						ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
						putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.CONTROLFAILURE, payload);
					}
				} catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
			}
			
			//******** Handling messages of the Accounting sequence ************
			mt = MessageTemplate.and(
					MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
					MessageTemplate.MatchLanguage(codec.getName()),MessageTemplate.and(
							MessageTemplate.MatchConversationId("accountingInform"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM))));
			msg = receive(mt);		
			if (msg != null){	
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Accounting Sequence: INFORM Message Received *******");
				try {
					ContentElement ce;
					Predicate _pc;
					ce = getContentManager().extractContent(msg);
					_pc = (Predicate) ce;
					if(_pc instanceof AccountingSequenceInform){
						addBehaviour(new AccountingSequencePerformer(msg.shallowClone()));
					}
					
				} catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
			}
			
			//******** Handling messages of the loadTimeWindows Sharing ************
			mt = MessageTemplate.and(
					MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
					MessageTemplate.MatchLanguage(codec.getName()),MessageTemplate.and(
							MessageTemplate.MatchConversationId("loadTimeWindows"),
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST))));
			msg = receive(mt);	
			if (msg != null){
				System.out.println(this.getAgent().getAID().getLocalName()+"******* LoadTimeWindows Sharing: REQUEST Message Received *******");
				try {
					ContentElement ce = getContentManager().extractContent(msg);
					Action _ac = (Action) ce;
					if(_ac.getAction() instanceof LoadTimeWindowsShareRequest){
						addBehaviour(new LoadTimeWindowsRequestPerformer(msg.shallowClone()));
					}
				}catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
			}
			
			//******** Handling messages of the Frequency Relay Status sequence ************
			mt = MessageTemplate.and(
					MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
					MessageTemplate.MatchLanguage(codec.getName()),MessageTemplate.and(
							MessageTemplate.MatchConversationId("frequencyRelayTriggered"),
					MessageTemplate.MatchPerformative(ACLMessage.INFORM))));
			msg = receive(mt);		
			if (msg != null){	
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Frequency Relay Status: INFORM Message Received *******");
				try {
					ContentElement ce;
					Predicate _pc;
					ce = getContentManager().extractContent(msg);
					_pc = (Predicate) ce;
					if(_pc instanceof FreqRelayStatusInform){
						FreqRelayStatusInform _frsi = (FreqRelayStatusInform)_pc;
						addBehaviour(new FRStatusPerformer(msg.shallowClone(),_frsi.getTuName(), _frsi.getAgentName()));
					}
				} catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
			}
			
			//******** Handling messages of the RequestInfo Requests ************
			mt = MessageTemplate.and(
					MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
					MessageTemplate.MatchLanguage(codec.getName()),MessageTemplate.and(
							MessageTemplate.MatchConversationId("requestInfoOperation"),
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST))));
			msg = receive(mt);	
			if (msg != null){
				System.out.println(this.getAgent().getAID().getLocalName()+"******* RequestInfo Request : REQUEST Message Received *******");
				try {
					ContentElement ce = getContentManager().extractContent(msg);
					Action _ac = (Action) ce;
					if(_ac.getAction() instanceof RequestInfoRequest){
						addBehaviour(new RequestInfoResponsePerformer(msg.shallowClone()));
					}
				}catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
			}
			
			//******** Handling messages of the Cancel Operation ************
			mt = MessageTemplate.and(
					MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
					MessageTemplate.MatchLanguage(codec.getName()),MessageTemplate.and(
					MessageTemplate.MatchConversationId("cancelOperation"),
					MessageTemplate.MatchPerformative(ACLMessage.CANCEL))));
			msg = receive(mt);		
			if (msg != null){	
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Cancel-Operation Sequence: CANCEL Message Received *******");
				try {
					ContentElement ce;
					Predicate _pc;
					ce = getContentManager().extractContent(msg);
					_pc = (Predicate) ce;
					if(_pc instanceof CancelOperation){
						addBehaviour(new ReceiveCancelOperationPerformer(msg.shallowClone()));
					}
				} catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
			}	else {
			//******** going into Block (suspends the behaviour till a new message appears) for every other type of message ************
				block();
			}
		}		
	}	

	//********************** 0. VPP Input Listener **********************
	//Manages incoming webservice requests from the ProducingRest_VPP class
	private class VPPInputListener extends CyclicBehaviour{
		//this input listener is called as an threaded behaviour so the extra thread can go into wait and resume if the rest ws notifies via the LOCK
		//it processes the Node-RED inputs and this threaded approach is needed to prevent an increased CPU usage 
		private static final long serialVersionUID = 1L;

		public void action() {
			synchronized(VppVariables.LOCK){
				try {
					VppVariables.LOCK.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				//******************* Starting the Scheduling Sequence (generic behaviour) ***************
				if (VppVariables.schedulingRequestTrigger == true) {
					VppVariables.schedulingRequestTrigger = false;
					myAgent.addBehaviour(new SchedulingSequencePerformer(VppVariables.schedulingStart,VppVariables.schedulingEnd, 
							VppVariables.expiration, VppVariables.referenceID, VppVariables.serviceDescriptions));
					VppVariables.resetScheduling();
				}
				
				//******************* Starting the TU Control Sequence (one shot behaviour) ***************
				if (VppVariables.newSetpointTrigger) {
					VppVariables.newSetpointTrigger = false;
					myAgent.addBehaviour(new ControlSequencePerformer(VppVariables.newSetpoint,VppVariables.tuName));
					VppVariables.resetNewSetpoint();
				}
				
				if (VppVariables.newLoadProfileTriggerUpdated) {
					VppVariables.newLoadProfileTriggerUpdated = false;
					myAgent.addBehaviour(new ControlSequencePerformer(VppVariables.newLoadProfile,VppVariables.tuName));
					VppVariables.resetNewLoadProfile();
				}
				
				//******************* Starting Balancing Sequence (generic behaviour) ***************
				if (VppVariables.balancingTrigger) {
					VppVariables.balancingTrigger = false;
					myAgent.addBehaviour(new BalancingSequencePerformer(VppVariables.balancingStart,VppVariables.balancingEnd,
							VppVariables.balancingTUName, VppVariables.balancingUpdateRate, VppVariables.balancingReferenceID));
					VppVariables.resetBalancing();
				}
				
				//******************* Starting loadTimeWindowsBroadcast Sequence (one shot behaviour) ***************
				if (VppVariables.loadTimeWindowsBroadcastTrigger) {
					VppVariables.loadTimeWindowsBroadcastTrigger = false;
					myAgent.addBehaviour(new LoadTimeWindowsBroadcastPerformer(
							VppVariables.loadTimeWindowsReference,VppVariables.windowHighBegin,
							VppVariables.windowHighEnd ,VppVariables.windowLowBegin ,VppVariables.windowLowEnd ));
					VppVariables.resetloadTimeWindows();
				}
				
				//******************* Starting Frequency Relay Enable Sequence (generic behaviour) ***************
				if (VppVariables.freqRelayEnableTrigger) {
					VppVariables.freqRelayEnableTrigger = false;
					myAgent.addBehaviour(new FREnablePerformer(VppVariables.freqRelayEnableTUName));
					VppVariables.resetfreqRelayEnable();
				}
				
				//******************* Starting Frequency Relay Block Sequence (generic behaviour) ***************
				if (VppVariables.freqRelayDisableTrigger) {
					VppVariables.freqRelayDisableTrigger = false;
					myAgent.addBehaviour(new FRBlockPerformer(VppVariables.freqRelayDisableTUName));
					VppVariables.resetfreqRelayEnable();
				}
				//******************* Starting the operationCancel Sequence (generic behaviour) ***************
				if (VppVariables.sendCancelOperationTrigger) {
					VppVariables.sendCancelOperationTrigger = false;
					myAgent.addBehaviour(new SendCancelOperationPerformer(VppVariables.sendCancelOperationTuName, VppVariables.sendCancelOperationReference));
				}
				
				//******************* Starting the RequestInfos Sequence (generic behaviour) ***************
				if (VppVariables.requestInfosTrigger) {
					VppVariables.requestInfosTrigger = false;
					myAgent.addBehaviour(new RequestInfoPerformer(VppVariables.requestInfosTuName));
				}
				
			}
		}
	}
	
	//********************** 1. Scheduling Sequence **********************
	private class SchedulingSequencePerformer extends Behaviour{
		private static final long serialVersionUID = 1L;
		
		private int step = 0;								
		private int agentAmount = 0;
		private int responseCounter = 0;
		private int receivedMessages = 0;
		private String schedulingStart;
		private String schedulingEnd;
		private String expiration;
		private String referenceID;
		private String conversationID = "empty";
		private Date date;		//offer expiration date
		private ArrayList <AID> loadAgentList = new ArrayList<AID>();
		private TreeMap<String, AID> agentIDOfferList = new TreeMap<String, AID>();
		private ArrayList <String> sdList = new ArrayList <String>();	
		private ArrayList <String> agentList = new ArrayList<String>();
		
	
		//constructor that prepares the schedulingSequencePerformer
		public SchedulingSequencePerformer(String _schedulingStart, String _schedulingEnd,
				String _expiration, String _referenceID, String _serviceDescriptions) {	
			schedulingStart = _schedulingStart;
			schedulingEnd = _schedulingEnd;
			expiration = _expiration;
			referenceID = _referenceID;
			
			_serviceDescriptions = _serviceDescriptions.replaceAll("\\s+","");	//remove all whitespaces
			List <String> serviceDescriptionList = new ArrayList<String>();
			serviceDescriptionList = Arrays.asList(_serviceDescriptions.split(","));	
			for (int i = 0; i < serviceDescriptionList.size(); i++){
				sdList.add(serviceDescriptionList.get(i));
			}
		}
		
		public void onStart(){
			idCounter++;
			conversationID = ("scheduling-"+idCounter);
			System.out.println(this.getAgent().getAID().getLocalName()+"******* SchedulingSequencePerformer started ********");
			//before executing the scheduling sequence, checking the if the service descriptions have been set, if not: abort
			String empty = "empty";
			if(sdList.get(0).equals(empty)){
				System.out.println("********** No ServiceDescription set, abort SchedulingSequence. ************");
				step=99;
			}
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
			formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
			try {
				date = formatter.parse(expiration);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		public void action(){
			switch(step){
			//********** searching the DF for the suitable Agents **********
			case 0: 
				//pairing the referenceID from the VPP to the conversationID given by the AgentSystem
				idPairsScheduling.put(referenceID, conversationID);
				//setting up the service description that will be searched
				DFAgentDescription sdSearchTemplate = new DFAgentDescription() ;		//contains the service description list that the schedulingSequence uses
				sdSearchTemplate.addLanguages(codec.getName());
				sdSearchTemplate.addOntologies(ontology.getName());
				DFAgentDescription sdSearchTemplate_pla = new DFAgentDescription() ;
				sdSearchTemplate_pla.addLanguages(codec.getName());
				sdSearchTemplate_pla.addOntologies(ontology.getName());
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Will search for: ********");
				for(int i = 0; i < sdList.size(); i++){
					ServiceDescription sd = new ServiceDescription();
					sd.setType(sdList.get(i));
					System.out.println("****** Service Description: "+sd.getType()+" ********");
					sdSearchTemplate.addServices(sd);
					//adding the planning specific services (for the agents that exclusively plan)
					ServiceDescription sd_pla = new ServiceDescription();
					sd_pla.setType(sdList.get(i)+"_pla");
					sdSearchTemplate_pla.addServices(sd_pla);
					//searching the DF for the Agents that can provide the service
					try {
						DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
						System.out.println("******* Found the following load agents: ********");
						//loadAgent = new AID[result.length];
						for(int j = 0; j < result.length; j++){
							//loadAgent[j] = result[j].getName();
							loadAgentList.add(result[j].getName());
							System.out.println("******* "+result[j].getName()+" ********");
						}
						//doing the same search but now with the planning specific services
						DFAgentDescription[] result_pla = DFService.search(myAgent, sdSearchTemplate_pla);
						for(int j = 0; j < result_pla.length; j++){
							loadAgentList.add(result_pla[j].getName());
							System.out.println("******* "+result_pla[j].getName()+" ********");
						}
					}
					catch (FIPAException fe){
						fe.printStackTrace();
					}
					sdSearchTemplate.clearAllServices();
					sdSearchTemplate_pla.clearAllServices();
				}
				if(loadAgentList.size()>0) {
					step = 1;
				}else {
					System.out.println("No Agent can be found with that service description");
					step = 99;
				}
				step = 1;
				break;
			case 1: 
				//********** sending out the CFP message to the agents that can provide the service
				ACLMessage msg = new ACLMessage(ACLMessage.CFP);
				agentAmount = loadAgentList.size();
				for(int i = 0; i < loadAgentList.size(); i++){
					msg.addReceiver(loadAgentList.get(i));
				}
				loadAgentList.clear();
				msg.setOntology(ontology.getName());
				msg.setLanguage(codec.getName());
				msg.setConversationId(conversationID);
				msg.setReplyByDate(date);
				try {
					ContentManager cm = myAgent.getContentManager();
					ContentElementList cel = new ContentElementList();
					CFPSchedulingSequence newSchedulingSequence = new CFPSchedulingSequence();
					newSchedulingSequence.setTimeBegin(schedulingStart);
					newSchedulingSequence.setTimeEnd(schedulingEnd);
					Action act = new Action();
					act.setAction(newSchedulingSequence); 			//Adding the Action the Agent has to perform
					act.setActor(new AID("*", AID.ISGUID));			//Adding an dummy Agent, because the actor field is mandatory (source: http://jade.tilab.com/pipermail/jade-develop/2010q4/016200.html)
					cel.add(act);
					cm.fillContent(msg, cel);
					myAgent.send(msg);
					msg.reset();
				}
				catch (OntologyException oe){
					oe.printStackTrace();
				}
				catch (CodecException ce){
					ce.printStackTrace();
				}
				step = 2;
				break;
			case 2:
				//************* collecting responses from the TUs **************** 
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
						MessageTemplate.MatchConversationId(conversationID),MessageTemplate.and(
						MessageTemplate.MatchLanguage(codec.getName()),MessageTemplate.or(
						MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
						MessageTemplate.MatchPerformative(ACLMessage.REFUSE)))));

				msg = receive(mt);		//returns the first message of the message queue with the corresponding template
				if (msg != null){	
						receivedMessages++;
						int substep = msg.getPerformative();
						ContentElement ce = null;
						InterfacePayloadAgentReference payload;
						switch(substep){
							case (ACLMessage.PROPOSE):
							System.out.println(this.getAgent().getAID().getLocalName()+"******* PROPOSE Message Received from "+msg.getSender().getLocalName()+" *******");
							try {
								ce = getContentManager().extractContent(msg);
								Predicate _pc = (Predicate) ce;
								if(_pc instanceof CFPSchedulingSequencePropose){
									CFPSchedulingSequencePropose _cfpp = (CFPSchedulingSequencePropose)_pc;
									// connecting the offer to the agent ID with the conversation referenceID
									//idPairsScheduling.put(referenceID, conversationID);
									agentIDOfferList.put((msg.getSender().getLocalName()+", "+referenceID), msg.getSender());
									payload = new InterfacePayloadAgentReference(msg.getSender().getLocalName()+", "+referenceID,_cfpp.getAgentName(), _cfpp.getTuName()); 
									ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
									putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.SCHEDULINGPLANINFOS, payload);
									// Saving / sending the propose with the scheduling plan to the VPP
									byte[] buffer;
									buffer = _cfpp.getSchedulingPlan();			
									putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.SCHEDULINGPLAN, buffer);
								}
							} catch (CodecException | OntologyException e) {
								e.printStackTrace();
								step = 99;
							}
							break;
							case (ACLMessage.REFUSE):
							System.out.println(this.getAgent().getAID().getLocalName()+"******* REFUSE Message Received from "+msg.getSender().getLocalName()+" *******");
							try {
								ce = getContentManager().extractContent(msg);
								Predicate _pc = (Predicate) ce;
								if(_pc instanceof CFPSchedulingSequenceRefuse){
									CFPSchedulingSequenceRefuse _cfpr = (CFPSchedulingSequenceRefuse)_pc;
									payload = new InterfacePayloadAgentReference(msg.getSender().getLocalName()+", "+referenceID,_cfpr.getAgentName(), _cfpr.getTuName());
									ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
									putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.SCHEDULINGREFUSE, payload);
								}
							} catch (CodecException | OntologyException e) {
								e.printStackTrace();
								step = 99;
							}
							break;
							default:
							System.out.println(this.getAgent().getAID().getName()+"******* nothing *******");
							}	
						if(receivedMessages >= agentAmount ) {
							step = 3;
							receivedMessages = 0;
							agentAmount = 0;
						}					
				} else {
					//checking if the expiration time for the offer transfer is over, then move to the next step 
					Date dateNow = new Date();
					long t= date.getTime();
					Date afterAddingOneMin=new Date(t + (1 * 60000));
					if(dateNow.after(afterAddingOneMin)) {
						if(receivedMessages == 0) {
							step = 99;
						}else {
							step = 3;
						}
						receivedMessages = 0;
						agentAmount = 0;
					}
					block(); //behaviour is ‘blocked’ so that the agent no longer schedules it for execution.
				}
				break;
			case 3:
				//************* sending out new accept- and reject-proposals **************** 
				//************* accept-proposals **************
				boolean atLeastOneAccepted = false;
				if(VppVariables.schedulingAcceptTrigger){
					VppVariables.schedulingAcceptTrigger = false;
					atLeastOneAccepted = true;
					//checking if the answer from the VPP is referencing to one of these conversations
					if(agentIDOfferList.containsKey(VppVariables.schedulingPlanReference)) {
						if(!agentList.contains(VppVariables.schedulingPlanReference)) {
							responseCounter++;
							agentList.add(VppVariables.schedulingPlanReference);
						}
						System.out.println(this.getAgent().getAID().getLocalName()+"******* "+VppVariables.schedulingPlanReference+" accepted *******");
						//*********** Sending accept-proposal to the corresponding agent ***********
						msg = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
						msg.setOntology(ontology.getName());
						msg.setLanguage(codec.getName());
						msg.setConversationId(conversationID);	
						AID receiver = new AID();
						receiver = agentIDOfferList.get(VppVariables.schedulingPlanReference);
						msg.addReceiver(receiver);	
						try {
							ContentManager cm = myAgent.getContentManager();
							ContentElementList cel = new ContentElementList();
							CFPSchedulingSequenceAccept newSchedulingSequenceAccept = new CFPSchedulingSequenceAccept();
							newSchedulingSequenceAccept.setSchedulingStart(VppVariables.schedulingStart);
							newSchedulingSequenceAccept.setSchedulingEnd(VppVariables.schedulingEnd);
							cel.add(newSchedulingSequenceAccept);
							cm.fillContent(msg, cel);
							myAgent.send(msg);
							agentAmount++;									//increase the agent amount counter to wait for the correct amount of agents that responds later
							msg.reset();
						} catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					}	
				}//************* reject-proposals **************
				else if (VppVariables.schedulingRejectTrigger) {
					VppVariables.schedulingRejectTrigger = false;
					if(agentIDOfferList.containsKey(VppVariables.schedulingPlanReference)) {
						if(!agentList.contains(VppVariables.schedulingPlanReference)) {
							responseCounter++;
							agentList.add(VppVariables.schedulingPlanReference);
						}
						System.out.println(this.getAgent().getAID().getLocalName()+"******* "+VppVariables.schedulingPlanReference+" rejected *******");
						//*********** Sending reject-proposal to the corresponding agent ***********
						msg = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
						msg.setOntology(ontology.getName());
						msg.setLanguage(codec.getName());
						msg.setConversationId(conversationID);	
						AID receiver = new AID();
						receiver = agentIDOfferList.get(VppVariables.schedulingPlanReference);
						msg.addReceiver(receiver);	
						try {
							ContentManager cm = myAgent.getContentManager();
							ContentElementList cel = new ContentElementList();
							CFPSchedulingSequenceReject newSchedulingSequenceReject = new CFPSchedulingSequenceReject();
							cel.add(newSchedulingSequenceReject);
							cm.fillContent(msg, cel);
							myAgent.send(msg);
	////////////////////////////
							//agentAmount++;									//increase the agent amount counter to wait for the correct amount of agents that responds later
							msg.reset();
						} catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					}
				}
				//if all answers have been sent, change to the next state
				if((responseCounter >= agentIDOfferList.size())) {
					if(!atLeastOneAccepted) {		//if no agent has been accepted, end the behaviour
						step = 99;
					}else {
					step = 4;
					}
				}
				break;
			case 4:
				//************* collecting responses from the TUs **************** 
				mt = MessageTemplate.and(
						MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
						MessageTemplate.MatchConversationId(conversationID),MessageTemplate.and(
						MessageTemplate.MatchLanguage(codec.getName()),	MessageTemplate.or(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchPerformative(ACLMessage.FAILURE)))));
								
				msg = receive(mt);		//returns the first message of the message queue with the corresponding template
				if (msg != null){	
						
						int substep = msg.getPerformative();
						ContentElement ce = null;
						InterfacePayloadAgentReference payload;
						switch(substep){
							case (ACLMessage.INFORM):
							System.out.println(this.getAgent().getAID().getLocalName()+"******* INFORM Message received from: "+msg.getSender().getLocalName()+ " *******");
	////////////////////////////
							receivedMessages++;
							try {
								//msg.getConversationId();
								ce = getContentManager().extractContent(msg);
								Predicate _pc = (Predicate) ce;
								if(_pc instanceof CFPSchedulingSequenceDone){
									CFPSchedulingSequenceDone _cfpd = (CFPSchedulingSequenceDone)_pc;
									payload = new InterfacePayloadAgentReference(msg.getSender().getLocalName()+", "+referenceID,_cfpd.getAgentName(), _cfpd.getTuName());
									//********** Inform the VPP-System about the results**********
									//******* sending the INFROM info to the VPP ******** 
									ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
									putInstance = new ConsumingRest_VPP();
									putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.SCHEDULINGDONE, payload);
								}else {
									step = 99;
								}	
							} catch (CodecException | OntologyException e) {
								e.printStackTrace();
								step = 99;
							}
							break;
							case (ACLMessage.FAILURE):
							System.out.println(this.getAgent().getAID().getLocalName()+"******* FAILURE Message received from: "+msg.getSender().getLocalName()+ " *******");
	////////////////////////////
							receivedMessages++;
							try {
								//msg.getConversationId();
								ce = getContentManager().extractContent(msg);
								Predicate _pc = (Predicate) ce;
								if(_pc instanceof CFPSchedulingSequenceFailure){
									CFPSchedulingSequenceFailure _cfpf = (CFPSchedulingSequenceFailure)_pc;
									//********** Inform the VPP-System about the results**********
									//******* sending the FAILURE info to the VPP ******** 
									payload = new InterfacePayloadAgentReference(msg.getSender().getLocalName()+", "+referenceID,_cfpf.getAgentName(), _cfpf.getTuName());
									ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
									putInstance = new ConsumingRest_VPP();
									putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.SCHEDULINGFAILURE, payload);
								}else {
									step = 99;
								}
							}catch (CodecException | OntologyException e) {
								e.printStackTrace();
								step = 99;
							}
							break;
							default:
							System.out.println(this.getAgent().getAID().getLocalName()+"******* nothing *******");
							}					
				}else {
					block();
				}
				if(receivedMessages >= agentAmount) {
					step = 99;
				}
				break;
			case 99: //final case, here reset() must be called. A reset() in done() would result in a reset() call every cycle, because done() gets called every cycle
				reset(); 				//any Behaviour object that has been executed once, must be	reset by calling its reset() method before it can be executed again.
				step = 100;
				break;
			default:
				step = 99;
			}
		}
		
		public boolean done() {
			return step == 100;			//the agent behaviour action stops if step == 10
		}
	}

	//********************** 2. TU Control Sequence **********************
	private class ControlSequencePerformer extends OneShotBehaviour{
		private static final long serialVersionUID = 1L;
		private boolean newSetpointVersion = false;
		private int newSetpoint = 0;
		private String tuName = "noNameSet";
		private String conversationID = "noID";
		private ArrayList <AID> agentIdentifiers = new ArrayList<AID>();
		private byte[] newLoadProfile;
		
		public ControlSequencePerformer(int _newSetpoint, String _tuName) {	
			tuName = _tuName;
			newSetpoint = _newSetpoint;
			newSetpointVersion = true;
		}
		
		public ControlSequencePerformer(byte[] _newLoadProfile, String _tuName) {	
			tuName = _tuName;
			newLoadProfile = _newLoadProfile;
		}
		
		public void onStart(){
			idCounter++;
			conversationID = ("loadControl-"+idCounter);
			System.out.println(this.getAgent().getAID().getLocalName()+"******* TUControlSequencePerformer started ********");
		}
	
		public void action() {
			//********** searching the DF for the Agent **********
			//searching for the complete AID of the referenced agent
			DFAgentDescription sdSearchTemplate = new DFAgentDescription() ;		//contains the service description list that the schedulingSequence uses
			sdSearchTemplate.addLanguages(codec.getName());
			sdSearchTemplate.addOntologies(ontology.getName());
			System.out.println("Tu Control Performer started, will search for  "+tuName);
			ServiceDescription sd = new ServiceDescription();
			sd.setName(tuName);
			sdSearchTemplate.addServices(sd);
				//searching the DF for the Agents that can provide the service
				try {
					DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
					System.out.println("Found the following load agents: ");
					//loadAgent = new AID[result.length];
					for(int j = 0; j < result.length; j++){
						//loadAgent[j] = result[j].getName();
						agentIdentifiers.add(result[j].getName());
						System.out.println(result[j].getName());
					}
				}
				catch (FIPAException fe){
					fe.printStackTrace();
				}
				sdSearchTemplate.clearAllServices();
			if(agentIdentifiers.size()==0) {
				sd.setName(tuName+"_act");
				sdSearchTemplate.addServices(sd);
				//searching the DF for the Agents that can provide the service
				try {
					DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
					System.out.println("Found the following load agents: ");
					//loadAgent = new AID[result.length];
					for(int j = 0; j < result.length; j++){
						//loadAgent[j] = result[j].getName();
						agentIdentifiers.add(result[j].getName());
						System.out.println(result[j].getName());
					}
				}
				catch (FIPAException fe){
					fe.printStackTrace();
				}
			}
				
			if(agentIdentifiers.size()>0) {
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out new Setpoint to TU: " + tuName +". NewSetpoint:"+newSetpoint+" *******");
				//*********** Sending Request to the corresponding agent ***********
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setOntology(ontology.getName());
				msg.setLanguage(codec.getName());
				msg.setConversationId(conversationID);	
				AID receiver = new AID();
				for(int j = 0; j < agentIdentifiers.size(); j++){
					receiver = agentIdentifiers.get(j);
				}
				msg.addReceiver(receiver);	
				try {
					ContentManager cm = myAgent.getContentManager();
					ContentElementList cel = new ContentElementList();
					if(newSetpointVersion) {
						TUControlSequenceRequestSetpoint newLoadControlRequestNewSetpoint = new TUControlSequenceRequestSetpoint();
						newLoadControlRequestNewSetpoint.setNewSetpoint(newSetpoint);
						newLoadControlRequestNewSetpoint.setTuName(tuName);
						Action act = new Action();
						act.setAction(newLoadControlRequestNewSetpoint); 			//Adding the Action the Agent has to perform
						act.setActor(new AID("*", AID.ISGUID));			//Adding an dummy Agent, because the actor field is mandatory (source: http://jade.tilab.com/pipermail/jade-develop/2010q4/016200.html)
						cel.add(act);
						cm.fillContent(msg, cel);
						myAgent.send(msg);
						msg.reset();
					}else {
						TUControlSequenceRequestLoadProfile newLoadControlRequestLoadProfile = new TUControlSequenceRequestLoadProfile();
						newLoadControlRequestLoadProfile.setNewLoadProfile(newLoadProfile);
						newLoadControlRequestLoadProfile.setTuName(tuName);
						Action act = new Action();
						act.setAction(newLoadControlRequestLoadProfile); 			//Adding the Action the Agent has to perform
						act.setActor(new AID("*", AID.ISGUID));			//Adding an dummy Agent, because the actor field is mandatory (source: http://jade.tilab.com/pipermail/jade-develop/2010q4/016200.html)
						cel.add(act);
						cm.fillContent(msg, cel);
						myAgent.send(msg);
						msg.reset();	
					}
	
				} catch (CodecException | OntologyException e){
					e.printStackTrace();
				}
			}else { 
				System.out.println("No Agent can be found under that name");
			}
		}
	}

	//********************** 3. TU Balancing Sequence **********************
	private class BalancingSequencePerformer extends Behaviour{
		private static final long serialVersionUID = 1L;
		
		private int step = 0;
		private int updateRate = 1;
		private String balancingStart;
		private String balancingEnd;
		private String tuName = "noNameSet";
		private String conversationID = "noID";
		private String referenceID = "noID";
		private Date balancingEndDate;
		private ArrayList <AID> agentIdentifiers = new ArrayList<AID>();
		
		
		//constructor that prepares the schedulingSequencePerformer
		public BalancingSequencePerformer(String _balancingStart, String _balancingEnd,
						String _tuName, int _updateRate, String _referenceID) {	
				balancingStart = _balancingStart;
				balancingEnd = _balancingEnd;
				tuName = _tuName;
				updateRate = _updateRate;
				referenceID = _referenceID;
				}
		
		public void onStart(){
			idCounter++;
			conversationID = ("Balancing-"+idCounter);
			
			try {
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
				formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
				balancingEndDate = formatter.parse(balancingEnd);
			} catch (ParseException e) {
	
				e.printStackTrace();
			}
		}
		
		public void action() {
			Date dateNow = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
			formatter.format(dateNow); 
			if (dateNow.after(balancingEndDate)){
				step = 99;
			}
			switch(step){
			//********** searching the DF for the Agent **********
			case 0: 
				System.out.println(this.getAgent().getAID().getLocalName()+"******* BalancingSequencePerformer started ********");
				//pairing the referenceID from the VPP to the conversationID given by the AgentSystem
				idPairsBalancing.put(referenceID, conversationID);
				//searching for the complete AID of the referenced agent
				DFAgentDescription sdSearchTemplate = new DFAgentDescription() ;		//contains the service description list that the schedulingSequence uses
				sdSearchTemplate.addLanguages(codec.getName());
				sdSearchTemplate.addOntologies(ontology.getName());
				ServiceDescription sd = new ServiceDescription();
				sd.setName(tuName);
				sdSearchTemplate.addServices(sd);
					//searching the DF for the Agents that can provide the service
					try {
						DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
						System.out.println("Found the following load agents: ");
						//loadAgent = new AID[result.length];
						for(int j = 0; j < result.length; j++){
							//loadAgent[j] = result[j].getName();
							agentIdentifiers.add(result[j].getName());
							System.out.println(result[j].getName());
						}
					}
					catch (FIPAException fe){
						fe.printStackTrace();
					}
					sdSearchTemplate.clearAllServices();
					if(agentIdentifiers.size()>0) {
						step = 1;
					}else {
						System.out.println("No Agent can be found under that name");
						step = 99;
					}
					break;
			//*********** Sending SUBSCRIBE Request to the corresponding agent ***********
			case 1: 
					System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out new Subscribe to TU: " + tuName +" *******");
					ACLMessage msg = new ACLMessage(ACLMessage.SUBSCRIBE);
					msg.setOntology(ontology.getName());
					msg.setLanguage(codec.getName());
					msg.setConversationId(conversationID);	
					SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
					formatter2.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
					Date date;
					try {
						date = formatter2.parse(balancingStart);
						msg.setReplyByDate(date);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					
					AID receiver = new AID();
					for(int j = 0; j < agentIdentifiers.size(); j++){
						receiver = agentIdentifiers.get(j);
					}
					msg.addReceiver(receiver);	
					try {
						ContentManager cm = myAgent.getContentManager();
						ContentElementList cel = new ContentElementList();
						BalancingSequenceSubscribe newBalancingSequenceSubscribe = new BalancingSequenceSubscribe();
						newBalancingSequenceSubscribe.setTuName(tuName);
						newBalancingSequenceSubscribe.setBalancingStart(balancingStart);
						newBalancingSequenceSubscribe.setBalancingEnd(balancingEnd);
						newBalancingSequenceSubscribe.setUpdateRate(updateRate);
						Action act = new Action();
						act.setAction(newBalancingSequenceSubscribe); 			//Adding the Action the Agent has to perform
						act.setActor(receiver);			//Adding an dummy Agent, because the actor field is mandatory (source: http://jade.tilab.com/pipermail/jade-develop/2010q4/016200.html)
						cel.add(act);
						cm.fillContent(msg, cel);
						myAgent.send(msg);
						msg.reset();
						step = 2;						
					} catch (CodecException | OntologyException e){
						e.printStackTrace();
						step = 99;
					}
				break;
			//************* collecting responses from the TUs **************** 
			case 2:
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
						MessageTemplate.MatchConversationId(conversationID),MessageTemplate.and(
						MessageTemplate.MatchLanguage(codec.getName()),MessageTemplate.or(
						MessageTemplate.MatchPerformative(ACLMessage.REFUSE),
						MessageTemplate.MatchPerformative(ACLMessage.AGREE)))));
				msg = receive(mt);		//returns the first message of the message queue with the corresponding template
				if (msg != null){	
					ContentElement ce = null;	
					if (msg.getPerformative() == ACLMessage.AGREE) {
						System.out.println(this.getAgent().getAID().getLocalName()+"******* AGREE Message Received *******");
						try {
							ce = getContentManager().extractContent(msg);
							Predicate _pc = (Predicate) ce;
							if(_pc instanceof BalancingSequenceAgree){
								//********** Inform the VPP-System about the results**********	
								BalancingSequenceAgree _bsa = (BalancingSequenceAgree)_pc;
								InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_bsa.getTuName(),_bsa.getAgentName());
								ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.BALANCINGAGREE, payload);							
								step = 3;
							}
						} catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					}else if (msg.getPerformative() == ACLMessage.REFUSE) {
						System.out.println(this.getAgent().getAID().getLocalName()+"******* REJECT Message Received *******");
						try {
							ce = getContentManager().extractContent(msg);
							Predicate _pc = (Predicate) ce;
							if(_pc instanceof BalancingSequenceRefuse){
								//********** Inform the VPP-System about the results**********
								BalancingSequenceRefuse _bsr = (BalancingSequenceRefuse)_pc;
								InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_bsr.getTuName(),_bsr.getAgentName());
								ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.BALANCINGREFUSE, payload);		
								step = 99;
							}
						}catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					} else {	//normally can not be the case
						step = 99;
					}
				}else {
					block();
				}
				break;
			//************* collecting values from the TUs **************** 	
			case 3:	
				mt = MessageTemplate.and(
						MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
						MessageTemplate.MatchConversationId(conversationID),MessageTemplate.and(
						MessageTemplate.MatchLanguage(codec.getName()),MessageTemplate.or(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchPerformative(ACLMessage.FAILURE)))));
				msg = receive(mt);		//returns the first message of the message queue with the corresponding template
				if (msg != null){	
					ContentElement ce = null;	
					if (msg.getPerformative() == ACLMessage.INFORM) {
						System.out.println(this.getAgent().getAID().getLocalName()+"******* INFORM Message Received *******");
						try {
							ce = getContentManager().extractContent(msg);
							Predicate _pc = (Predicate) ce;
							if(_pc instanceof BalancingSequenceInform){
								//********** Inform the VPP-System about the results**********
								BalancingSequenceInform _bsi = (BalancingSequenceInform)_pc;
								//InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_bsi.getTuName(),_bsi.getAgentName());
								ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.BALANCINGINFORM, _bsi);	
								Date currentTime = new Date();
								long t= balancingEndDate.getTime();
								Date afterAddingOneMin=new Date(t + (1 * 60000));
								if(currentTime.after(afterAddingOneMin)) {
									step = 99;
								}
							}
						} catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					}else if(msg.getPerformative() == ACLMessage.FAILURE) {
						System.out.println(this.getAgent().getAID().getLocalName()+"******* FAILURE Message Received *******");
						try {
							ce = getContentManager().extractContent(msg);
							Predicate _pc = (Predicate) ce;
							if(_pc instanceof BalancingSequenceFailure){
								//********** Inform the VPP-System about the results**********
								BalancingSequenceFailure _bsf = (BalancingSequenceFailure)_pc;
								InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_bsf.getTuName(),msg.getSender().getLocalName());
								ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.BALANCINGFAILURE, payload);								
								step = 99;
							}
						}catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					}else {	//normally can not be the case
						step = 99;
					}
				}
				break;
			case 99: //final case, here reset() must be called. A reset() in done() would result in a reset() call every cycle, because done() gets called every cycle
				reset(); 				//any Behaviour object that has been executed once, must be	reset by calling its reset() method before it can be executed again.
				step = 100;
				break;
			default:
				step = 99;
			}	
		}
		public boolean done() {
			return step == 100;
		}
	}

	//********************** 4. Accounting Sequence **********************
	private class AccountingSequencePerformer extends Behaviour{
		private static final long serialVersionUID = 1L;
			
		private int step = 0;
		private String tuName = "noNameSet";
		private String agentName = "noNameSet";
		private byte[]  energyConsumptionProfile;
		private ACLMessage msg;
			
		//constructor that prepares the schedulingSequencePerformer
		AccountingSequencePerformer(ACLMessage _msg) {	
				msg = _msg;
				}
			
		public void onStart(){
			try {
				ContentElement ce = getContentManager().extractContent(msg);
				Predicate _pc = (Predicate) ce;
				AccountingSequenceInform _asi = (AccountingSequenceInform)_pc;
				tuName = _asi.getTuName();
				agentName = _asi.getAgentName();
				energyConsumptionProfile = _asi.getEnergyConsumptionProfile();
				} catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
		}
			 
		public void action() {
			switch (step) {
			case 0:	//sending the energy consumption profiles to the VPP
					ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
					putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.ENERGYCONSUMPTIONPROFILE, energyConsumptionProfile);
					ConsumingRest_VPP putInstance2 = new ConsumingRest_VPP();
					InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(agentName,tuName);
					putInstance2.putNodeRed(Addresses.URL_NODERED, PutVariable.ACCOUNTINGECPREFERENCE, payload);
					step = 1;
					break;
			case 1:
					if(VppVariables.accountingTrigger && tuName.equals(VppVariables.accountingTUName)) {
						VppVariables.accountingTrigger = false;
						System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out new INFORM to TU: " + tuName +" *******");
						try {
							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.INFORM);
							ContentManager cm = myAgent.getContentManager();
							ContentElementList cel = new ContentElementList();
							AccountingSequenceInformReceived newAccountingSequenceInformReceived = new AccountingSequenceInformReceived();
							newAccountingSequenceInformReceived.setTuName(tuName);
							cel.add(newAccountingSequenceInformReceived);
							cm.fillContent(reply, cel);
							myAgent.send(reply);
						} catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					step = 99;
				}
			break;
			case 99: //final case, here reset() must be called. A reset() in done() would result in a reset() call every cycle, because done() gets called every cycle
				reset(); 				//any Behaviour object that has been executed once, must be	reset by calling its reset() method before it can be executed again.
				step = 100;
				break;
			default:
				step = 99;
			}
		}

		public boolean done() {
			return step == 100;
		}
	}
					
	//********************** 5.A LoadTimeWindowsSharing Sequence (Request from other Agents) **********************			
	private class LoadTimeWindowsRequestPerformer extends Behaviour{
		private static final long serialVersionUID = 1L;
		
		private int step = 0;
		private String loadTimeWindowsReference;
		private ACLMessage msg;

		LoadTimeWindowsRequestPerformer(ACLMessage _msg){
			msg = _msg;
		}
		
		public void onStart(){
			try {
				ContentElement ce = getContentManager().extractContent(msg);
				Action _ac = (Action) ce;
				if(_ac.getAction() instanceof LoadTimeWindowsShareRequest){
					LoadTimeWindowsShareRequest _ltwsr = (LoadTimeWindowsShareRequest)_ac.getAction();
					loadTimeWindowsReference = _ltwsr.getLoadTimeWindowsReference();
				}
			} catch (CodecException | OntologyException e) {
				e.printStackTrace();
			}
		}
		
		public void action() {
			switch (step) {
			case 0:	//sending the request with reference profiles to the VPP
				ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
				InterfacePayloadLoadTimeWindows payload = new InterfacePayloadLoadTimeWindows(loadTimeWindowsReference);
				putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.LOADTIMEWINDOWSREQUEST, payload);	
				step = 1;
				break;
			case 1:
				if(VppVariables.loadTimeWindowsTrigger && loadTimeWindowsReference.equals(VppVariables.loadTimeWindowsReference)) {
					VppVariables.loadTimeWindowsTrigger = false;
					System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out new INFORM to TU: " + msg.getSender().getLocalName() +" *******");
					try {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						ContentManager cm = myAgent.getContentManager();
						ContentElementList cel = new ContentElementList();
						LoadTimeWindowsShareInform newLoadTimeWindowsShareInform = new LoadTimeWindowsShareInform();
						newLoadTimeWindowsShareInform.setLoadTimeWindowsReference(loadTimeWindowsReference);
						newLoadTimeWindowsShareInform.setWindowHighBegin(VppVariables.windowHighBegin);
						newLoadTimeWindowsShareInform.setWindowHighEnd(VppVariables.windowHighEnd);
						newLoadTimeWindowsShareInform.setWindowLowBegin(VppVariables.windowLowBegin);
						newLoadTimeWindowsShareInform.setWindowLowEnd(VppVariables.windowLowEnd);
						VppVariables.resetloadTimeWindows();
						cel.add(newLoadTimeWindowsShareInform);
						cm.fillContent(reply, cel);
						myAgent.send(reply);
					} catch (CodecException | OntologyException e) {
						e.printStackTrace();
					}
					step = 99;
				}
				break;
			case 99: //final case, here reset() must be called. A reset() in done() would result in a reset() call every cycle, because done() gets called every cycle
				reset(); 				//any Behaviour object that has been executed once, must be	reset by calling its reset() method before it can be executed again.
				step = 100;
				break;
			default:
				step = 99;
			}
		}

		public boolean done() {
			return step == 100;
		}
		
	}
	
	//********************** 5.B LoadTimeWindowsSharing Sequence (Broadcast from the VPP) **********************
	private class LoadTimeWindowsBroadcastPerformer extends OneShotBehaviour{
		private static final long serialVersionUID = 1L;
		private String loadTimeWindowsReference;
		private String windowHighBegin;
		private String windowHighEnd;
		private String windowLowBegin;
		private String windowLowEnd;
		
		LoadTimeWindowsBroadcastPerformer(String _loadTimeWindowsReference, String _windowHighBegin, String _windowHighEnd, String _windowLowBegin, String _windowLowEnd){
			loadTimeWindowsReference = _loadTimeWindowsReference;
			windowHighBegin = _windowHighBegin;
			windowHighEnd = _windowHighEnd;
			windowLowBegin = _windowLowBegin;
			windowLowEnd = _windowLowEnd;
		}
		

		public void action() {

			ArrayList <AID> tuAgents = new ArrayList<AID>();
			System.out.println(this.getAgent().getAID().getLocalName()+"******* Informing TUs about new LoadTimeWindows ********");
			
			
			//searching for the complete AID of the referenced agent
			DFAgentDescription sdSearchTemplate = new DFAgentDescription() ;		//contains the service description list that the schedulingSequence uses
			sdSearchTemplate.addLanguages(codec.getName());
			sdSearchTemplate.addOntologies(ontology.getName());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("ANN");
			sdSearchTemplate.addServices(sd);
			//the same for the "ANN_pla" Services
			DFAgentDescription sdSearchTemplate_pla =new DFAgentDescription() ;
			sdSearchTemplate_pla.addLanguages(codec.getName());
			sdSearchTemplate_pla.addOntologies(ontology.getName());
			ServiceDescription sd_pla = new ServiceDescription();
			sd_pla.setType("ANN_pla");
			sdSearchTemplate_pla.addServices(sd_pla);
			//searching the DF for the Agents that can provide the service
			try {
				DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
				for(int j = 0; j < result.length; j++){
					tuAgents.add(result[j].getName());
				}
				DFAgentDescription[] result_pla = DFService.search(myAgent, sdSearchTemplate_pla);
				for(int j = 0; j < result_pla.length; j++){
					tuAgents.add(result_pla[j].getName());
				}
			}
			catch (FIPAException fe){
				fe.printStackTrace();
			}
			sdSearchTemplate.clearAllServices();
			sdSearchTemplate_pla.clearAllServices();
			
			if(tuAgents.size()>0) {
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out new INFORM to TUs *******");
				try {
		
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setOntology(ontology.getName());
					msg.setLanguage(codec.getName());
					msg.setConversationId("loadTimeWindows");
					//add receiver with ANN service description
					for(int i = 0; i < tuAgents.size(); i++){
						msg.addReceiver(tuAgents.get(i));
					}
					ContentManager cm = myAgent.getContentManager();
					ContentElementList cel = new ContentElementList();
					LoadTimeWindowsShareInform newLoadTimeWindowsShareInform = new LoadTimeWindowsShareInform();
					newLoadTimeWindowsShareInform.setLoadTimeWindowsReference(loadTimeWindowsReference);
					newLoadTimeWindowsShareInform.setWindowHighBegin(windowHighBegin);
					newLoadTimeWindowsShareInform.setWindowHighEnd(windowHighEnd);
					newLoadTimeWindowsShareInform.setWindowLowBegin(windowLowBegin);
					newLoadTimeWindowsShareInform.setWindowLowEnd(windowLowEnd);
					cel.add(newLoadTimeWindowsShareInform);
					cm.fillContent(msg, cel);
					myAgent.send(msg);
				} catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
			}
		}

	
	}
	
	//********************** 6.A FrequencyRelayEnable Sequence **********************
	private class FREnablePerformer extends Behaviour{
		private static final long serialVersionUID = 1L;
			
		private int step = 0;
		private String tuName = "noNameSet";
		private String conversationID = "noID";
		private ArrayList <AID> agentIdentifiers = new ArrayList<AID>();
				
		//constructor that prepares the schedulingSequencePerformer
		FREnablePerformer(String _tuName) {	
				tuName = _tuName;
				}

		public void onStart(){
			idCounter++;
			conversationID = ("FREnable-"+idCounter);
		}
		
		
		public void action() {
			switch (step) {
			case 0:	//finding the correct agent and sending the enable request
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Frequency Relay Enable started ********");
				//searching for the complete AID of the referenced agent
				DFAgentDescription sdSearchTemplate = new DFAgentDescription() ;		//contains the service description list that the schedulingSequence uses
				sdSearchTemplate.addLanguages(codec.getName());
				sdSearchTemplate.addOntologies(ontology.getName());
				ServiceDescription sd = new ServiceDescription();
				sd.setName(tuName);
				sdSearchTemplate.addServices(sd);
					//searching the DF for the Agents that can provide the service
					try {
						DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
						System.out.println("Found the following load agents: ");
						//loadAgent = new AID[result.length];
						for(int j = 0; j < result.length; j++){
							agentIdentifiers.add(result[j].getName());
							System.out.println(result[j].getName());
						}
					}
					catch (FIPAException fe){
						fe.printStackTrace();
					}
					sdSearchTemplate.clearAllServices();
					if(agentIdentifiers.size()>0) {
						step = 1;
					}else {
						System.out.println("No Agent can be found under that name");
						step = 99;
					}
					break;
			case 1://waiting for confirmation
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out FR enable to TU: " + tuName +" *******");
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setOntology(ontology.getName());
				msg.setLanguage(codec.getName());
				msg.setConversationId(conversationID);	
				AID receiver = new AID();
				for(int j = 0; j < agentIdentifiers.size(); j++){
					receiver = agentIdentifiers.get(j);
				}
				msg.addReceiver(receiver);	
				try {
					ContentManager cm = myAgent.getContentManager();
					ContentElementList cel = new ContentElementList();
					FreqRelayEnableRequest newFreqRelayEnableRequest = new FreqRelayEnableRequest();
					newFreqRelayEnableRequest.setTuName(tuName);
					Action act = new Action();
					act.setAction(newFreqRelayEnableRequest); 			//Adding the Action the Agent has to perform
					act.setActor(new AID("*", AID.ISGUID));				//Adding an dummy Agent, because the actor field is mandatory (source: http://jade.tilab.com/pipermail/jade-develop/2010q4/016200.html)
					cel.add(act);
					cm.fillContent(msg, cel);
					myAgent.send(msg);
					msg.reset();
					step = 2;
					
				} catch (CodecException | OntologyException e){
					e.printStackTrace();
					step = 99;
				}
			break;
			case 2:
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
						MessageTemplate.MatchConversationId(conversationID),MessageTemplate.and(
						MessageTemplate.MatchLanguage(codec.getName()),MessageTemplate.or(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchPerformative(ACLMessage.FAILURE)))));
				msg = receive(mt);		//returns the first message of the message queue with the corresponding template
				if (msg != null){
					try {
						ContentElement ce = null;	
						if (msg.getPerformative() == ACLMessage.INFORM) {
							System.out.println(this.getAgent().getAID().getLocalName()+"******* INFORM Message Received *******");
							ce = getContentManager().extractContent(msg);
							Predicate _pc = (Predicate) ce;
							if(_pc instanceof FreqRelayEnableInform){
								//********** Inform the VPP-System about the results**********	
								FreqRelayEnableInform _fre = (FreqRelayEnableInform)_pc;
								InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_fre.getTuName(),_fre.getAgentName());
								ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.FRENABLEINFORM, payload);							
								step = 99;
							}
						}else if (msg.getPerformative() == ACLMessage.FAILURE) {
							System.out.println(this.getAgent().getAID().getLocalName()+"******* FAILURE Message Received *******");
							ce = getContentManager().extractContent(msg);
							Predicate _pc = (Predicate) ce;
							if(_pc instanceof FreqRelayEnableFailure){
								//********** Inform the VPP-System about the results**********
								FreqRelayEnableFailure _fre = (FreqRelayEnableFailure)_pc;
								InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_fre.getTuName(),_fre.getAgentName());
								ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.FRENABLEFAILURE, payload);		
								step = 99;
							}	
						} else {	//normally can not be the case
							step = 99;
						}
					} catch (CodecException | OntologyException e) {
						e.printStackTrace();
						step = 99;
					}
				}else {
					block();
				}
			break;
			case 99: 					//final case, here reset() must be called. A reset() in done() would result in a reset() call every cycle, because done() gets called every cycle
				reset(); 				//any Behaviour object that has been executed once, must be	reset by calling its reset() method before it can be executed again.
				step = 100;
				break;
			default:
				step = 99;
			}
		}
		public boolean done() {
			return step == 100;
		}
	}

	//********************** 6.B FrequencyRelayBlock Sequence **********************
	private class FRBlockPerformer extends Behaviour{
		private static final long serialVersionUID = 1L;
			
		private int step = 0;
		private String tuName = "noNameSet";
		private String conversationID = "noID";
		private ArrayList <AID> agentIdentifiers = new ArrayList<AID>();
				
		//constructor that prepares the schedulingSequencePerformer
		FRBlockPerformer(String _tuName) {	
				tuName = _tuName;
				}

		public void onStart(){
			idCounter++;
			conversationID = ("FRBlock-"+idCounter);
		}
		
		
		public void action() {
			switch (step) {
			case 0:	//finding the correct agent and sending the enable request
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Frequency Relay Block started ********");
				//searching for the complete AID of the referenced agent
				DFAgentDescription sdSearchTemplate = new DFAgentDescription() ;		//contains the service description list that the schedulingSequence uses
				sdSearchTemplate.addLanguages(codec.getName());
				sdSearchTemplate.addOntologies(ontology.getName());
				ServiceDescription sd = new ServiceDescription();
				sd.setName(tuName);
				sdSearchTemplate.addServices(sd);
					//searching the DF for the Agents that can provide the service
					try {
						DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
						System.out.println("Found the following load agents: ");
						//loadAgent = new AID[result.length];
						for(int j = 0; j < result.length; j++){
							agentIdentifiers.add(result[j].getName());
						}
					}
					catch (FIPAException fe){
						fe.printStackTrace();
					}
					sdSearchTemplate.clearAllServices();
					if(agentIdentifiers.size()>0) {
						step = 1;
					}else {
						System.out.println("No Agent can be found under that name");
						step = 99;
					}
					break;
			case 1://waiting for confirmation
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out FR block to TU: " + tuName +" *******");
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setOntology(ontology.getName());
				msg.setLanguage(codec.getName());
				msg.setConversationId(conversationID);	
				AID receiver = new AID();
				for(int j = 0; j < agentIdentifiers.size(); j++){
					receiver = agentIdentifiers.get(j);
				}
				msg.addReceiver(receiver);	
				try {
					ContentManager cm = myAgent.getContentManager();
					ContentElementList cel = new ContentElementList();
					FreqRelayBlockRequest newFreqRelayBlockRequest = new FreqRelayBlockRequest();
					newFreqRelayBlockRequest.setTuName(tuName);
					Action act = new Action();
					act.setAction(newFreqRelayBlockRequest); 			//Adding the Action the Agent has to perform
					act.setActor(new AID("*", AID.ISGUID));				//Adding an dummy Agent, because the actor field is mandatory (source: http://jade.tilab.com/pipermail/jade-develop/2010q4/016200.html)
					cel.add(act);
					cm.fillContent(msg, cel);
					myAgent.send(msg);
					msg.reset();
					step = 2;
					
				} catch (CodecException | OntologyException e){
					e.printStackTrace();
					step = 99;
				}
			break;
			case 2:
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
						MessageTemplate.MatchConversationId(conversationID),MessageTemplate.and(
						MessageTemplate.MatchLanguage(codec.getName()),MessageTemplate.or(
						MessageTemplate.MatchPerformative(ACLMessage.INFORM),
						MessageTemplate.MatchPerformative(ACLMessage.FAILURE)))));
				msg = receive(mt);		//returns the first message of the message queue with the corresponding template
				if (msg != null){
					try {
						ContentElement ce = null;	
						if (msg.getPerformative() == ACLMessage.INFORM) {
							System.out.println(this.getAgent().getAID().getLocalName()+"******* INFORM Message Received *******");
							ce = getContentManager().extractContent(msg);
							Predicate _pc = (Predicate) ce;
							if(_pc instanceof FreqRelayBlockInform){
								//********** Inform the VPP-System about the results**********	
								FreqRelayBlockInform _frb = (FreqRelayBlockInform)_pc;
								InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_frb.getTuName(),_frb.getAgentName());
								ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.FRBLOCKINFORM, payload);							
								step = 99;
							}
						}else if (msg.getPerformative() == ACLMessage.FAILURE) {
							System.out.println(this.getAgent().getAID().getLocalName()+"******* FAILURE Message Received *******");
							ce = getContentManager().extractContent(msg);
							Predicate _pc = (Predicate) ce;
							if(_pc instanceof FreqRelayBlockFailure){
								//********** Inform the VPP-System about the results**********
								FreqRelayBlockFailure _frb = (FreqRelayBlockFailure)_pc;
								InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_frb.getTuName(),_frb.getAgentName());
								ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.FRBLOCKFAILURE, payload);		
								step = 99;
							}	
						} else {	//normally can not be the case
							step = 99;
						}
					} catch (CodecException | OntologyException e) {
						e.printStackTrace();
						step = 99;
					}
				}else {
					block();
				}
				break;
			case 99: 					//final case, here reset() must be called. A reset() in done() would result in a reset() call every cycle, because done() gets called every cycle
				reset(); 				//any Behaviour object that has been executed once, must be	reset by calling its reset() method before it can be executed again.
				step = 100;
				break;
			default:
				step = 99;
			}
		}
		public boolean done() {
			return step == 100;
		}
	}

	//********************** 6.C FrequencyRelayStatus Sequence **********************
	private class FRStatusPerformer extends Behaviour{
		private static final long serialVersionUID = 1L;
			
		private int step = 0;
		private ACLMessage msg;
		private String tuName = "NoNameSet";  
		private String agentName = "noNameSet";
		
		
		//constructor that prepares the schedulingSequencePerformer
		FRStatusPerformer(ACLMessage _msg, String _tuName, String _agentName) {	
			msg = _msg;	
			tuName = _tuName;
			agentName = _agentName;
		}

		public void onStart(){
			System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending Frequency Relay Status to VPP *******");
		
		}
		
		
		public void action() {
			switch(step){
			case 0: 
				//******* sending the request to the TU system *******
				ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
				InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(agentName, tuName);
				putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.FRTRIGGERED, payload);	
				step = 1;
				break;
			case 1:
				try {
					if(VppVariables.freqRelayConfirmTrigger && tuName.equals(VppVariables.freqRelayConfirmTUName)) {
						VppVariables.freqRelayConfirmTrigger = false;
						VppVariables.resetfreqRelayConfirm();
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						ContentManager cm = myAgent.getContentManager();
						ContentElementList cel = new ContentElementList();
						FreqRelayStatusConfirm newFreqRelayStatusConfirm = new FreqRelayStatusConfirm();
						newFreqRelayStatusConfirm.setTuName(tuName);
						newFreqRelayStatusConfirm.setAgentName(agentName);
						cel.add(newFreqRelayStatusConfirm);
						cm.fillContent(reply, cel);
						myAgent.send(reply);
						step = 99;
					}
				}
				catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
				break;
			case 99: 
				reset();
				step = 100;
				break;
			default:
				step = 99;
			}
		}
	
		public boolean done() {
			return step == 100;
		}
	}

	//********************** 7.A Receive CancelOperation Sequence **********************
	private class ReceiveCancelOperationPerformer extends Behaviour{
		private static final long serialVersionUID = 1L;
			
		private int step = 0;
		private String tuName = "noNameSet";
		private String agentName = "noNameSet";
		private String operationReference = "noReferenceSet";
		private ACLMessage msg;
			
		//constructor that prepares the schedulingSequencePerformer
		ReceiveCancelOperationPerformer(ACLMessage _msg) {	
				msg = _msg;
				}
			
		public void onStart(){
			try {
				ContentElement ce = getContentManager().extractContent(msg);
				Predicate _pc = (Predicate) ce;
				CancelOperation _coi = (CancelOperation)_pc;
				tuName = _coi.getTuName();
				agentName = _coi.getAgentName();
				operationReference = _coi.getOperationReference();
				} catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
		}
			 
		public void action() {
			switch (step) {
			case 0:	//sending the energy consumption profiles to the VPP
					ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
					InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(operationReference, agentName,tuName);
					putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.CANCELOPERATION, payload);
					step = 1;
					break;
			case 1:
					if(VppVariables.cancelOperationTrigger && operationReference.equals(VppVariables.cancelOperationReference)) {
						VppVariables.cancelOperationTrigger = false;
						System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out new INFORM to TU: " + tuName +" *******");
						try {
							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.INFORM);
							ContentManager cm = myAgent.getContentManager();
							ContentElementList cel = new ContentElementList();
							CancelOperationConfirm newCancelOperationInformReceived = new CancelOperationConfirm();
							newCancelOperationInformReceived.setTuName(tuName);
							newCancelOperationInformReceived.setOperationReference(operationReference);
							cel.add(newCancelOperationInformReceived);
							cm.fillContent(reply, cel);
							myAgent.send(reply);
						} catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					step = 99;
				}else 	if(VppVariables.cancelOperationTriggerFail && operationReference.equals(VppVariables.cancelOperationReference)) {
					VppVariables.cancelOperationTriggerFail = false;
					System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out new INFORM to TU: " + tuName +" *******");
					try {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.FAILURE);
						ContentManager cm = myAgent.getContentManager();
						ContentElementList cel = new ContentElementList();
						CancelOperationFailure newCancelOperationInformReceived = new CancelOperationFailure();
						newCancelOperationInformReceived.setTuName(tuName);
						newCancelOperationInformReceived.setOperationReference(operationReference);
						cel.add(newCancelOperationInformReceived);
						cm.fillContent(reply, cel);
						myAgent.send(reply);
					} catch (CodecException | OntologyException e) {
						e.printStackTrace();
					}
				step = 99;
				}
			break;
			case 99: //final case, here reset() must be called. A reset() in done() would result in a reset() call every cycle, because done() gets called every cycle
				reset(); 				//any Behaviour object that has been executed once, must be	reset by calling its reset() method before it can be executed again.
				step = 100;
				break;
			default:
				step = 99;
			}
		}

		public boolean done() {
			return step == 100;
		}
	}
	
	//********************** 7.B Send CancelOperation Sequence **********************
	private class SendCancelOperationPerformer extends Behaviour {
		private static final long serialVersionUID = 1L;
		
		private int step = 0;
		private String tuName = "noNameSet";
		private String agentName = "noNameSet";
		//private String date;
		private String operationReference = "noIDSet"; 
		private String conversationID;
		private ArrayList <AID> tuAgents = new ArrayList<AID>();

		SendCancelOperationPerformer(String _tuName, String _operationReference){
			tuName = _tuName;
			operationReference = _operationReference;
		}
		
		
		public void onStart(){
			System.out.println(this.getAgent().getAID().getLocalName()+"******* OperationCancelBehaviour started ********");
			agentName = getAID().getLocalName();
//			Date dateNow = new Date();
//			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
//			formatter.format(dateNow); 
			//date = dateNow.toString();
			conversationID = "cancelOperation";
		}

		public  void action(){
			switch(step){
			case 0:
				DFAgentDescription sdSearchTemplate = new DFAgentDescription() ;		//contains the service description list that the schedulingSequence uses
				sdSearchTemplate.addLanguages(codec.getName());
				sdSearchTemplate.addOntologies(ontology.getName());
				ServiceDescription sd = new ServiceDescription();
				sd.setName(tuName);
				sdSearchTemplate.addServices(sd);
					//searching the DF for the Agents that can provide the service
					try {
						DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
						for(int j = 0; j < result.length; j++){
							tuAgents.add(result[j].getName());
						}
					}
					catch (FIPAException fe){
						fe.printStackTrace();
					}
					sdSearchTemplate.clearAllServices();
					if(tuAgents.size()>0) {
						step = 1;
					}else {
						System.out.println("No Agent can be found under that name");
						step = 99;
					}			
				break;
			case 1:
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out CANCEL (cancel operation) to VPP *******");
				
				ACLMessage msg = new ACLMessage(ACLMessage.CANCEL);
				msg.setOntology(ontology.getName());
				msg.setLanguage(codec.getName());
				msg.setConversationId(conversationID);	
				AID receiver = new AID();
				for(int j = 0; j < tuAgents.size(); j++){
					receiver = tuAgents.get(j);
				}
				msg.addReceiver(receiver);	
				try {
					ContentManager cm = myAgent.getContentManager();
					ContentElementList cel = new ContentElementList();
					CancelOperation newCancelOperationInform = new CancelOperation();
					newCancelOperationInform.setAgentName(agentName);
					newCancelOperationInform.setTuName(tuName);
					newCancelOperationInform.setOperationReference(operationReference);
					cel.add(newCancelOperationInform);
					cm.fillContent(msg, cel);
					myAgent.send(msg);
					msg.reset();
					step = 2;
				} catch (CodecException | OntologyException e){
					e.printStackTrace();
					step = 99;
				}
			case 2:	
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
						MessageTemplate.MatchConversationId(conversationID),
						MessageTemplate.MatchLanguage(codec.getName())));
				msg = receive(mt);		//returns the first message of the message queue with the corresponding template
				if (msg != null){	
					ContentElement ce = null;	
					if (msg.getPerformative() == ACLMessage.INFORM) {
						System.out.println(this.getAgent().getAID().getLocalName()+"******* INFORM Message Received *******");
						try {
							ce = getContentManager().extractContent(msg);
							Predicate _pc = (Predicate) ce;
							if(_pc instanceof CancelOperationConfirm){
								//********** Inform the EMS-System about the results**********				
								CancelOperationConfirm _asir = (CancelOperationConfirm)_pc;
								InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_asir.getOperationReference(), getAID().getLocalName(),_asir.getTuName() );
								ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.CANCELOPERATIONCONFIRM, payload);							
								step = 99;
							}
						} catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					}else if(msg.getPerformative() == ACLMessage.FAILURE) {
						System.out.println(this.getAgent().getAID().getLocalName()+"******* FAILURE Message Received *******");
						try {
							ce = getContentManager().extractContent(msg);
							Predicate _pc = (Predicate) ce;
							if(_pc instanceof CancelOperationFailure){
								//********** Inform the VPP-System about the results**********				
								CancelOperationFailure _asir = (CancelOperationFailure)_pc;
								InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_asir.getOperationReference(), getAID().getLocalName(),_asir.getTuName() );
								ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.CANCELOPERATIONFAILED, payload);							
								step = 99;
							}
						} catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					}else {
						step = 99;
					}
				}else {
					block();
				}
				break;
			case 99: 
				reset();
				step = 100;
				break;
			default:
				step = 99;
			}
			
		}

		public boolean done(){
			return step == 100;
		}	
		
	}

	//********************** 8.A RequestInfos Sequence **********************
	private class RequestInfoPerformer extends Behaviour {
	private static final long serialVersionUID = 1L;
		
		private int step = 0;
		private String tuName = "noNameSet";
//		private String agentName = "noNameSet";
		private String conversationID;
		private byte[] infoSet;
		private ArrayList <AID> tuAgents = new ArrayList<AID>();
		private Date expireDate;

		RequestInfoPerformer(String _tuName){
			tuName = _tuName;
		}
		
		
		public void onStart(){
			System.out.println(this.getAgent().getAID().getLocalName()+"******* RequestInfosBehaviour started ********");
//			agentName = getAID().getLocalName();
			Date dateStart = new Date();
			long t;
			t = dateStart.getTime();
			expireDate=new Date(t + (5 * 60000));   //5min added
			conversationID = "requestInfoOperation";
		}

		public  void action(){
			switch(step){
			case 0:
				DFAgentDescription sdSearchTemplate = new DFAgentDescription() ;		//contains the service description list that the schedulingSequence uses
				sdSearchTemplate.addLanguages(codec.getName());
				sdSearchTemplate.addOntologies(ontology.getName());
				ServiceDescription sd = new ServiceDescription();
				sd.setName(tuName);
				sdSearchTemplate.addServices(sd);
					//searching the DF for the Agents that can provide the service
					try {
						DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
						for(int j = 0; j < result.length; j++){
							tuAgents.add(result[j].getName());
						}
					}
					catch (FIPAException fe){
						fe.printStackTrace();
					}
					sdSearchTemplate.clearAllServices();
					if(tuAgents.size()>0) {
						step = 1;
					}else {
						System.out.println("No Agent can be found under that name");
						step = 99;
					}			
				break;
			case 1:
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out REQUEST (Request Infos) to TU *******");
				
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setOntology(ontology.getName());
				msg.setLanguage(codec.getName());
				msg.setConversationId(conversationID);	
				AID receiver = new AID();
				for(int j = 0; j < tuAgents.size(); j++){
					receiver = tuAgents.get(j);
				}
				msg.addReceiver(receiver);	
				try {
					ContentManager cm = myAgent.getContentManager();
					ContentElementList cel = new ContentElementList();
					RequestInfoRequest newRequestInfoRequest = new RequestInfoRequest();
					newRequestInfoRequest.setTuName(tuName);
					newRequestInfoRequest.setTuName(getAID().getLocalName());
					Action act = new Action();
					act.setAction(newRequestInfoRequest); 			//Adding the Action the Agent has to perform
					act.setActor(new AID("*", AID.ISGUID));				//Adding an dummy Agent, because the actor field is mandatory (source: http://jade.tilab.com/pipermail/jade-develop/2010q4/016200.html)
					cel.add(act);
					cm.fillContent(msg, cel);
					myAgent.send(msg);
					msg.reset();
					step = 2;
				} catch (CodecException | OntologyException e){
					e.printStackTrace();
					step = 99;
				}
			case 2:	
				MessageTemplate mt = MessageTemplate.and(
						MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
						MessageTemplate.MatchConversationId(conversationID),
						MessageTemplate.MatchLanguage(codec.getName())));
				msg = receive(mt);		//returns the first message of the message queue with the corresponding template
				if (msg != null){	
					ContentElement ce = null;	
					if (msg.getPerformative() == ACLMessage.INFORM) {
						System.out.println(this.getAgent().getAID().getLocalName()+"******* INFORM Message Received *******");
						try {
							ce = getContentManager().extractContent(msg);
							Predicate _pc = (Predicate) ce;
							if(_pc instanceof RequestInfoInform){
								//********** Inform the VPP-System about the results**********				
								RequestInfoInform _rii = (RequestInfoInform)_pc;
								ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
								infoSet = _rii.getInfoSet();
								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.REQUESTINFOINFORM, infoSet);							
								step = 99;
							}
						} catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					}else if(msg.getPerformative() == ACLMessage.FAILURE) {
						step = 99;
					}else{
						step = 99;
					}
				}else {
					Date dateNow = new Date();
					if(dateNow.after(expireDate)) {
							step = 99;
					}
				}
				break;
			case 99: 
				reset();
				step = 100;
				break;
			default:
				step = 99;
			}
			
		
		
		}
		public boolean done() {
			return step == 100;
		}
		
		
		}

	//********************** 8.B RequestInfosResponse Sequence **********************
	private class RequestInfoResponsePerformer extends OneShotBehaviour{
		private static final long serialVersionUID = 1L;
		
		private String tuName = "noNameSet";
//		private String agentName = "noNameSet";
		private byte[] infoSet;
		private ACLMessage msg;
			
		//constructor that prepares the schedulingSequencePerformer
		RequestInfoResponsePerformer(ACLMessage _msg) {	
				msg = _msg;
				}
		
		public void onStart(){
			try {
				ContentElement ce = getContentManager().extractContent(msg);
				Action _ac = (Action) ce;
				if(_ac.getAction() instanceof RequestInfoRequest){
					RequestInfoRequest _rir = (RequestInfoRequest)_ac.getAction();
					tuName = _rir.getTuName();
				}
			} catch (CodecException | OntologyException e) {
				e.printStackTrace();
			}
		}
			 
		public void action() {
			ConsumingRest_VPP getInstance = new ConsumingRest_VPP();
			infoSet = getInstance.getNodeRed(Addresses.URL_NODERED, PutVariable.REQUESTINFO);
			System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out new RequestInfo *******");
			try {
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				ContentManager cm = myAgent.getContentManager();
				ContentElementList cel = new ContentElementList();
				RequestInfoInform newRequestInfoInform = new RequestInfoInform();
				newRequestInfoInform.setTuName(tuName);
				newRequestInfoInform.setAgentName(getAID().getLocalName());
				newRequestInfoInform.setInfoSet(infoSet);
				cel.add(newRequestInfoInform);
				cm.fillContent(reply, cel);
				myAgent.send(reply);
			} catch (CodecException | OntologyException e) {
				e.printStackTrace();
			}
		}

	}
					

}


