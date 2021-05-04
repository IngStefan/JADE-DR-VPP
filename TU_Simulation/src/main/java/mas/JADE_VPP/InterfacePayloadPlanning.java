package mas.JADE_VPP;

public class InterfacePayloadPlanning {


	private String schedulingStart;
	private String schedulingEnd;
	private String expiration;
	private String referenceID;

	public InterfacePayloadPlanning(){
		setSchedulingStart("0000-00-00 00:00:00.0");
		setSchedulingEnd("0000-00-00 00:00:00.0");
		setExpiration("0000-00-00 00:00:00.0");
		setReferenceID("empty");
	}

	//for requesting the LoadSchedulePlans
	public InterfacePayloadPlanning(String _schedulingStart, String _schedulingEnd, String _expiration, String _referenceID){
		schedulingStart = _schedulingStart;
		schedulingEnd = _schedulingEnd;
		expiration = _expiration;
		referenceID = _referenceID;
	}
	
	//for answering the ERP with the results of the VPP
	public InterfacePayloadPlanning(String _schedulingStart, String _schedulingEnd, String _referenceID){
		schedulingStart = _schedulingStart;
		schedulingEnd = _schedulingEnd;
		expiration = "empty";
		referenceID = _referenceID;
	}
	
	//for answering the ERP with the results of the VPP
	public InterfacePayloadPlanning(String _referenceID){
		schedulingStart = "empty";
		schedulingEnd = "empty";
		expiration = "empty";
		referenceID = _referenceID;
	}
	
	
	
	@Override
    public String toString() {
        return "Payload{" +
                "schedulingStart=" + schedulingStart + "  " + 
                "schedulingEnd=" + schedulingEnd + "  " + 
                "expiration=" + expiration + "  " + 
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
	
}
