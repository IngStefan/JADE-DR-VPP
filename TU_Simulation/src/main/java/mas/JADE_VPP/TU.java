package mas.JADE_VPP;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

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

import java.lang.Object;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import mas.JADE_VPP.ontology.*;

public class TU extends Agent {
	private static final long serialVersionUID = 1L;
	
	private Codec codec = new SLCodec();
	//private Codec codec2 = new LEAPCodec();			//required for data transfer via bytesequences (i.e. Scheduling Plans), can also be done with SL Codec
	private Ontology ontology = VPP_DR_Ontology.getInstance();
	private ThreadedBehaviourFactory tbf = new ThreadedBehaviourFactory(); //tbf is needed for extra thread generation, which is needed to prevent a cpu usage of 100% 

	//*********** Agent startup ***********
	protected void setup(){
		ArrayList <String> sdList = new ArrayList<String>(); 
		//Setting Language and Ontology
		//getContentManager().registerLanguage(codec2);
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(ontology);
		//Getting Arguments from the Startup.class
		Object[] args = getArguments();
		//the local name is user-set and given at the startup
		String localName = getAID().getLocalName();
		String arguments = String.valueOf(args[0]);		//get the arguments given from the Startup.java
		arguments = arguments.replaceAll("\\s+","");	//remove all whitespaces
		System.out.println("********* TU-Agent online: " + getAID().getName()+ " with Service Descriptions: " +String.valueOf(args[0])+ " *********");
		
		if (args != null && args.length > 0) 
		{
			List <String> serviceDescriptionList = new ArrayList<String>();
			serviceDescriptionList = Arrays.asList(arguments.split(","));	
			for (int i = 0; i < serviceDescriptionList.size(); i++){
				sdList.add(serviceDescriptionList.get(i));
			}
			//register / publishing own service
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			dfd.addLanguages(codec.getName());
			//dfd.addLanguages(codec2.getName());
			dfd.addOntologies(ontology.getName());
			
			for(int i = 0; i <sdList.size(); i++) {
				ServiceDescription sd = new ServiceDescription();
				sd.setName(localName);
				sd.setType(sdList.get(i));
				dfd.addServices(sd);
			}
			try{
				DFService.register(this, dfd);
			}
			catch(FIPAException fe){
				fe.printStackTrace();
			}
			


			
			//start the main cyclic behavior
			addBehaviour(new TUManager());
			addBehaviour(tbf.wrap(new TUInputListener()));
			

			
		}
		else {
				System.out.println(getAID().getName()+" Agent shutting down. Wrong Service Description");
				doDelete();
		}
	}
	
	//*********** Agent shutdown ***********
	protected void takdeDown(){
		System.out.println("TU-Agent: " +getAID().getLocalName() +" terminating.");
		tbf.interrupt();
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
	}
	
	//********************** 0. TU Manager (Main behaviour) **********************
	private class TUManager extends CyclicBehaviour{
		private static final long serialVersionUID = 1L;

		public void action(){
			
			//******************* Collecting Messages from other Agents ************
			//******** Handling unknown messages: ****************
			MessageTemplate mt = MessageTemplate.not(MessageTemplate.or(MessageTemplate.MatchOntology(ontology.getName()), //filter for messages that dont use the VPP_DR_Ontology
					MessageTemplate.MatchOntology("FIPA-Agent-Management"))); 			
			ACLMessage msg = receive(mt); 	//returns the first message of the message queue with the corresponding template
			if (msg != null){				//if a proper message can be found
				//********** every Message that does not use the VPP_DR_Ontology can not be understood ********
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Message Received that cannot be understood *******");
				ACLMessage reply = msg.createReply();	
				reply.setPerformative(ACLMessage.NOT_UNDERSTOOD);
				myAgent.send(reply);
			}
			
			//checking the message if it has the same language and ontology
			mt = MessageTemplate.and(
					MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
					MessageTemplate.MatchLanguage(codec.getName()),MessageTemplate.or(
					MessageTemplate.MatchPerformative(ACLMessage.CFP),MessageTemplate.or(
					MessageTemplate.MatchPerformative(ACLMessage.REQUEST),MessageTemplate.or(
					MessageTemplate.MatchPerformative(ACLMessage.CANCEL),MessageTemplate.or(
					MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),MessageTemplate.or(
					MessageTemplate.MatchConversationId("loadTimeWindows"),
					MessageTemplate.MatchConversationId("cancelOperation")))))))));
			msg = receive(mt);		//returns the first message of the message queue with the corresponding template
			if (msg != null){	
				try {
					int step = msg.getPerformative();
					ContentElement ce;
					Action _ac;
					switch(step){
					//***************** Handling Requests *******************
					case (ACLMessage.REQUEST):
						
						ce = getContentManager().extractContent(msg);
						_ac = (Action) ce;
						if(_ac.getAction() instanceof TUControlSequenceRequestSetpoint){
							System.out.println(this.getAgent().getAID().getLocalName()+"******* TU Control Request Message Received *******");
							TUControlSequenceRequestSetpoint _csrs = (TUControlSequenceRequestSetpoint)_ac.getAction();
							addBehaviour(new ControlSequencePerformer(msg.shallowClone(), _csrs.getTuName(), _csrs.getNewSetpoint()));
						}else if(_ac.getAction() instanceof TUControlSequenceRequestLoadProfile) {
							System.out.println(this.getAgent().getAID().getLocalName()+"******* TU Control Request Message Received *******");
							TUControlSequenceRequestLoadProfile _csrlp = (TUControlSequenceRequestLoadProfile)_ac.getAction();
							addBehaviour(new ControlSequencePerformer(msg.shallowClone(), _csrlp.getTuName(), _csrlp.getNewLoadProfile()));
						}else if(_ac.getAction() instanceof FreqRelayEnableRequest) {
							System.out.println(this.getAgent().getAID().getLocalName()+"******* Frequency Relay Enable Message Received *******");
							FreqRelayEnableRequest _frer = (FreqRelayEnableRequest)_ac.getAction();
							addBehaviour(new FREnablePerformer(msg.shallowClone(),_frer.getTuName()));
						}else if(_ac.getAction() instanceof FreqRelayBlockRequest) {
							System.out.println(this.getAgent().getAID().getLocalName()+"******* Frequency Relay Enable Message Received *******");
							FreqRelayBlockRequest _frbr = (FreqRelayBlockRequest)_ac.getAction();
							addBehaviour(new FRBlockPerformer(msg.shallowClone(),_frbr.getTuName()));
						}else if(_ac.getAction() instanceof RequestInfoRequest) {
							System.out.println(this.getAgent().getAID().getLocalName()+"******* RequestInfoRequest Message Received *******");
							addBehaviour(new RequestInfoResponsePerformer(msg.shallowClone()));
						}
						break;
					//***************** Handling Call For Proposals *******************
					case (ACLMessage.CFP):
						System.out.println(this.getAgent().getAID().getLocalName()+"******* CFP Message Received *******");
						ce = getContentManager().extractContent(msg);
						_ac = (Action) ce;
						//choosing the right way of processing the CFP by comparing it to the corresponding ontology-class
						//****** CFPSchedulingSequence ******
						if(_ac.getAction() instanceof CFPSchedulingSequence){
							CFPSchedulingSequence _cfpss = (CFPSchedulingSequence)_ac.getAction();
							String timeBegin = _cfpss.getTimeBegin();
							String timeEnd = _cfpss.getTimeEnd();
							String expiration;
							//convert the jade date format to the "yyyy-MM-dd HH:mm:ss" format
							SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
							formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
							expiration = formatter.format(msg.getReplyByDate());
							//String conversationID = msg.getConversationId();						
							System.out.println(this.getAgent().getAID().getLocalName()+" Time-window: From: " +timeBegin + " Till: " +timeEnd + " Response-By: " + expiration);
							addBehaviour(new SchedulingSequencePerformer(msg.shallowClone()));
						}
						break;
					case (ACLMessage.SUBSCRIBE):
						System.out.println(this.getAgent().getAID().getLocalName()+"******* SUBSCRIBE Message Received *******");
						ce = getContentManager().extractContent(msg);
						_ac = (Action) ce;
						//choosing the right way of processing the CFP by comparing it to the corresponding ontology-class
						//****** BalancingSequenceSubscribe ******
						if(_ac.getAction() instanceof BalancingSequenceSubscribe){
							addBehaviour(new BalancingSequencePerformer(msg.shallowClone()));
						}
						break;
					//***************** Handling Informs *******************
					case (ACLMessage.INFORM):
						
						ce = getContentManager().extractContent(msg);
						Predicate _pc = (Predicate) ce;
						if(_pc instanceof LoadTimeWindowsShareInform){
							System.out.println(this.getAgent().getAID().getLocalName()+"******* LoadTimeWindows Share Received *******");
							LoadTimeWindowsShareInform _ltwsi = (LoadTimeWindowsShareInform)_pc;
							InterfacePayloadLoadTimeWindows payload = new InterfacePayloadLoadTimeWindows(_ltwsi.getLoadTimeWindowsReference(),
									_ltwsi.getWindowHighBegin(),_ltwsi.getWindowHighEnd(), _ltwsi.getWindowLowBegin(), _ltwsi.getWindowLowEnd());
//							ConsumingRest_TU putInstance = new ConsumingRest_TU();
//							putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.LOADTIMEWINDOWSINFORM, payload);
						}
						break;	
						//***************** Handling Cancels *******************
					case (ACLMessage.CANCEL):
						ce = getContentManager().extractContent(msg);
						Predicate _pcc = (Predicate) ce;
						//******** Handling messages of the CancelOperation sequence ************
						if(_pcc instanceof CancelOperation){
							System.out.println(this.getAgent().getAID().getLocalName()+"******* Cancel-Operation Sequence: CANCEL Message Received *******");
							addBehaviour(new ReceiveCancelOperationPerformer(msg.shallowClone()));
						}
						break;
					default:
						System.out.println(this.getAgent().getAID().getLocalName()+"******* Message cannot be processed *******");
					}
				}
				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				}
			}
			else {
				block();
			}
		}
	}

	//********************** 0. TU Input Listener **********************
	private class TUInputListener extends CyclicBehaviour{
		//this input listener is called as an threaded behaviour so the extra thread can go into wait and resume if the rest ws notifies via the LOCK
		//it processes the Node-RED inputs and this threaded approach is needed to prevent an increased CPU usage 
		private static final long serialVersionUID = 1L;

		public void action() {
			synchronized(TuVariables.LOCK){
				try {
					TuVariables.LOCK.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				//******************* Starting the Accounting Sequence (generic behaviour) ***************
				if (TuVariables.accountingTriggerReference && TuVariables.accountingTriggerECP) {
					TuVariables.accountingTriggerReference = false;
					TuVariables.accountingTriggerECP = false;
					myAgent.addBehaviour(new AccountingSequencePerformer(TuVariables.energyConsumptionProfile, TuVariables.accountingTuName, TuVariables.accountingReferenceID));
				}
				//******************* Starting the loadTimeWindows Sequence (generic behaviour) ***************
				if (TuVariables.loadTimeWindowsTrigger) {
					TuVariables.loadTimeWindowsTrigger = false;
					myAgent.addBehaviour(new LoadTimeWindowsRequestPerformer(TuVariables.loadTimeWindowsReference));
					TuVariables.resetLoadTimeWindows();
				}	
				//******************* Starting the loadTimeWindows Sequence (generic behaviour) ***************
				if (TuVariables.freqRelayConfirmTrigger) {
					TuVariables.freqRelayConfirmTrigger = false;
					myAgent.addBehaviour(new FRTriggeredPerformer(TuVariables.freqRelayConfirmTUName));
					TuVariables.resetfreqRelayConfirm();
				}	
				//******************* Starting the operationCancel Sequence (generic behaviour) ***************
				if (TuVariables.cancelOperationTrigger) {
					TuVariables.cancelOperationTrigger = false;
					myAgent.addBehaviour(new SendCancelOperationPerformer(TuVariables.cancelOperationTuName, TuVariables.cancelOperationReference));
				}
				//******************* Starting the RequestInfos Sequence (generic behaviour) ***************
				if (TuVariables.requestInfosTrigger) {
					TuVariables.requestInfosTrigger = false;
					myAgent.addBehaviour(new RequestInfoPerformer());
				}
			}
		}
	
		
		
	}
	
	//********************** 1. Scheduling Sequence **********************
	private class SchedulingSequencePerformer extends Behaviour {
		private static final long serialVersionUID = 1L;
		
		private boolean validOffer = false; 			//state of the TU Offer (true = valid offer and TU wants to propose)
		private int step = 2;							//step counter for state machine
		private String timeBegin;
		private String timeEnd;
		private String expiration;
		private String conversationID = "empty";
		private String tuName = "noNameSet";
		private ACLMessage msg;
		private Date date;
		private Date dateTimeBegin;
		
		private SchedulingSequencePerformer(ACLMessage _msg, String _timeBegin, String _timeEnd, String _expiration, String _conversationID) {
			msg = _msg;
			timeBegin = _timeBegin;
			timeEnd = _timeEnd;
			expiration = _expiration;
			conversationID = _conversationID;
		}
		
		private SchedulingSequencePerformer(ACLMessage _msg) {
			msg = _msg;
		}
		
		
		public void onStart() {
			ContentElement ce;
			Action _ac;
			try {
				ce = getContentManager().extractContent(msg);
				_ac = (Action) ce;
				//choosing the right way of processing the CFP by comparing it to the corresponding ontology-class
				//****** CFPSchedulingSequence ******
				if(_ac.getAction() instanceof CFPSchedulingSequence){
					CFPSchedulingSequence _cfpss = (CFPSchedulingSequence)_ac.getAction();
					timeBegin = _cfpss.getTimeBegin();
					timeEnd = _cfpss.getTimeEnd();
					//convert the jade date format to the "yyyy-MM-dd HH:mm:ss" format
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
					formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
					expiration = formatter.format(msg.getReplyByDate());
					conversationID = msg.getConversationId();
				}
			} catch (CodecException | OntologyException e) {
				e.printStackTrace();
			}
		}
		
		public void action(){
		switch(step){
			case 0: 
				//******* sending the scheduling request information to the Planning system *******
				ConsumingRest_TU putInstance = new ConsumingRest_TU();
				InterfacePayloadPlanning payload = new InterfacePayloadPlanning(timeBegin, timeEnd, expiration, conversationID);
				System.out.println(payload.toString());
				TuVariables.schedulingTrigger = false;			//resetting the schedulingTrigger so the ERP system needs to send a new propose to activate the response of the agent
				putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.STARTSCHEDULING, payload);
				//putInstance.getNodeRed(Addresses.URL_NODERED, PutVariable.ALL);
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
				try {
					date = formatter.parse(expiration);
					dateTimeBegin = formatter.parse(timeBegin);
					msg.setReplyByDate(date);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				step=1;
				break;
			case 1:
				//******* waiting for the response of the Planning system *******
				Date dateNow = new Date();
				//scheduling plans has been sent or the the call has been refused --> switch to the next case
				if(TuVariables.schedulingTrigger && (TuVariables.referenceID.equals(conversationID))){
					validOffer = true;
					tuName = TuVariables.schedulingTUName;
					TuVariables.schedulingTrigger = false;
					step = 2;
				}else if ((TuVariables.refuseSchedulingTrigger && (TuVariables.referenceID.equals(conversationID))) || dateNow.after(date)){
					validOffer = false;	
					TuVariables.refuseSchedulingTrigger = false;
					step = 2;
				}
				
				break;
			case 2:
				byte[] schedulingplan = new byte[] {(byte)0x00};
				int randomNum = ThreadLocalRandom.current().nextInt(1000, 5000);
				try {
					Thread.sleep(randomNum);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				if(randomNum > 1400) {
					validOffer = true;
				}else {
					validOffer = false;
				}
				//******* sending an answer to the VPP according to the reaction of the ERP system **********
//				Date replyDate = msg.getReplyByDate();							
//				System.out.println("Date from Instant:\n" + replyDate + " long: " + replyDate.getTime());
				try{
					if(validOffer) {
					//Sending a Propose-Answer
					ACLMessage reply = msg.createReply(); //create a new ACLMessage that is a reply to this message. In particular, it sets the following parameters of the new message: receiver, language, ontology, protocol, conversation-id, in-reply-to, reply-with
					reply.setLanguage(codec.getName());
					reply.setPerformative(ACLMessage.PROPOSE);
					ContentManager cm = myAgent.getContentManager();
					ContentElementList cel = new ContentElementList();
					CFPSchedulingSequencePropose newSchedulingSequencePropose = new CFPSchedulingSequencePropose();
				    newSchedulingSequencePropose.setSchedulingPlan(schedulingplan);
				    newSchedulingSequencePropose.setAgentName(myAgent.getLocalName());
				    newSchedulingSequencePropose.setTuName(getAID().getLocalName());
					//Action act = new Action();
					//act.setAction(newSchedulingSequencePropose); 	//Adding the Action the Agent has to perform
					//act.setActor(new AID("*", AID.ISGUID));			//Adding an dummy Agent, because the actor field is mandatory (source: http://jade.tilab.com/pipermail/jade-develop/2010q4/016200.html)
					cel.add(newSchedulingSequencePropose);
					cm.fillContent(reply, cel);
					myAgent.send(reply);
					}
				if(!validOffer) {
					//Sending a REFUSE-Answer
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.REFUSE);
					ContentManager cm = myAgent.getContentManager();
					ContentElementList cel = new ContentElementList();
					CFPSchedulingSequenceRefuse newSchedulingSequenceRefuse = new CFPSchedulingSequenceRefuse();
					newSchedulingSequenceRefuse.setAgentName(myAgent.getLocalName());
					newSchedulingSequenceRefuse.setTuName(getAID().getLocalName());
					cel.add(newSchedulingSequenceRefuse);
					cm.fillContent(reply, cel);
					myAgent.send(reply);
					step = 99;
					}
				}
				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
				} 
				validOffer = false;
				step = 3;
			break;
			//********** Receiving answers from VPP-Agent and informing the Planning-System **********
			case 3:
				MessageTemplate mt = MessageTemplate.and(
							MessageTemplate.MatchOntology(ontology.getName()),MessageTemplate.and(
							MessageTemplate.MatchConversationId(conversationID),MessageTemplate.and(
							MessageTemplate.MatchLanguage(codec.getName()),MessageTemplate.or(
							MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL),
							MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL)))));
				msg = receive(mt);		//returns the first message of the message queue with the corresponding template
				if (msg != null){	
					ContentElement ce = null;	
					if (msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
						System.out.println(this.getAgent().getAID().getLocalName()+"******* ACCEPT Message Received *******");
						try {
							ce = getContentManager().extractContent(msg);
							Predicate _pc = (Predicate) ce;
							//***************** CFPSchedulingSequence Accept *******************
							if(_pc instanceof CFPSchedulingSequenceAccept){
								timeBegin = ((CFPSchedulingSequenceAccept) _pc).getSchedulingStart();
								timeEnd = ((CFPSchedulingSequenceAccept) _pc).getSchedulingEnd();
								//********** Inform the ERP-System about the results**********
								//******* the sending the new SchedulingPlan to the Planning System ******** 
//								putInstance = new ConsumingRest_TU();
//								payload = new InterfacePayloadPlanning(timeBegin, timeEnd, conversationID);
//								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.SCHEDULINGACCEPTED, payload);
								step = 5;
							}
						} catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					}else if (msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
						System.out.println(this.getAgent().getAID().getLocalName()+"******* REJECT Message Received *******");
						try {
							ce = getContentManager().extractContent(msg);
							Predicate _pc = (Predicate) ce;
						//***************** CFPSchedulingSequence Reject *******************
							if(_pc instanceof CFPSchedulingSequenceReject){			
								//********** Inform the Planning-System about the results**********
								//******* the sending the reject info to the Planning System ******** 
//								putInstance = new ConsumingRest_TU();
//								payload = new InterfacePayloadPlanning(conversationID);
//								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.SCHEDULINGREJECTED, payload);
								step = 99;
							}
						}catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					}
				}else {
					block();
				}
				break;
			//********** Receiving Info from Planning-System about the results**********
			case 4:
				dateNow = new Date();
//				SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
//				formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
//				formatter2.format(dateNow);
				if(TuVariables.schedulingInformTrigger && (TuVariables.referenceID.equals(conversationID))){
					TuVariables.schedulingInformTrigger = false;
					validOffer = true;
					tuName = TuVariables.schedulingTUName;
					step = 5;
				}else if ((TuVariables.schedulingFailureTrigger && (TuVariables.referenceID.equals(conversationID))) || dateNow.after(dateTimeBegin)){
					TuVariables.schedulingFailureTrigger = false;
					validOffer = false;	
					tuName = TuVariables.schedulingTUName;
					step = 5;
				}
				step = 5;
				break;
			//********** Confirm the VPP-Agent about the results from the ERP-System**********
			case 5:
				validOffer=true;
				randomNum = ThreadLocalRandom.current().nextInt(1000, 5000);
				try {
					Thread.sleep(randomNum);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				if(randomNum > 1400) {
					validOffer = true;
				}else {
					validOffer = false;
				}
				try{
					if(validOffer) {
						System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending INFORM to VPP *******");
						//Sending a INFORM-Done-Answer
						ACLMessage reply = msg.createReply(); //create a new ACLMessage that is a reply to this message. In particular, it sets the following parameters of the new message: receiver, language, ontology, protocol, conversation-id, in-reply-to, reply-with
						reply.setLanguage(codec.getName());
						reply.setPerformative(ACLMessage.INFORM);
						ContentManager cm = myAgent.getContentManager();
						ContentElementList cel = new ContentElementList();
						CFPSchedulingSequenceDone newSchedulingSequenceDone = new CFPSchedulingSequenceDone();
						newSchedulingSequenceDone.setAgentName(myAgent.getLocalName());
						newSchedulingSequenceDone.setTuName(myAgent.getLocalName());
						cel.add(newSchedulingSequenceDone);
						cm.fillContent(reply, cel);
						myAgent.send(reply);
						step = 99;
					}
					else{
						System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending FAILURE to VPP *******");
					//Sending a FAILURE-Answer
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.FAILURE);
					ContentManager cm = myAgent.getContentManager();
					ContentElementList cel = new ContentElementList();
					CFPSchedulingSequenceFailure newSchedulingSequenceFailure = new CFPSchedulingSequenceFailure();
					newSchedulingSequenceFailure.setAgentName(myAgent.getLocalName());
					newSchedulingSequenceFailure.setTuName(myAgent.getLocalName());
					cel.add(newSchedulingSequenceFailure);
					cm.fillContent(reply, cel);
					myAgent.send(reply);
					step = 99;
					}
				}
				catch (CodecException ce) {
					ce.printStackTrace();
				}
				catch (OntologyException oe) {
					oe.printStackTrace();
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
	
	//********************** 2. TU Control Sequence **********************
	private class ControlSequencePerformer extends Behaviour {
		private static final long serialVersionUID = 1L;
			private int step = 0;							//step counter for state machine
			private boolean newSetpointVersion = false;
			private ACLMessage msg;
			private String tuName; 
			private byte[] newLoadProfile;
			private int newSetpoint;
			
			private ControlSequencePerformer(ACLMessage _msg, String _tuName, int _newSetpoint) {
				msg = _msg;
				tuName = _tuName;
				newSetpoint = _newSetpoint;
				newSetpointVersion = true;
			}
			
			private ControlSequencePerformer(ACLMessage _msg, String _tuName, byte[] _newLoadProfile) {
				msg = _msg;
				tuName = _tuName;
				newLoadProfile = _newLoadProfile;
			}
			
			
			public void onStart(){
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending newSetpoint to TU *******");
			}
			
			public void action(){
				
				switch(step){
					case 0: 
						//******* sending the scheduling request information to the TU system *******
//						if(newSetpointVersion) {
//							ConsumingRest_TU putInstance = new ConsumingRest_TU();
//							InterfacePayloadNewSetpoint payload = new InterfacePayloadNewSetpoint(newSetpoint, tuName);
//							putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.SETPOINT, payload);	
//						}else {
//							ConsumingRest_TU putInstanceOne = new ConsumingRest_TU();
//							InterfacePayloadAgentReference payloadOne = new InterfacePayloadAgentReference(getAID().getLocalName(), tuName);
//							putInstanceOne.putNodeRed(Addresses.URL_NODERED, PutVariable.LOADPROFILEINFO, payloadOne);	
//							ConsumingRest_TU putInstanceTwo = new ConsumingRest_TU();
//							putInstanceTwo.putNodeRed(Addresses.URL_NODERED, PutVariable.LOADPROFILE, newLoadProfile);	
//						}
						step = 1;
						break;
					case 1:
						if(TuVariables.requestDoneTrigger) {
							TuVariables.requestDoneTrigger = false;
							step = 99;
						}else if(TuVariables.requestFailureTrigger) {
							TuVariables.requestFailureTrigger = false;
							System.out.println(this.getAgent().getAID().getLocalName()+"******* Setting newSetpoint failed *******");
							tuName=TuVariables.tuName;
							//Sending a FAILURE-Answer
							try {
								ACLMessage reply = msg.createReply();
								reply.setPerformative(ACLMessage.FAILURE);
								reply.setConversationId("tuControlSequenceFailed");
								ContentManager cm = myAgent.getContentManager();
								ContentElementList cel = new ContentElementList();
								TUControlSequenceFailure newTUControlSequenceFailure = new TUControlSequenceFailure();
								newTUControlSequenceFailure.setTuName(tuName);
								newTUControlSequenceFailure.setAgentName(getAID().getLocalName());
								//newTUControlSequenceFailure.setTuControlSequenceFailed("tuControlSequenceFailed");
								cel.add(newTUControlSequenceFailure);
								cm.fillContent(reply, cel);
								myAgent.send(reply);
							} catch (CodecException e) {
								e.printStackTrace();
							} catch (OntologyException e) {
								e.printStackTrace();
							}
							step = 99;
						}
						step = 99;
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

	//********************** 3. TU Balancing Sequence **********************
	private class BalancingSequencePerformer extends Behaviour {
		private static final long serialVersionUID = 1L;
		
		private int step = 1;
		private int updateRate = 0;
		private String balancingStart;
		private String balancingEnd;
		private String tuName = "noNameSet";
		//private String agentName = "noNameSet";
		//private String expiration;
		private Date balancingStartDate;
		private Date balancingEndDate;
		private ACLMessage msg;
		private Timer timer = new Timer(); //timer need to start the balancing updating process 
		
		//constructor that prepares the schedulingSequencePerformer
		public BalancingSequencePerformer(ACLMessage _msg) {
				msg = _msg;
				}
		
		public void onStart(){
			System.out.println(this.getAgent().getAID().getLocalName()+"******* BalancingRequestBehaviour started ********");
			try {
				ContentElement ce;
				Action _ac;
				ce = getContentManager().extractContent(msg);
				_ac = (Action) ce;
				BalancingSequenceSubscribe _bss = (BalancingSequenceSubscribe)_ac.getAction();
				balancingStart = _bss.getBalancingStart();
				balancingEnd = _bss.getBalancingEnd();
				tuName = _bss.getTuName();
				//SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
				//formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
				//expiration = formatter.format(msg.getReplyByDate());	
				updateRate = _bss.getUpdateRate();
			} catch (CodecException | OntologyException e) {
				step = 99;
				e.printStackTrace();
			}
			try {
				//setting the expiration time for the request of the VPP
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
				formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
				//also setting the balancingEnd date for the proper was of ending this sequence
				balancingEndDate = formatter.parse(balancingEnd);
				balancingStartDate = formatter.parse(balancingStart);
				
			} catch (ParseException e) {
				step = 99;
				e.printStackTrace();
			}
		}

		public void action(){

			
			switch(step){
			case 0: 
				//******* sending the balancing request information to the TU system *******
				ConsumingRest_TU putInstance = new ConsumingRest_TU();
				InterfacePayloadBalancing payload = new InterfacePayloadBalancing(balancingStart, balancingEnd, tuName);
				putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.BALANCING, payload);
				step = 1;
				break;
			case 1:
				int randomNum = ThreadLocalRandom.current().nextInt(20, 100);
				try {
					Thread.sleep(randomNum);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//getting the current expiration time and date so the TU can send a refuse message if the deadline is done 
				Date dateNow = new Date();
				//SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
				//formatter2.format(dateNow); 
//				if (dateNow.after(balancingStartDate)){
//					step = 99;
//				}
				if(step == 1) {
					TuVariables.balancingAgreeTrigger = false;
					System.out.println(this.getAgent().getAID().getLocalName()+"******* Balancing Request accepted *******");
					//Sending an AGREE-Answer to the VPP
					try {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.AGREE);
						ContentManager cm = myAgent.getContentManager();
						ContentElementList cel = new ContentElementList();
						BalancingSequenceAgree newBalancingSequenceAgree = new BalancingSequenceAgree();
						newBalancingSequenceAgree.setTuName(tuName);
						newBalancingSequenceAgree.setAgentName(getAID().getLocalName());
						cel.add(newBalancingSequenceAgree);
						cm.fillContent(reply, cel);
						myAgent.send(reply);
					} catch (CodecException | OntologyException e) {
						e.printStackTrace();
						step = 99;
					}
					step = 2;
				}else if(TuVariables.balancingRefuseTrigger && tuName.equals(TuVariables.balancingTuName) || dateNow.after(balancingEndDate)) {
					TuVariables.balancingRefuseTrigger = false;
					System.out.println(this.getAgent().getAID().getLocalName()+"******* Balancing Request Failed *******");
					//Sending a REFUSE-Answer to the VPP
					try {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REFUSE);
						ContentManager cm = myAgent.getContentManager();
						ContentElementList cel = new ContentElementList();
						BalancingSequenceRefuse newBalancingSequenceRefuse = new BalancingSequenceRefuse();
						newBalancingSequenceRefuse.setTuName(TuVariables.balancingTuName);
						newBalancingSequenceRefuse.setAgentName(getAID().getLocalName());
						cel.add(newBalancingSequenceRefuse);
						cm.fillContent(reply, cel);
						myAgent.send(reply);
					} catch (CodecException | OntologyException e) {
						e.printStackTrace();
					}
					step = 99;
				}
				break;
			case 2:
				Date dateNow2 = new Date();
//				SimpleDateFormat formatter3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
//				formatter3.format(dateNow2);
				//start a separate performer that replies every n second to the VPP
				if (dateNow2.after(balancingStartDate)){
					step = 3;
					timer.schedule(new balancingUpdatePerformer(msg.shallowClone()), 0, updateRate);
				}
				break;
				
			case 3:
				if(TuVariables.balancingInformTrigger && tuName.equals(TuVariables.balancingTuName)) {
					TuVariables.balancingInformTrigger = false;
				}else if(TuVariables.balancingInformInstantTrigger && tuName.equals(TuVariables.balancingTuName)) {
					TuVariables.balancingInformInstantTrigger = false;
					try {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.INFORM);
						ContentManager cm = myAgent.getContentManager();
						ContentElementList cel = new ContentElementList();
						BalancingSequenceInform newBalancingSequenceInform = new BalancingSequenceInform();
						//giving the data to the newTUDataSet
						TUDataSet newTUDataSet = new TUDataSet(TuVariables.feedIn, TuVariables.operatingPoint, 	
						TuVariables.leadingOperatingPoint, TuVariables.currentValueFR, TuVariables.assignedPool,		
						TuVariables.status, TuVariables.frequency, TuVariables.aFRRsetpoint, TuVariables.aFRRsetpointEcho,		
						TuVariables.setpointFR,	TuVariables.aFRRGradientPOS, TuVariables.aFRRGradientNEG,		
						TuVariables.capacityPOS, TuVariables.capacityNEG, TuVariables.holdingCapacityPOS,
						TuVariables.holdingCapacityNEG,	TuVariables.controlBandPOS, TuVariables.controlBandNEG);	
						newBalancingSequenceInform.setTUDataSet(newTUDataSet);
						newBalancingSequenceInform.setTuName(TuVariables.balancingTuName);
						newBalancingSequenceInform.setAgentName(getAID().getLocalName());
						cel.add(newBalancingSequenceInform);
						cm.fillContent(reply, cel);
						myAgent.send(reply);
						System.out.println(getAgent().getAID().getLocalName()+"******* Spontaneous Inform sent *******");
						TuVariables.resetBalancing();
					} catch (CodecException | OntologyException e) {
						e.printStackTrace();
					}
				}else if(TuVariables.balancingFailureTrigger && tuName.equals(TuVariables.balancingTuName)) {
					TuVariables.balancingFailureTrigger = false;
						
					System.out.println(this.getAgent().getAID().getLocalName()+"******* Balancing Request Failed *******");
					//Sending a FAILURE-Answer
					try {
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.FAILURE);
						ContentManager cm = myAgent.getContentManager();
						ContentElementList cel = new ContentElementList();
						BalancingSequenceFailure newBalancingSequenceFailure = new BalancingSequenceFailure();
						newBalancingSequenceFailure.setTuName(TuVariables.balancingTuName);
						newBalancingSequenceFailure.setAgentName(getAID().getLocalName());
						cel.add(newBalancingSequenceFailure);
						cm.fillContent(reply, cel);
						myAgent.send(reply);
					} catch (CodecException | OntologyException e) {
						e.printStackTrace();
					}
					step = 99;
					}
				//End the balancing after the balancing end Time
				Date _dateNow = new Date();
//				SimpleDateFormat formatter3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
//				formatter3.format(_dateNow); 
				if (_dateNow.after(balancingEndDate)){
					step = 99;
				}
				break;
			case 99: 
				myAgent.addBehaviour(tbf.wrap(new ScheduleAccounting(tuName,"accounting-"+tuName)));
				//myAgent.addBehaviour(new ScheduleAccounting(tuName,"accounting-"+tuName));
				TuVariables.resetScheduling();
				reset();
				timer.cancel();				//stopping the updating behaviour	
				step = 100;
				break;
			default:
				step = 99;
			}
		}
	public boolean done(){
		return step == 100;
		}
		
	private class balancingUpdatePerformer extends TimerTask {
			private ACLMessage msg;
			
			balancingUpdatePerformer(ACLMessage _msg){
				msg = _msg;
			}
		    public void run() {
				//Sending a INFORM-Answer
				try {
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.INFORM);
					ContentManager cm = myAgent.getContentManager();
					ContentElementList cel = new ContentElementList();
					BalancingSequenceInform newBalancingSequenceInform = new BalancingSequenceInform();
					//TUDataSet newTUDataSet = new TUDataSet();
					//giving the data to the newTUDataSet
					TuVariables.randomBalancing();
					
					TUDataSet newTUDataSet = new TUDataSet(TuVariables.feedIn, TuVariables.operatingPoint, 	
					TuVariables.leadingOperatingPoint, TuVariables.currentValueFR, TuVariables.assignedPool,		
					TuVariables.status, TuVariables.frequency, TuVariables.aFRRsetpoint, TuVariables.aFRRsetpointEcho,		
					TuVariables.setpointFR,	TuVariables.aFRRGradientPOS, TuVariables.aFRRGradientNEG,		
					TuVariables.capacityPOS, TuVariables.capacityNEG, TuVariables.holdingCapacityPOS,
					TuVariables.holdingCapacityNEG,	TuVariables.controlBandPOS, TuVariables.controlBandNEG);	
					newBalancingSequenceInform.setTUDataSet(newTUDataSet);
					newBalancingSequenceInform.setTuName(getAID().getLocalName());
					newBalancingSequenceInform.setAgentName(getAID().getLocalName());
					cel.add(newBalancingSequenceInform);
					cm.fillContent(reply, cel);
					myAgent.send(reply);
					System.out.println(getAgent().getAID().getLocalName()+"******* Inform sent *******");
					TuVariables.resetBalancing();
				
				} catch (CodecException | OntologyException e) {
					e.printStackTrace();
				}
		    }
		}
	}
	
	//********************** 4. Accounting Sequence **********************
	public class AccountingSequencePerformer extends Behaviour {
		private static final long serialVersionUID = 1L;
		
		private int step = 0;
		private String tuName = "noNameSet";
		private String agentName = "noNameSet";
		//private String date;
		private String referenceID = "noIDSet";
		private String conversationID;
		private byte[]  energyConsumptionProfile;
		private ArrayList <AID> vppAgents = new ArrayList<AID>();

		AccountingSequencePerformer(byte[]  _energyConsumptionProfile, String _tuName, String _referenceID){
			energyConsumptionProfile = _energyConsumptionProfile;
			tuName = _tuName;
			referenceID = _referenceID;
		}
		
		
		public void onStart(){
			System.out.println(this.getAgent().getAID().getLocalName()+"******* AccountingSequenceBehaviour started ********");
			agentName = getAID().getLocalName();
			Date dateNow = new Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
			formatter.format(dateNow); 
			//date = dateNow.toString();
			conversationID = "accountingInform";
		}

		public  void action(){
			switch(step){
			case 0:
				//CHANGED FOR SIMULATION****
				step =1;
		    	break;
			// sending the energy consumption profile to the VPP
			case 1:
				System.out.println(this.getAgent().getAID().getLocalName()+"******* AccountingSequencePerformer started ********");
				//searching for the complete AID of the referenced agent
				DFAgentDescription sdSearchTemplate = new DFAgentDescription() ;		//contains the service description list that the schedulingSequence uses
				sdSearchTemplate.addLanguages(codec.getName());
				sdSearchTemplate.addOntologies(ontology.getName());
				ServiceDescription sd = new ServiceDescription();
				sd.setType("VPP");
				sdSearchTemplate.addServices(sd);
					//searching the DF for the Agents that can provide the service
					try {
						DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
						for(int j = 0; j < result.length; j++){
							vppAgents.add(result[j].getName());
						}
					}
					catch (FIPAException fe){
						fe.printStackTrace();
					}
					sdSearchTemplate.clearAllServices();
					if(vppAgents.size()>0) {
						step = 2;
					}else {
						System.out.println("No Agent can be found under that name");
						step = 99;
					}			
				break;
			case 2:
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out INFORM (energy consumption profiles) to VPP *******");
				
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setOntology(ontology.getName());
				msg.setLanguage(codec.getName());
				msg.setConversationId(conversationID);	
				AID receiver = new AID();
				for(int j = 0; j < vppAgents.size(); j++){
					receiver = vppAgents.get(j);
				}
				msg.addReceiver(receiver);	
				try {
					ContentManager cm = myAgent.getContentManager();
					ContentElementList cel = new ContentElementList();
					AccountingSequenceInform newAccountingSequenceInform = new AccountingSequenceInform();
					newAccountingSequenceInform.setAgentName(agentName);
					newAccountingSequenceInform.setTuName(tuName);
					newAccountingSequenceInform.setEnergyConsumptionProfile(energyConsumptionProfile);
					cel.add(newAccountingSequenceInform);
					cm.fillContent(msg, cel);
					myAgent.send(msg);
					msg.reset();
					step = 3;
				} catch (CodecException | OntologyException e){
					e.printStackTrace();
					step = 99;
				}
				break;
			case 3:	
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
							if(_pc instanceof AccountingSequenceInformReceived){
								//********** Inform the EMS-System about the results**********				
//								AccountingSequenceInformReceived _asir = (AccountingSequenceInformReceived)_pc;
//								InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(referenceID, getAID().getLocalName(),_asir.getTuName() );
//								ConsumingRest_TU putInstance = new ConsumingRest_TU();
//								putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.ACCOUNTINGECPRECEIVED, payload);							
								step = 99;
							}
						} catch (CodecException | OntologyException e) {
							e.printStackTrace();
						}
					}else if(msg.getPerformative() == ACLMessage.FAILURE) {
						step = 99;
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
	
	//********************** 5.LoadTimeWindows Request Sequence **********************	
	public class LoadTimeWindowsRequestPerformer extends OneShotBehaviour{
		private static final long serialVersionUID = 1L;
		
		private String loadTimeWindowsReference;
		
		LoadTimeWindowsRequestPerformer( String _loadTimeWindowsReference){
			loadTimeWindowsReference = _loadTimeWindowsReference;
		}

		public void action() {
			ArrayList <AID> vppAgents = new ArrayList<AID>();
			System.out.println(this.getAgent().getAID().getLocalName()+"******* loadTimeWindowsSequence started ********");
			//searching for the complete AID of the referenced agent
			DFAgentDescription sdSearchTemplate = new DFAgentDescription() ;		//contains the service description list that the schedulingSequence uses
			sdSearchTemplate.addLanguages(codec.getName());
			sdSearchTemplate.addOntologies(ontology.getName());
			ServiceDescription sd = new ServiceDescription();
			sd.setType("VPP");
			sdSearchTemplate.addServices(sd);
			//searching the DF for the Agents that can provide the service
			try {
				DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
				for(int j = 0; j < result.length; j++){
					vppAgents.add(result[j].getName());
				}
			}
			catch (FIPAException fe){
				fe.printStackTrace();
			}
			sdSearchTemplate.clearAllServices();
			if(vppAgents.size()>0) {
				
				
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out Request (load time windows) to VPP *******");
				
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setOntology(ontology.getName());
				msg.setLanguage(codec.getName());
				msg.setConversationId("loadTimeWindows");	
				AID receiver = new AID();
				for(int j = 0; j < vppAgents.size(); j++){
					receiver = vppAgents.get(j);
				}
				msg.addReceiver(receiver);	
				try {
					ContentManager cm = myAgent.getContentManager();
					ContentElementList cel = new ContentElementList();
					LoadTimeWindowsShareRequest newLoadTimeWindowsShareRequest = new LoadTimeWindowsShareRequest();
					newLoadTimeWindowsShareRequest.setLoadTimeWindowsReference(loadTimeWindowsReference);
					Action act = new Action();
					act.setAction(newLoadTimeWindowsShareRequest); 			
					act.setActor(new AID("*", AID.ISGUID));		
					cel.add(act);
					cm.fillContent(msg, cel);
					myAgent.send(msg);
					msg.reset();
				} catch (CodecException | OntologyException e){
					e.printStackTrace();
				}
			}else {
				System.out.println("No VPP Agent can be found");
			}			
		}
	}
				
	//********************** 6.A Frequency Relay Enable Sequence **********************
	private class FREnablePerformer extends Behaviour {
		private static final long serialVersionUID = 1L;
			private int step = 1;							//step counter for state machine
			private ACLMessage msg;
			private String tuName = "NoNameSet"; 
			
			private FREnablePerformer(ACLMessage _msg, String _tuName) {
				msg = _msg;
				tuName = _tuName;
			}
			
		
			public void onStart(){
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending Enable Frequency Relay to TU *******");
			}
			
			public void action(){
				
				switch(step){
					case 0: 
						//******* sending the request to the TU system *******
						ConsumingRest_TU putInstance = new ConsumingRest_TU();
						InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(tuName);
						putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.FRENABLEREQUEST, payload);	
						step = 1;
						break;
					case 1:
						int randomNum = ThreadLocalRandom.current().nextInt(20, 200);
						try {
							Thread.sleep(randomNum);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							if(step ==1) {
								TuVariables.freqRelayEnableInformTrigger = false;
//								tuName=TuVariables.freqRelayEnableTUName;
								ACLMessage reply = msg.createReply();
								reply.setPerformative(ACLMessage.INFORM);
								ContentManager cm = myAgent.getContentManager();
								ContentElementList cel = new ContentElementList();
								FreqRelayEnableInform newFreqRelayEnableInform = new FreqRelayEnableInform();
								newFreqRelayEnableInform.setTuName(tuName);
								newFreqRelayEnableInform.setAgentName(getAID().getLocalName());
								cel.add(newFreqRelayEnableInform);
								cm.fillContent(reply, cel);
								myAgent.send(reply);
								step = 99;
							}else if(TuVariables.freqRelayEnableFailureTrigger) {
								TuVariables.freqRelayEnableFailureTrigger = false;
								tuName=TuVariables.freqRelayEnableTUName;
								ACLMessage reply = msg.createReply();
								reply.setPerformative(ACLMessage.FAILURE);
								ContentManager cm = myAgent.getContentManager();
								ContentElementList cel = new ContentElementList();
								FreqRelayEnableFailure newFreqRelayEnableFailure = new FreqRelayEnableFailure();
								newFreqRelayEnableFailure.setTuName(tuName);
								newFreqRelayEnableFailure.setAgentName(getAID().getLocalName());
								cel.add(newFreqRelayEnableFailure);
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
			public boolean done(){
				return step == 100;
				}
			}

	//********************** 6.B Frequency Relay Block Sequence **********************
	private class FRBlockPerformer extends Behaviour {
		private static final long serialVersionUID = 1L;
			private int step = 1;							//step counter for state machine

			private ACLMessage msg;
			private String tuName = "NoNameSet";  

			
			private FRBlockPerformer(ACLMessage _msg, String _tuName) {
				msg = _msg;
				tuName = _tuName;
			}
			
		
			public void onStart(){
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending Block Frequency Relay to TU *******");
			}
			
			public void action(){
				
				switch(step){
					case 0: 
						//******* sending the request to the TU system *******
						ConsumingRest_TU putInstance = new ConsumingRest_TU();
						InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(tuName);
						putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.FRBLOCKREQUEST, payload);	
						step = 1;
						break;
					case 1:
						int randomNum = ThreadLocalRandom.current().nextInt(20, 200);
						try {
							Thread.sleep(randomNum);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							if(step==1) {
								TuVariables.freqRelayDisableInformTrigger = false;
//								tuName=TuVariables.freqRelayDisableTUName;
								ACLMessage reply = msg.createReply();
								reply.setPerformative(ACLMessage.INFORM);
								ContentManager cm = myAgent.getContentManager();
								ContentElementList cel = new ContentElementList();
								FreqRelayBlockInform newFreqRelayBlockInform = new FreqRelayBlockInform();
								newFreqRelayBlockInform.setTuName(tuName);
								newFreqRelayBlockInform.setAgentName(getAID().getLocalName());
								cel.add(newFreqRelayBlockInform);
								cm.fillContent(reply, cel);
								myAgent.send(reply);
								step = 99;
							}else if(TuVariables.freqRelayDisableFailureTrigger) {
								TuVariables.freqRelayDisableFailureTrigger = false;
								tuName=TuVariables.freqRelayDisableTUName;
								ACLMessage reply = msg.createReply();
								reply.setPerformative(ACLMessage.FAILURE);
								ContentManager cm = myAgent.getContentManager();
								ContentElementList cel = new ContentElementList();
								FreqRelayBlockFailure newFreqRelayBlockFailure = new FreqRelayBlockFailure();
								newFreqRelayBlockFailure.setTuName(tuName);
								newFreqRelayBlockFailure.setAgentName(getAID().getLocalName());
								cel.add(newFreqRelayBlockFailure);
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
			public boolean done(){
				return step == 100;
				}
			}	

	//********************** 6.C Frequency Relay Triggered Sequence **********************
	public class FRTriggeredPerformer extends Behaviour {
			private static final long serialVersionUID = 1L;
			
			private int step = 0;
			private String tuName = "noNameSet";
			private String agentName = "noNameSet";
			private String conversationID = "frequencyRelayTriggered";
			private ArrayList <AID> vppAgents = new ArrayList<AID>();

			FRTriggeredPerformer(String _tuName){
				tuName = _tuName;
			}
			
			
			public void onStart(){
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Frequency Relay Triggered Behaviour started ********");
				agentName = getAID().getLocalName();
			}

			public  void action(){
				switch(step){
				// sending the energy consumption profile to the VPP
				case 0:
					//searching for the complete AID of the referenced agent
					DFAgentDescription sdSearchTemplate = new DFAgentDescription() ;		//contains the service description list that the schedulingSequence uses
					sdSearchTemplate.addLanguages(codec.getName());
					sdSearchTemplate.addOntologies(ontology.getName());
					ServiceDescription sd = new ServiceDescription();
					sd.setType("VPP");
					sdSearchTemplate.addServices(sd);
						//searching the DF for the Agents that can provide the service
						try {
							DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
							for(int j = 0; j < result.length; j++){
								vppAgents.add(result[j].getName());
							}
						}
						catch (FIPAException fe){
							fe.printStackTrace();
						}
						sdSearchTemplate.clearAllServices();
						if(vppAgents.size()>0) {
							step = 1;
						}else {
							System.out.println("No Agent can be found under that name");
							step = 99;
						}			
					break;
				case 1:
					System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out INFORM (Frequency Relay Triggered) to VPP *******");
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setOntology(ontology.getName());
					msg.setLanguage(codec.getName());
					msg.setConversationId(conversationID);	
					AID receiver = new AID();
					for(int j = 0; j < vppAgents.size(); j++){
						receiver = vppAgents.get(j);
					}
					msg.addReceiver(receiver);	
					try {
						ContentManager cm = myAgent.getContentManager();
						ContentElementList cel = new ContentElementList();
						FreqRelayStatusInform newFreqRelayStatusInform = new FreqRelayStatusInform();
						newFreqRelayStatusInform.setAgentName(agentName);
						newFreqRelayStatusInform.setTuName(tuName);
						cel.add(newFreqRelayStatusInform);
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
								if(_pc instanceof FreqRelayStatusConfirm){
									//********** Inform the EMS-System about the results**********				
									FreqRelayStatusConfirm _frsc = (FreqRelayStatusConfirm)_pc;
									InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_frsc.getTuName());
									ConsumingRest_TU putInstance = new ConsumingRest_TU();
									putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.FRCONFIRM, payload);							
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

	//********************** 7. CancelOperation Sequence **********************
	public class SendCancelOperationPerformer extends Behaviour {
		private static final long serialVersionUID = 1L;
		
		private int step = 0;
		private String tuName = "noNameSet";
		private String agentName = "noNameSet";
		//private String date;
		private String operationReference = "noIDSet"; 
		private String conversationID;
		private ArrayList <AID> vppAgents = new ArrayList<AID>();

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
				sd.setType("VPP");
				sdSearchTemplate.addServices(sd);
					//searching the DF for the Agents that can provide the service
					try {
						DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
						for(int j = 0; j < result.length; j++){
							vppAgents.add(result[j].getName());
						}
					}
					catch (FIPAException fe){
						fe.printStackTrace();
					}
					sdSearchTemplate.clearAllServices();
					if(vppAgents.size()>0) {
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
				for(int j = 0; j < vppAgents.size(); j++){
					receiver = vppAgents.get(j);
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
				break;
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
								ConsumingRest_TU putInstance = new ConsumingRest_TU();
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
								//********** Inform the Planning-System about the results**********				
								CancelOperationFailure _asir = (CancelOperationFailure)_pc;
								InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_asir.getOperationReference(), getAID().getLocalName(),_asir.getTuName() );
								ConsumingRest_TU putInstance = new ConsumingRest_TU();
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
	
	//********************** ----only for simulation--- **********************
	public class ScheduleAccounting extends OneShotBehaviour{
		private static final long serialVersionUID = 1L;
		String tuName;
		String referenceID;
		
		ScheduleAccounting(String _tuName, String _referenceID){
			tuName = _tuName;
			referenceID = _referenceID;
			
		}
		
		public  void action(){
			// adding a random amount of time for the TU to send the ECPs (not realized via wait, so the agent task does not get suspended)
			int randomNum = ThreadLocalRandom.current().nextInt(0, 60000); 
			try {
				Thread.sleep(randomNum);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			byte[] schedulingplan = new byte[] {(byte)0x01};
			myAgent.addBehaviour(tbf.wrap(new AccountingSequencePerformer(schedulingplan, tuName, referenceID)));

		}
	
	}
	
	//********************** 7.B Receive CancelOperation Sequence **********************
	private class ReceiveCancelOperationPerformer extends Behaviour{
		private static final long serialVersionUID = 1L;
			
		private int step = 1;
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
					ConsumingRest_TU putInstance = new ConsumingRest_TU();
					InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(operationReference, agentName,tuName);
					putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.CANCELOPERATION, payload);
					step = 1;
					break;
			case 1:
//						if(TuVariables.receiveCancelOperationTrigger && operationReference.equals(TuVariables.receiveCancelOperationReference)) {
//							TuVariables.receiveCancelOperationTrigger = false;
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
//					}else if(TuVariables.receiveCancelOperationTriggerFail && operationReference.equals(TuVariables.receiveCancelOperationReference)) {
//					TuVariables.receiveCancelOperationTriggerFail = false;
//					System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out new INFORM to TU: " + tuName +" *******");
//					try {
//						ACLMessage reply = msg.createReply();
//						reply.setPerformative(ACLMessage.FAILURE);
//						ContentManager cm = myAgent.getContentManager();
//						ContentElementList cel = new ContentElementList();
//						CancelOperationFailure newCancelOperationInformReceived = new CancelOperationFailure();
//						newCancelOperationInformReceived.setTuName(tuName);
//						newCancelOperationInformReceived.setOperationReference(operationReference);
//						cel.add(newCancelOperationInformReceived);
//						cm.fillContent(reply, cel);
//						myAgent.send(reply);
//					} catch (CodecException | OntologyException e) {
//						e.printStackTrace();
//					}
//				step = 99;
//				}
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

	//********************** 8.A RequestInfos Sequence **********************
	private class RequestInfoPerformer extends Behaviour {
	private static final long serialVersionUID = 1L;
		
		private int step = 0;
		private String tuName = "VPPAgent";
//			private String agentName = "noNameSet";
		private String conversationID;
		private byte[] infoSet;
		private ArrayList <AID> vppAgents = new ArrayList<AID>();
		private Date expireDate;

		
		public void onStart(){
			System.out.println(this.getAgent().getAID().getLocalName()+"******* RequestInfosBehaviour started ********");
//				agentName = getAID().getLocalName();
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
				sd.setType("VPP");
				sdSearchTemplate.addServices(sd);
					//searching the DF for the Agents that can provide the service
					try {
						DFAgentDescription[] result = DFService.search(myAgent, sdSearchTemplate);
						for(int j = 0; j < result.length; j++){
							vppAgents.add(result[j].getName());
						}
					}
					catch (FIPAException fe){
						fe.printStackTrace();
					}
					sdSearchTemplate.clearAllServices();
					if(vppAgents.size()>0) {
						step = 1;
					}else {
						System.out.println("No Agent can be found under that name");
						step = 99;
					}		
				break;
			case 1:
				System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out REQUEST (Request Infos) to VPP *******");
				
				ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
				msg.setOntology(ontology.getName());
				msg.setLanguage(codec.getName());
				msg.setConversationId(conversationID);	
				AID receiver = new AID();
				for(int j = 0; j < vppAgents.size(); j++){
					receiver = vppAgents.get(j);
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
				break;
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
//			private String agentName = "noNameSet";
		private byte[] infoSet = new byte[] {(byte)0x01};
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
//				ConsumingRest_TU getInstance = new ConsumingRest_TU();
//				infoSet = getInstance.getNodeRed(Addresses.URL_NODERED, PutVariable.REQUESTINFO);
			System.out.println(this.getAgent().getAID().getLocalName()+"******* Sending out new RequestInfo *******");
			try {
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.INFORM);
				ContentManager cm = myAgent.getContentManager();
				ContentElementList cel = new ContentElementList();
				RequestInfoInform newRequestInfoInform = new RequestInfoInform();
				newRequestInfoInform.setTuName(getAID().getLocalName());
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
		
		