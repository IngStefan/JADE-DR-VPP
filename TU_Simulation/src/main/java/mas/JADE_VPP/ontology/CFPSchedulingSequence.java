package mas.JADE_VPP.ontology;

import jade.content.AgentAction;


public class CFPSchedulingSequence implements AgentAction {
	private static final long serialVersionUID = 1L;
	
	private String timeBegin;		//format = "2020-02-18 05:00:00.0"; can be parsed with "DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");" and "LocalDateTime timeBegin = LocalDateTime.parse(timeBegin,formatter);"
	private String timeEnd;


	public String getTimeBegin() {
		return timeBegin;
	}

	public void setTimeBegin(String timeBegin) {
		this.timeBegin = timeBegin;
	}

	public String getTimeEnd() {
		return timeEnd;
	}

	public void setTimeEnd(String timeEnd) {
		this.timeEnd = timeEnd;
	}
	
	
}
