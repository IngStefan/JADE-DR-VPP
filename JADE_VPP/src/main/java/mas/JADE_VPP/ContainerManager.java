package mas.JADE_VPP;

//ContainerManager.java : manages the startup of the JADE agent platform (additional descriptions in respective JADE documentation)

import jade.core.Profile;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class ContainerManager {
	
	private static ContainerManager instance = null;
	private ContainerController containerController; //wraps high lvl function of the agent container (installing MTP, killing container,...)
	
	private ContainerManager(){		
		init();
	}
	
    public static ContainerManager getInstance(){
        if (null == instance){
            instance = new ContainerManager();
        }
        return instance;
    }
	
    //initializes the agent container with needed settings
	private void init(){
		//Get JADE runtime interface
		jade.core.Runtime runtime = jade.core.Runtime.instance(); 
		//runtime.setCloseVM(true);
		
		////***** for MAIN CONTAINER (VPP) ************
		//Creates a profile for the start of the Main Container (for VPP Agent)
		Profile p = new jade.core.ProfileImpl();
		p.setParameter(jade.core.Profile.CONTAINER_NAME, "VPP");
		p.setParameter(jade.core.Profile.MAIN_HOST, "localhost");
		p.setParameter(jade.core.Profile.GUI, "true");		//starts the JADE user interface
		containerController = runtime.createMainContainer(p);	
		
				
		////***** for Agent CONTAINER (TU) ************
		//Alternative profile to join a Main Container (for TU-Agents)
//		Profile p = new jade.core.ProfileImpl();
//		p.setParameter(jade.core.Profile.CONTAINER_NAME, "TU_Company3");
//		p.setParameter(jade.core.Profile.MAIN_HOST, "192.168.152.233");	// IP of the Main Host the TUs should connect to
//		containerController = runtime.createAgentContainer(p);	
		
	}
	
    public AgentController instantiateAgent(String name, String className) throws StaleProxyException{
    	AgentController agentController = containerController.createNewAgent(name,className, null);
        agentController.start();
        return agentController;
    }
    
    public AgentController instantiateAgent(String name, String className, Object[] agentArgs) throws StaleProxyException{
        AgentController agentController = containerController.createNewAgent(name,className, agentArgs);
        agentController.start();
        return agentController;
    }
}
