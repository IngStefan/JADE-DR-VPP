package mas.JADE_VPP;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterfacePayloadVPP {

	private String referenceID;
	private String serviceDescriptions;
	private String schedulingStart;
	private String schedulingEnd;
	private String expiration;
	private String schedulingPlanReference;
	private String agentName;
	private String tuName;
	private int newSetpoint;
	private String balancingStart;
	private String balancingEnd;
	private int balancingUpdateRate;

	

	
	
	public InterfacePayloadVPP(){
		setReferenceID("0");
		setSchedulingStart("1990-02-22 05:00:00.0");
		setSchedulingEnd("1990-02-22 07:00:00.0");
		setExpiration("1990-02-22 07:00:00.0");
		setServiceDescriptions("empty");
		setSchedulingPlanReference("empty");
		setAgentName("noName");
		setTuName("noName");
		setNewSetpoint(0);
	}
	
	public InterfacePayloadVPP( String _referenceID, String _serviceDescriptions, String _schedulingStart, String _schedulingEnd, 
			String _expiration){
		referenceID = _referenceID;
		schedulingStart = _schedulingStart;
		schedulingEnd = _schedulingEnd;
		expiration = _expiration;
		serviceDescriptions = _serviceDescriptions;
	}
	
	public InterfacePayloadVPP( String _schedulingPlanReference, String _schedulingStart, String _schedulingEnd){
		schedulingPlanReference = _schedulingPlanReference;
		schedulingStart = _schedulingStart;
		schedulingEnd = _schedulingEnd;
	}
	
	public InterfacePayloadVPP( String _agentName, String _tuName, int _newSetpoint){
		agentName = _agentName;
		tuName = _tuName;
		newSetpoint = _newSetpoint;
	}
	
	public InterfacePayloadVPP( String _schedulingPlanReference){
		schedulingPlanReference = _schedulingPlanReference;
	}
	
	//balancing
	public InterfacePayloadVPP(String _tuName, String _balancingStart, String  _balancingEnd, int _balancingUpdateRate, String _referenceID){
		tuName = _tuName;
		setBalancingStart(_balancingStart);
		setBalancingEnd(_balancingEnd);
		setBalancingUpdateRate(_balancingUpdateRate);
		referenceID = _referenceID;
	}
	
	
	@Override
    public String toString() {
        return "Payload{" +
                "referenceID" + referenceID + "  " + 
                "schedulingStart=" + schedulingStart + "  " + 
                "schedulingEnd=" + schedulingEnd + "  " + 
                "serviceDescriptions=" + serviceDescriptions + "  " + 
                "offerExpiration=" + expiration + " " +
                '\'' +
                '}';
    }



	public String getSchedulingStart() {
		return schedulingStart;
	}

	public void setSchedulingStart(String schedulingStart) {
		this.schedulingStart = schedulingStart;
	}

	public String getSchedulingEnd() {
		return schedulingEnd;
	}

	public void setSchedulingEnd(String schedulingEnd) {
		this.schedulingEnd = schedulingEnd;
	}

	public String getExpiration() {
		return expiration;
	}

	public void setExpiration(String expiration) {
		this.expiration = expiration;
	}

	public String getReferenceID() {
		return referenceID;
	}

	public void setReferenceID(String referenceID) {
		this.referenceID = referenceID;
	}

	public String getServiceDescriptions() {
		return serviceDescriptions;
	}

	public void setServiceDescriptions(String serviceDescriptions) {
		this.serviceDescriptions = serviceDescriptions;
	}

	public String getSchedulingPlanReference() {
		return schedulingPlanReference;
	}

	public void setSchedulingPlanReference(String schedulingPlanReference) {
		this.schedulingPlanReference = schedulingPlanReference;
	}

	public int getNewSetpoint() {
		return newSetpoint;
	}

	public void setNewSetpoint(int newSetpoint) {
		this.newSetpoint = newSetpoint;
	}

	public String getAgentName() {
		return agentName;
	}

	public void setAgentName(String agentName) {
		this.agentName = agentName;
	}

	public String getTuName() {
		return tuName;
	}

	public void setTuName(String tuName) {
		this.tuName = tuName;
	}


	public String getBalancingStart() {
		return balancingStart;
	}


	public void setBalancingStart(String balancingStart) {
		this.balancingStart = balancingStart;
	}


	public String getBalancingEnd() {
		return balancingEnd;
	}


	public void setBalancingEnd(String balancingEnd) {
		this.balancingEnd = balancingEnd;
	}


	public int getBalancingUpdateRate() {
		return balancingUpdateRate;
	}


	public void setBalancingUpdateRate(int balancingUpdateRate) {
		this.balancingUpdateRate = balancingUpdateRate;
	}




	
	
}