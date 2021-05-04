package mas.JADE_VPP;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterfacePayloadActivation {

	private int feedIn; 				//Einspeisung
	private int operatingPoint; 		//Arbeitspunkt
	private int leadingOperatingPoint;	//vorauseilender Arbeitspunkt
	private int currentValueFR;			//Regelleistungsistwert
	private int assignedPool;			//Poolzuordnung
	private int status;					//Status (Meldung)
	private int frequency;				//Frequenz
	private int aFRRsetpoint;			//aFRR-Soll (ÜNB -> POOL)
	private int aFRRsetpointEcho;		//aFRR-Soll-Echo (Pool -> ÜNB)
	private int setpointFR;				//Regelleistungs-Soll
	private int aFRRGradientPOS;		//aFRR-Gradient POS
	private int aFRRGradientNEG;		//aFRR-Gradient NEG
	private int capacityPOS;			//Arbeitsvermögen POS (bei begrenztem Energiespeicher)
	private int capacityNEG;			//Arbeitsvermögen NEG (bei begrenzten Energiespeicher)
	private int holdingCapacityPOS;		//Aktuelle Vorhalteleistung POS
	private int holdingCapacityNEG;		//Aktuelle Vorhalteleistung NEG
	private int controlBandPOS;			//Regelband POS
	private int controlBandNEG;			//Regelband NEG
	
	
	public InterfacePayloadActivation(){
		setFeedIn(0);			
		setOperatingPoint(0);	 		
		setLeadingOperatingPoint(0);		
		setCurrentValueFR(0);				
		setAssignedPool(0);			
		setStatus(0);						
		setFrequency(0);					
		setaFRRsetpoint(0);				
		setaFRRsetpointEcho(0);			
		setSetpointFR(0);					
		setaFRRGradientPOS(0);			
		setaFRRGradientNEG(0);		
		setCapacityPOS(0);				
		setCapacityNEG(0);				
		setHoldingCapacityPOS(0);		
		setHoldingCapacityNEG(0);			
		setControlBandPOS(0);			
		setControlBandNEG(0);	
	}
	
	public InterfacePayloadActivation(
			int _feedIn, int _operatingPoint, int _leadingOperatingPoint, int _currentValueFR, int _assignedPool, int _status,
			 int _frequency, int _aFRRsetpoint, int _aFRRsetpointEcho, int _setpointFR, int _aFRRGradientPOS, int _aFRRGradientNEG, 
			 int _capacityPOS, int _capacityNEG, int _holdingCapacityPOS, int _holdingCapacityNEG, int _controlBandPOS, int _controlBandNEG){
		feedIn = _feedIn;
		operatingPoint = _operatingPoint;
		leadingOperatingPoint = _leadingOperatingPoint;
		currentValueFR = _currentValueFR;
		assignedPool = _assignedPool;
		status = _status;
		frequency = _frequency;
		aFRRsetpoint = _aFRRsetpoint;
		aFRRsetpointEcho = _aFRRsetpointEcho;
		setpointFR = _setpointFR;
		aFRRGradientPOS = _aFRRGradientPOS;
		aFRRGradientNEG = _aFRRGradientNEG;
		capacityPOS = _capacityPOS;
		capacityNEG = _capacityNEG;
		holdingCapacityPOS = _holdingCapacityPOS;
		holdingCapacityNEG = _holdingCapacityNEG;
		controlBandPOS = _controlBandPOS;
		controlBandNEG = _controlBandNEG;
	}

	public int getFeedIn() {
		return feedIn;
	}

	public void setFeedIn(int feedIn) {
		this.feedIn = feedIn;
	}

	public int getOperatingPoint() {
		return operatingPoint;
	}

	public void setOperatingPoint(int operatingPoint) {
		this.operatingPoint = operatingPoint;
	}

	public int getLeadingOperatingPoint() {
		return leadingOperatingPoint;
	}

	public void setLeadingOperatingPoint(int leadingOperatingPoint) {
		this.leadingOperatingPoint = leadingOperatingPoint;
	}

	public int getCurrentValueFR() {
		return currentValueFR;
	}

	public void setCurrentValueFR(int currentValueFR) {
		this.currentValueFR = currentValueFR;
	}

	public int getAssignedPool() {
		return assignedPool;
	}

	public void setAssignedPool(int assignedPool) {
		this.assignedPool = assignedPool;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int getaFRRsetpoint() {
		return aFRRsetpoint;
	}

	public void setaFRRsetpoint(int aFRRsetpoint) {
		this.aFRRsetpoint = aFRRsetpoint;
	}

	public int getaFRRsetpointEcho() {
		return aFRRsetpointEcho;
	}

	public void setaFRRsetpointEcho(int aFRRsetpointEcho) {
		this.aFRRsetpointEcho = aFRRsetpointEcho;
	}

	public int getSetpointFR() {
		return setpointFR;
	}

	public void setSetpointFR(int setpointFR) {
		this.setpointFR = setpointFR;
	}

	public int getaFRRGradientPOS() {
		return aFRRGradientPOS;
	}

	public void setaFRRGradientPOS(int aFRRGradientPOS) {
		this.aFRRGradientPOS = aFRRGradientPOS;
	}

	public int getaFRRGradientNEG() {
		return aFRRGradientNEG;
	}

	public void setaFRRGradientNEG(int aFRRGradientNEG) {
		this.aFRRGradientNEG = aFRRGradientNEG;
	}

	public int getCapacityPOS() {
		return capacityPOS;
	}

	public void setCapacityPOS(int capacityPOS) {
		this.capacityPOS = capacityPOS;
	}

	public int getCapacityNEG() {
		return capacityNEG;
	}

	public void setCapacityNEG(int capacityNEG) {
		this.capacityNEG = capacityNEG;
	}

	public int getHoldingCapacityPOS() {
		return holdingCapacityPOS;
	}

	public void setHoldingCapacityPOS(int holdingCapacityPOS) {
		this.holdingCapacityPOS = holdingCapacityPOS;
	}

	public int getHoldingCapacityNEG() {
		return holdingCapacityNEG;
	}

	public void setHoldingCapacityNEG(int holdingCapacityNEG) {
		this.holdingCapacityNEG = holdingCapacityNEG;
	}

	public int getControlBandPOS() {
		return controlBandPOS;
	}

	public void setControlBandPOS(int controlBandPOS) {
		this.controlBandPOS = controlBandPOS;
	}

	public int getControlBandNEG() {
		return controlBandNEG;
	}

	public void setControlBandNEG(int controlBandNEG) {
		this.controlBandNEG = controlBandNEG;
	}
	
	

	
//	@Override
//    public String toString() {
//        return "Payload{" +
//                "energy=" + energy + "  " + 
//                "status=" + status + "  " + 
//                "operatingPoint=" + operatingPoint + "  " + 
//                "fullfillmentPrice=" + fullfillmentPrice + "  " + 
//                '\'' +
//                '}';
//    }


	
	
}
