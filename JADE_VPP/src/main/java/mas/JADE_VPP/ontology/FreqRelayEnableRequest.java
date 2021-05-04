package mas.JADE_VPP.ontology;

import jade.content.AgentAction;

public class FreqRelayEnableRequest implements AgentAction {
	private static final long serialVersionUID = 1L;
	
	private String tuName;

	public String getTuName() {
		return tuName;
	}

	public void setTuName(String tuName) {
		this.tuName = tuName;
	}
	
}
