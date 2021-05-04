package mas.JADE_VPP.ontology;

import jade.content.AgentAction;

public class TUControlSequenceRequestLoadProfile implements AgentAction {
	private static final long serialVersionUID = 1L;
	
	private byte[] newLoadProfile;
	private String tuName;

	public String getTuName() {
		return tuName;
	}

	public void setTuName(String tuName) {
		this.tuName = tuName;
	}

	public byte[] getNewLoadProfile() {
		return newLoadProfile;
	}

	public void setNewLoadProfile(byte[] newLoadProfile) {
		this.newLoadProfile = newLoadProfile;
	}
	
}
