package mas.JADE_VPP;

public class InterfacePayloadBalancing {
	private String balancingStart; 
	private String balancingEnd; 
	private String tuName;
	
	InterfacePayloadBalancing(String _balancingStart, String _balancingEnd, String _tuName){
		setBalancingStart(_balancingStart);
		setBalancingEnd(_balancingEnd);
		tuName = _tuName;
	}
	
	InterfacePayloadBalancing(String _tuName){
			tuName = _tuName;
	}
	
	public String getTuName() {
		return tuName;
	}
	public void setTuName(String tuName) {
		this.tuName = tuName;
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
