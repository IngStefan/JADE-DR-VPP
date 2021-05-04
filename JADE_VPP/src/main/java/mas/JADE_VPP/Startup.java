package mas.JADE_VPP;

//******* Startup.java: starts the agent platform and other services for this application 

//SPRING BOOT AND AUTOBUILDER FOR JADE AGENT
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class Startup {
 	
		public static void main(String[] args) 
		{
			//Spring Application builder, to start the Application with the wanted Arguments
			//instead of: SpringApplication.run(Startup.class, args);
			//use the following, so the headless mode is false and the GUI for the Agents System is also started
			SpringApplicationBuilder builder = new SpringApplicationBuilder(Startup.class);
			builder.headless(false);
			@SuppressWarnings("unused")
			ConfigurableApplicationContext context = builder.run(args);
			
			//factor to increase the number of TU agents for testing the platform
//			int factor = 1;		
			
			//******* start the platform (changes to the container can be made in the "ContainerManager" class)
			try
			{
				//******* starting the VPP agent of the platform (1st parameter: AgentName, 2nd parameter: class name)
				ContainerManager.getInstance().instantiateAgent("VPP_Aggregator_1", "mas.JADE_VPP.VPP");
				//******* starting an agent with additional arguments as 3rd parameter
				//Object[] agentArgs = {500, 511, 399, 555, 1500, 1600, 200, 900, 65, 1233, 426, 468, 934};  //
				//ContainerManager.getInstance().instantiateAgent("VPP2", "mas.JADE_VPP.VPP", agentArgs);
				
//				
				//******* starting TU Agents (1st parameter: AgentName, 2nd parameter: class name, 3rd parameter: service description in comma separation) 
				/*
				ContainerManager.getInstance().instantiateAgent("TU_Company1_PRL", "mas.JADE_VPP.TU", new String[]{"PRL"});	
				ContainerManager.getInstance().instantiateAgent("TU_Company2_PRL", "mas.JADE_VPP.TU", new String[]{"PRL"});	
				ContainerManager.getInstance().instantiateAgent("TU_Company3_PRL", "mas.JADE_VPP.TU", new String[]{"PRL"});	
				ContainerManager.getInstance().instantiateAgent("TU_Company1_SRL", "mas.JADE_VPP.TU", new String[]{"SRL"});	
				ContainerManager.getInstance().instantiateAgent("TU_Company2_SRL", "mas.JADE_VPP.TU", new String[]{"SRL"});	
				ContainerManager.getInstance().instantiateAgent("TU_Company3_SRL", "mas.JADE_VPP.TU", new String[]{"SRL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company2_SRL(BACKUP)", "mas.JADE_VPP.TU", new String[]{"SRL"});	
				ContainerManager.getInstance().instantiateAgent("TU_Company3_SRL(BACKUP)", "mas.JADE_VPP.TU", new String[]{"SRL"});	
				ContainerManager.getInstance().instantiateAgent("TU_Company4_SRL", "mas.JADE_VPP.TU", new String[]{"SRL,FLEX"});	
				ContainerManager.getInstance().instantiateAgent("TU_Company5_SRL", "mas.JADE_VPP.TU", new String[]{"SRL,FLEX"});	
				ContainerManager.getInstance().instantiateAgent("TU_Company1_MRL", "mas.JADE_VPP.TU", new String[]{"MRL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company2_MRL", "mas.JADE_VPP.TU", new String[]{"MRL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company3_MRL", "mas.JADE_VPP.TU", new String[]{"MRL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company4_MRL", "mas.JADE_VPP.TU", new String[]{"MRL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company5_MRL_A", "mas.JADE_VPP.TU", new String[]{"MRL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company5_MRL_B", "mas.JADE_VPP.TU", new String[]{"MRL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company5_MRL_C", "mas.JADE_VPP.TU", new String[]{"MRL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company6_SOL", "mas.JADE_VPP.TU", new String[]{"SOL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company7_SOL", "mas.JADE_VPP.TU", new String[]{"SOL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company8_SOL", "mas.JADE_VPP.TU", new String[]{"SOL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company9_SOL", "mas.JADE_VPP.TU", new String[]{"SOL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company10_SNL", "mas.JADE_VPP.TU", new String[]{"SNL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company11_SNL", "mas.JADE_VPP.TU", new String[]{"SNL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company12_SNL", "mas.JADE_VPP.TU", new String[]{"SNL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company13_SNL", "mas.JADE_VPP.TU", new String[]{"SNL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company14_SNL", "mas.JADE_VPP.TU", new String[]{"SNL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company15_SNL", "mas.JADE_VPP.TU", new String[]{"SNL"});
				ContainerManager.getInstance().instantiateAgent("TU_Company16_FLEX_pla", "mas.JADE_VPP.TU", new String[]{"FLEX_pla"});
				ContainerManager.getInstance().instantiateAgent("TU_Company16_FLEX_act", "mas.JADE_VPP.TU", new String[]{"FLEX_act"});
				ContainerManager.getInstance().instantiateAgent("TU_Company16_FLEX_acc", "mas.JADE_VPP.TU", new String[]{"FLEX_acc"});
				ContainerManager.getInstance().instantiateAgent("TU_Company17_FLEX_pla", "mas.JADE_VPP.TU", new String[]{"FLEX_pla"});
				ContainerManager.getInstance().instantiateAgent("TU_Company17_FLEX_act", "mas.JADE_VPP.TU", new String[]{"FLEX_act"});
				ContainerManager.getInstance().instantiateAgent("TU_Company17_FLEX_acc", "mas.JADE_VPP.TU", new String[]{"FLEX_acc"});
				ContainerManager.getInstance().instantiateAgent("TU_Company18_FLEX_pla", "mas.JADE_VPP.TU", new String[]{"FLEX_pla"});
				ContainerManager.getInstance().instantiateAgent("TU_Company18_FLEX_act", "mas.JADE_VPP.TU", new String[]{"FLEX_act"});
				ContainerManager.getInstance().instantiateAgent("TU_Company18_FLEX_acc", "mas.JADE_VPP.TU", new String[]{"FLEX_acc"});
				ContainerManager.getInstance().instantiateAgent("TU_Company19_FLEX_pla", "mas.JADE_VPP.TU", new String[]{"FLEX_pla"});
				ContainerManager.getInstance().instantiateAgent("TU_Company19_FLEX_act", "mas.JADE_VPP.TU", new String[]{"FLEX_act"});
				ContainerManager.getInstance().instantiateAgent("TU_Company19_FLEX_acc", "mas.JADE_VPP.TU", new String[]{"FLEX_acc"});
				ContainerManager.getInstance().instantiateAgent("TU_Company20_FLEX_pla", "mas.JADE_VPP.TU", new String[]{"FLEX_pla"});
				ContainerManager.getInstance().instantiateAgent("TU_Company20_FLEX_act", "mas.JADE_VPP.TU", new String[]{"FLEX_act"});
				ContainerManager.getInstance().instantiateAgent("TU_Company20_FLEX_acc", "mas.JADE_VPP.TU", new String[]{"FLEX_acc"});
				ContainerManager.getInstance().instantiateAgent("TU_Company21_FLEX", "mas.JADE_VPP.TU", new String[]{"FLEX"});
				ContainerManager.getInstance().instantiateAgent("TU_Company22_FLEX", "mas.JADE_VPP.TU", new String[]{"FLEX"});
				ContainerManager.getInstance().instantiateAgent("TU_Company23_FLEX", "mas.JADE_VPP.TU", new String[]{"FLEX"});
				ContainerManager.getInstance().instantiateAgent("TU_Company24_FLEX", "mas.JADE_VPP.TU", new String[]{"FLEX"});
				ContainerManager.getInstance().instantiateAgent("TU_Company25_FLEX", "mas.JADE_VPP.TU", new String[]{"FLEX"});
				ContainerManager.getInstance().instantiateAgent("TU_Company26_FLEX", "mas.JADE_VPP.TU", new String[]{"FLEX"});
				ContainerManager.getInstance().instantiateAgent("TU_Company27_FLEX", "mas.JADE_VPP.TU", new String[]{"FLEX"});
				ContainerManager.getInstance().instantiateAgent("TU_Company28_FLEX", "mas.JADE_VPP.TU", new String[]{"ANN"});
				ContainerManager.getInstance().instantiateAgent("TU_Company29_FLEX", "mas.JADE_VPP.TU", new String[]{"ANN"});
				ContainerManager.getInstance().instantiateAgent("TU_Company30_FLEX", "mas.JADE_VPP.TU", new String[]{"ANN"});
				
				//**** Adding additional Agents with a multiplier
				for(int i = 0 ; i < factor ; i++){
					ContainerManager.getInstance().instantiateAgent("TU_Company1_PRL+"+i, "mas.JADE_VPP.TU", new String[]{"PRL"});	
					ContainerManager.getInstance().instantiateAgent("TU_Company2_PRL+"+i, "mas.JADE_VPP.TU", new String[]{"PRL"});	
					ContainerManager.getInstance().instantiateAgent("TU_Company3_PRL+"+i, "mas.JADE_VPP.TU", new String[]{"PRL"});	
					ContainerManager.getInstance().instantiateAgent("TU_Company1_SRL+"+i, "mas.JADE_VPP.TU", new String[]{"SRL"});	
					ContainerManager.getInstance().instantiateAgent("TU_Company2_SRL+"+i, "mas.JADE_VPP.TU", new String[]{"SRL"});	
					ContainerManager.getInstance().instantiateAgent("TU_Company3_SRL+"+i, "mas.JADE_VPP.TU", new String[]{"SRL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company2_SRL(BACKUP)+"+i, "mas.JADE_VPP.TU", new String[]{"SRL"});	
					ContainerManager.getInstance().instantiateAgent("TU_Company3_SRL(BACKUP)+"+i, "mas.JADE_VPP.TU", new String[]{"SRL"});	
					ContainerManager.getInstance().instantiateAgent("TU_Company4_SRL,FLEX+"+i, "mas.JADE_VPP.TU", new String[]{"SRL,FLEX"});	
					ContainerManager.getInstance().instantiateAgent("TU_Company5_SRL,MRL+"+i, "mas.JADE_VPP.TU", new String[]{"SRL,MRL"});	
					ContainerManager.getInstance().instantiateAgent("TU_Company1_MRL+"+i, "mas.JADE_VPP.TU", new String[]{"MRL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company2_MRL+"+i, "mas.JADE_VPP.TU", new String[]{"MRL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company3_MRL+"+i, "mas.JADE_VPP.TU", new String[]{"MRL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company4_MRL+"+i, "mas.JADE_VPP.TU", new String[]{"MRL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company5_MRL_A+"+i, "mas.JADE_VPP.TU", new String[]{"MRL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company5_MRL_B+"+i, "mas.JADE_VPP.TU", new String[]{"MRL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company5_MRL_C+"+i, "mas.JADE_VPP.TU", new String[]{"MRL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company6_SOL+"+i, "mas.JADE_VPP.TU", new String[]{"SOL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company7_SOL+"+i, "mas.JADE_VPP.TU", new String[]{"SOL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company8_SOL+"+i, "mas.JADE_VPP.TU", new String[]{"SOL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company9_SOL+"+i, "mas.JADE_VPP.TU", new String[]{"SOL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company10_SNL+"+i, "mas.JADE_VPP.TU", new String[]{"SNL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company11_SNL+"+i, "mas.JADE_VPP.TU", new String[]{"SNL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company12_SNL+"+i, "mas.JADE_VPP.TU", new String[]{"SNL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company13_SNL+"+i, "mas.JADE_VPP.TU", new String[]{"SNL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company14_SNL+"+i, "mas.JADE_VPP.TU", new String[]{"SNL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company15_SNL+"+i, "mas.JADE_VPP.TU", new String[]{"SNL"});
					ContainerManager.getInstance().instantiateAgent("TU_Company16_FLEX_pla+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_pla"});
					ContainerManager.getInstance().instantiateAgent("TU_Company16_FLEX_act+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_act"});
					ContainerManager.getInstance().instantiateAgent("TU_Company16_FLEX_acc+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_acc"});
					ContainerManager.getInstance().instantiateAgent("TU_Company17_FLEX_pla+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_pla"});
					ContainerManager.getInstance().instantiateAgent("TU_Company17_FLEX_act+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_act"});
					ContainerManager.getInstance().instantiateAgent("TU_Company17_FLEX_acc+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_acc"});
					ContainerManager.getInstance().instantiateAgent("TU_Company18_FLEX_pla+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_pla"});
					ContainerManager.getInstance().instantiateAgent("TU_Company18_FLEX_act+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_act"});
					ContainerManager.getInstance().instantiateAgent("TU_Company18_FLEX_acc+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_acc"});
					ContainerManager.getInstance().instantiateAgent("TU_Company19_FLEX_pla+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_pla"});
					ContainerManager.getInstance().instantiateAgent("TU_Company19_FLEX_act+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_act"});
					ContainerManager.getInstance().instantiateAgent("TU_Company19_FLEX_acc+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_acc"});
					ContainerManager.getInstance().instantiateAgent("TU_Company20_FLEX_pla+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_pla"});
					ContainerManager.getInstance().instantiateAgent("TU_Company20_FLEX_act+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_act"});
					ContainerManager.getInstance().instantiateAgent("TU_Company20_FLEX_acc+"+i, "mas.JADE_VPP.TU", new String[]{"FLEX_acc"});
					ContainerManager.getInstance().instantiateAgent("TU_Company21_FLEX"+i, "mas.JADE_VPP.TU", new String[]{"FLEX"});
					ContainerManager.getInstance().instantiateAgent("TU_Company22_FLEX"+i, "mas.JADE_VPP.TU", new String[]{"FLEX"});
					ContainerManager.getInstance().instantiateAgent("TU_Company23_FLEX"+i, "mas.JADE_VPP.TU", new String[]{"FLEX"});
					ContainerManager.getInstance().instantiateAgent("TU_Company24_FLEX"+i, "mas.JADE_VPP.TU", new String[]{"FLEX"});
					ContainerManager.getInstance().instantiateAgent("TU_Company25_FLEX"+i, "mas.JADE_VPP.TU", new String[]{"FLEX"});
					ContainerManager.getInstance().instantiateAgent("TU_Company26_FLEX"+i, "mas.JADE_VPP.TU", new String[]{"FLEX"});
					ContainerManager.getInstance().instantiateAgent("TU_Company27_FLEX"+i, "mas.JADE_VPP.TU", new String[]{"FLEX"});
					ContainerManager.getInstance().instantiateAgent("TU_Company28_FLEX"+i, "mas.JADE_VPP.TU", new String[]{"ANN"});
					ContainerManager.getInstance().instantiateAgent("TU_Company29_FLEX"+i, "mas.JADE_VPP.TU", new String[]{"ANN"});
					ContainerManager.getInstance().instantiateAgent("TU_Company30_FLEX"+i, "mas.JADE_VPP.TU", new String[]{"ANN"});
				
				}
				*/
			}
			catch (Exception ex){
					ex.printStackTrace();
			}	
		}
}
