package mas.JADE_VPP.ontology;

import jade.content.AgentAction;

public class LoadTimeWindowsShareRequest implements AgentAction  {
	private static final long serialVersionUID = 1L;
	
	private String loadTimeWindowsReference;

	public String getLoadTimeWindowsReference() {
		return loadTimeWindowsReference;
	}

	public void setLoadTimeWindowsReference(String loadTimeWindowsReference) {
		this.loadTimeWindowsReference = loadTimeWindowsReference;
	}

	
}
