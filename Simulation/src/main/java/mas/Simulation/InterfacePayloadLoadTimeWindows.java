package mas.Simulation;

public class InterfacePayloadLoadTimeWindows {
	
	private String loadTimeWindowsReference;
	private String windowHighBegin;
	private String windowHighEnd;
	private String windowLowBegin;
	private String windowLowEnd;

	InterfacePayloadLoadTimeWindows(String _loadTimeWindowsReference){
		loadTimeWindowsReference = _loadTimeWindowsReference;
		windowHighBegin = "noTimeWindow";
		windowHighEnd = "noTimeWindow";
		windowLowBegin = "noTimeWindow";
		windowLowEnd = "noTimeWindow";
	}
	
	InterfacePayloadLoadTimeWindows(String _loadTimeWindowsReference, String _windowHighBegin, String _windowHighEnd, String _windowLowBegin, String _windowLowEnd){
		loadTimeWindowsReference = _loadTimeWindowsReference;
		windowHighBegin = _windowHighBegin;
		windowHighEnd = _windowHighEnd;
		windowLowBegin = _windowLowBegin;
		windowLowEnd = _windowLowEnd;
	}
	
	
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
