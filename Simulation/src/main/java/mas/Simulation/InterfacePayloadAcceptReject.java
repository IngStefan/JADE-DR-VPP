package mas.Simulation;

public class InterfacePayloadAcceptReject {


	private String schedulingStart;
	private String schedulingEnd;
	private String schedulingPlanReference;
	
	public InterfacePayloadAcceptReject(){
		setSchedulingStart("2022-02-22 00:00:00.0");
		setSchedulingEnd("2022-02-22 00:00:00.0");
	}

	//for requesting the LoadSchedulePlans
	public InterfacePayloadAcceptReject(String _schedulingStart, String _schedulingEnd, String _schedulingPlanReference){
		schedulingStart = _schedulingStart;
		schedulingEnd = _schedulingEnd;
		schedulingPlanReference = _schedulingPlanReference;

	}
	
	//for answering the ERP with the results of the VPP
	public InterfacePayloadAcceptReject(String _schedulingPlanReference){
		setSchedulingStart("2022-02-22 00:00:00.0");
		setSchedulingEnd("2022-02-22 00:00:00.0");
		schedulingPlanReference = _schedulingPlanReference;
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

	
	public String getSchedulingPlanReference() {
		return schedulingPlanReference;
	}

	public void setSchedulingPlanReference(String schedulingPlanReference) {
		this.schedulingPlanReference = schedulingPlanReference;
	}
	
}
