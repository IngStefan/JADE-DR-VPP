package mas.JADE_VPP.ontology;

import jade.content.Predicate;

public class CancelOperationConfirm implements Predicate {
	private static final long serialVersionUID = 1L;
	private String tuName;
	private String operationReference;
	
	public String getTuName() {
		return tuName;
	}
	public void setTuName(String tuName) {
		this.tuName = tuName;
	}
	public String getOperationReference() {
		return operationReference;
	}
	public void setOperationReference(String operationReference) {
		this.operationReference = operationReference;
	}
}
