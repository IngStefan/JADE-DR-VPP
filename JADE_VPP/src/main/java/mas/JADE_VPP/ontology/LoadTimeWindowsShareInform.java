package mas.JADE_VPP.ontology;

import jade.content.Predicate;

public class LoadTimeWindowsShareInform implements Predicate{
	private static final long serialVersionUID = 1L;
	
	private String loadTimeWindowsReference;
	private String windowHighBegin;
	private String windowHighEnd;
	private String windowLowBegin;
	private String windowLowEnd;

	
	public String getWindowHighBegin() {
		return windowHighBegin;
	}
	public void setWindowHighBegin(String windowHighBegin) {
		this.windowHighBegin = windowHighBegin;
	}
	public String getWindowHighEnd() {
		return windowHighEnd;
	}
	public void setWindowHighEnd(String windowHighEnd) {
		this.windowHighEnd = windowHighEnd;
	}
	public String getWindowLowBegin() {
		return windowLowBegin;
	}
	public void setWindowLowBegin(String windowLowBegin) {
		this.windowLowBegin = windowLowBegin;
	}
	public String getWindowLowEnd() {
		return windowLowEnd;
	}
	public void setWindowLowEnd(String windowLowEnd) {
		this.windowLowEnd = windowLowEnd;
	}
	public String getLoadTimeWindowsReference() {
		return loadTimeWindowsReference;
	}
	public void setLoadTimeWindowsReference(String loadTimeWindowsReference) {
		this.loadTimeWindowsReference = loadTimeWindowsReference;
	}
	

	
}
