package mas.JADE_VPP.ontology;

import jade.content.AgentAction;

public class RequestInfoRequest implements AgentAction {
		private static final long serialVersionUID = 1L;
		private String tuName;
		private String agentName;
		
		public String getTuName() {
			return tuName;
		}

		public void setTuName(String tuName) {
			this.tuName = tuName;
		}

		public String getAgentName() {
			return agentName;
		}

		public void setAgentName(String agentName) {
			this.agentName = agentName;
		}
}
	
	
	
