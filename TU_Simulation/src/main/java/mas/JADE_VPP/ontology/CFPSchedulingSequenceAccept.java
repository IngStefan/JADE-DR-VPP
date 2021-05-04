package mas.JADE_VPP.ontology;

import jade.content.Predicate;


public class CFPSchedulingSequenceAccept implements Predicate {
	private static final long serialVersionUID = 1L;

	private String schedulingStart;
	private String schedulingEnd;
	
	public String getSchedulingStart() {
		return schedulingStart;
	}
	public void setSchedulingStart(String schedulingStart) {
		this.schedulingStart = schedulingStart;
	}
	public String getSchedulingEnd() {
		return schedulingEnd;
	}
	public void setSchedulingEnd(String schedulingEnd) {
		this.schedulingEnd = schedulingEnd;
	}
		
		
}
