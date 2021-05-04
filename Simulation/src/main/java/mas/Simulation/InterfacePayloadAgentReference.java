package mas.Simulation;

public class InterfacePayloadAgentReference {
	private String agentName;
	private String tuName;
	private String referenceID;
	
	InterfacePayloadAgentReference(String _tuName){
		tuName = _tuName;
		agentName = "noNameSet";
		referenceID = "noIDSet";
}
	
	
	InterfacePayloadAgentReference(String _agentName, String _tuName){
		tuName = _tuName;
		agentName = _agentName;
		referenceID = "noIDSet";
}
	
	InterfacePayloadAgentReference(String _referenceID, String _agentName, String _tuName){
		tuName = _tuName;
		agentName = _agentName;
		referenceID = _referenceID;
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



	public String getReferenceID() {
		return referenceID;
	}



	public void setReferenceID(String referenceID) {
		this.referenceID = referenceID;
	}
	
}
