package mas.JADE_VPP.ontology;

import jade.content.AgentAction;

public class BalancingSequenceSubscribe implements AgentAction {
	private static final long serialVersionUID = 1L;
	private String balancingStart;
	private String balancingEnd;
	private String tuName;
	private int updateRate = 1000;				//update rate in milliseconds (standard = 1 millisecond)
	
	public String getTuName() {
		return tuName;
	}
	public void setTuName(String tuName) {
		this.tuName = tuName;
	}
	public int getUpdateRate() {
		return updateRate;
	}
	public void setUpdateRate(int updateRate) {
		this.updateRate = updateRate;
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
	
}
