//Class associated to the RequestNewSetpoint schema
package mas.JADE_VPP.ontology;

import jade.content.AgentAction;



public class TUControlSequenceRequestSetpoint implements AgentAction {
	private static final long serialVersionUID = 1L;
	
	private Integer newSetpoint;
	private String tuName;


	public Integer getNewSetpoint() {
		return newSetpoint;
	}

	public void setNewSetpoint(Integer newSetpoint) {
		this.newSetpoint = newSetpoint;
	}

	public String getTuName() {
		return tuName;
	}

	public void setTuName(String tuName) {
		this.tuName = tuName;
	}

}
