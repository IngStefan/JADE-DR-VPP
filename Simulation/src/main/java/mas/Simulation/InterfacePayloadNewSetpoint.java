package mas.Simulation;

public class InterfacePayloadNewSetpoint {
	private int newSetpoint; 
	private String tuName;
	
	InterfacePayloadNewSetpoint(int _newSetpoint, String _tuName){
		newSetpoint = _newSetpoint;
		tuName = _tuName;
	}
	
	InterfacePayloadNewSetpoint(String _tuName){
			tuName = _tuName;
	}
	
	
	public int getNewSetpoint() {
		return newSetpoint;
	}
	public void setNewSetpoint(int newSetpoint) {
		this.newSetpoint = newSetpoint;
	}
	public String getTuName() {
		return tuName;
	}
	public void setTuName(String tuName) {
		this.tuName = tuName;
	}
	
	
	
	
}
