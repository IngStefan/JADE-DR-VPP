package mas.Simulation;

public class InterfacePayloadBalancing {
	private String balancingStart; 
	private String balancingEnd; 
	private String balancingTUName;
	private String balancingReferenceID;
	private int balancingUpdateRate = 1000;
	
	InterfacePayloadBalancing(String _balancingStart, String _balancingEnd, String _balancingTUName, String _balancingReferenceID){
		setBalancingStart(_balancingStart);
		setBalancingEnd(_balancingEnd);
		setBalancingTUName(_balancingTUName);
		setBalancingUpdateRate(1000);
		setBalancingReferenceID(_balancingReferenceID);
	}
	
	
	InterfacePayloadBalancing(String _balancingStart, String _balancingEnd, String _balancingTUName, String _balancingReferenceID, int updateRate){
		setBalancingStart(_balancingStart);
		setBalancingEnd(_balancingEnd);
		setBalancingTUName(_balancingTUName);
		setBalancingUpdateRate(updateRate);
		setBalancingReferenceID(_balancingReferenceID);
	}
	
	InterfacePayloadBalancing(String _tuName){
		setBalancingTUName(_tuName);
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

	public String getBalancingReferenceID() {
		return balancingReferenceID;
	}

	public void setBalancingReferenceID(String balancingReferenceID) {
		this.balancingReferenceID = balancingReferenceID;
	}

	public int getBalancingUpdateRate() {
		return balancingUpdateRate;
	}

	public void setBalancingUpdateRate(int balancingUpdateRate) {
		this.balancingUpdateRate = balancingUpdateRate;
	}

	public String getBalancingTUName() {
		return balancingTUName;
	}

	public void setBalancingTUName(String balancingTUName) {
		this.balancingTUName = balancingTUName;
	}
}
