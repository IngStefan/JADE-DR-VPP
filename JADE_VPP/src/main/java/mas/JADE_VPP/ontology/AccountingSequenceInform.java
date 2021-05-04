package mas.JADE_VPP.ontology;

import jade.content.Predicate;

public class AccountingSequenceInform implements Predicate {
	private static final long serialVersionUID = 1L;
	
	private String agentName;
	private String tuName;
	private byte[] energyConsumptionProfile;
	
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
	public byte[] getEnergyConsumptionProfile() {
		return energyConsumptionProfile;
	}
	public void setEnergyConsumptionProfile(byte[] energyConsumptionProfile) {
		this.energyConsumptionProfile = energyConsumptionProfile;
	}
	
}
