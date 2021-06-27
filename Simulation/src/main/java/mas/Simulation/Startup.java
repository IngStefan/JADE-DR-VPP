package mas.Simulation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

//******* Startup.java: starts the agent platform and other services for this application 

//SPRING BOOT AND AUTOBUILDER FOR JADE AGENT
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;


@SpringBootApplication
public class Startup {
 	
	private static String DATE = "2021-06-27 ";
	private static int i = 0;
		public static void main(String[] args) //throws URISyntaxException 
		{
			boolean done = false;
			System.out.println( "Hello World! Simulation started" );
			SpringApplicationBuilder builder = new SpringApplicationBuilder(Startup.class);
			builder.headless(false);
			@SuppressWarnings("unused")
			ConfigurableApplicationContext context = builder.run(args);
			boolean oneTimeTest = true;
			Date date = new Date();
			Date startdate = new Date();
			String dateNowString = "0000";
			long t = date.getTime();
			Date afterAdding15Min=new Date(t + (1 * 15 * 60000));
			Date afterAddingOneMin=new Date(t + (1 * 60000));
			Date afterAdding15Secs=new Date(t + (1 * 15000));
			int step = 0;
			int quarterMinute = 0;
			boolean stateActivated = false;
			long startTime; 	//needed for time measurements
			long endTime;  		//needed for time measurements
			long timeElapsed; 	//needed for time measurements
			
			//Preparing the start of the simulation
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
			formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
			try {
				startdate = formatter.parse(DATE+"00:00:00");
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			long l = startdate.getTime();
			Date afterSubstracting3Mins=new Date(l - (3 * 60000));
			
			while(!done){
			Date dateNow = new Date();
			if(dateNow.after(afterSubstracting3Mins)) {
				
				if(Global.schedulingTrigger){
					try {
					System.out.println( "First PROPOSE received" );
					Thread.sleep(5000);
					System.out.println( "Calculating best offers" );
					Thread.sleep(5000);
//					Thread.sleep(50000);
					if(Global.OfferReferenceIDs.size()>0) {
						System.out.println("Nr:"+Global.OfferReferenceIDs.size()+" offers received");
						for(int i = 0; i < Global.OfferReferenceIDs.size(); i++) {
					    		System.out.println("Scheduling Plans received");
						    	int randomNum = ThreadLocalRandom.current().nextInt(0, 100); 
						    	if (randomNum >15)
						    	{
						    		System.out.println("Scheduling Plans accepted");
						    		InterfacePayloadAcceptReject payload = new InterfacePayloadAcceptReject(Global.OfferReferenceIDs.get(i));
							    	ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
									putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.SCHEDULINGACCEPTED,payload);	
							   	}else {
						    		System.out.println("Scheduling Plans refused");
						    		InterfacePayloadAcceptReject payload = new InterfacePayloadAcceptReject(Global.OfferReferenceIDs.get(i));
							    	ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
							    	putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.SCHEDULINGREJECTED,payload);
							   	}
						    Thread.sleep(50);				
						}
						Global.OfferReferenceIDs.clear();
						Global.schedulingTrigger = false;
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}	
				}
				if(Global.accountingTrigger) {
					String _tuName = Global.accountingList.remove(Global.accountingList.size()-1);
		    		InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(_tuName);
			    	ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
					putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.ACCOUNTINGECPRECEIVED,payload);
					if(Global.accountingList.isEmpty()) {
						Global.accountingTrigger = false;
					}
				}
			
				//******SIMULATION SPEED SETTINGS ******************
				
				// 1 Minute Setting
				// Checking if the expiration time for the offer transfer is over, then move to the next step 			
//				if(dateNow.after(afterAddingOneMin)) {
//					t = dateNow.getTime();
//					afterAddingOneMin=new Date(t + (1 * 60000));
//					step++;
//					//stateActivated = false;
//				}

				
				// Hybrid Setting
				//	Change the simulation speed at the activation day. before that, a faster simulation speed for the planning mechanisms is used
				if(step >= 3) {
					//Normal Operation Time
					//********* 15 Minutes Operations *************				
					if(dateNow.after(afterAdding15Min)) {
						if(quarterMinute==5) {
							quarterMinute = -1;
							step++;
						}
						t = dateNow.getTime();
						afterAdding15Min = new Date(t + (1 * 15 * 60000));
						quarterMinute++;
						stateActivated = false;
					}
				}else {
					//Faster Operation Time
					//********* 15 Seconds Operations *************
					if(dateNow.after(afterAdding15Secs)) {
						if(quarterMinute==5) {
							quarterMinute = -1;
							step++;
						}
						t = dateNow.getTime();
						afterAdding15Secs=new Date(t + (1 * 15000));
						quarterMinute++;
						stateActivated = false;
					}
				}

				//create current time as String
				dateNowString = formatter.format(dateNow);
				
				
				////******************** How to use the simulation **************
				//************
				//startScheduling(service description, expiration time, start of schedule, end of schedule)
				//startScheduling("SRL","2022-02-21 00:00:00.0","2022-02-22 00:00:00.0", "2022-02-20 00:00:00.0");
				//************
				//requestNewSetpoint(maxEnergy, TuName, amount of seconds to request) 
				//requestNewSetpoint(200,"TU_Company25",15);
				//************
				//startBalancing(tuName, time start, time end)
				//startBalancing("TUEmdenCompany0", dateNowString,"2022-02-22 00:00:00.0");
				//************
				//startBalancing(tuName, time start, time end, updaterate)
				//startBalancingUpdateRate("TUEmdenCompany0", dateNowString,"2022-02-22 00:00:00.0", 1000);
				//************
				//enableFR(tuName)
				//enableFR("TULeerCompany0");
				//************
				//blockFR(tuName)
				//blockFR("TULeerCompany1");
				//************
				//broadcastLTW(2021);
				
				/*
				//*******  TIMING MEASUREMENTS   **********
				startTime = System.nanoTime();
				System.out.println("Roundtrip START time in milliseconds : " + startTime / 1000000); //AUSGABE
//				System.out.println( "Time: "+ dateNow +" and Iterator for Request: " +i );
				enableFR("TU_Company7_SOL");
				endTime = System.nanoTime();  	//******************ZEITMESSUNG ENDE**************
				timeElapsed = endTime - startTime; 	//******************ZEITMESSUNG Berechnung**************
//			    System.out.println("Execution time in nanoseconds  : " + timeElapsed); //AUSGABE
//			    System.out.println("Execution time in milliseconds : " + timeElapsed / 1000000); //AUSGABE
			    System.out.println( "Time: "+ dateNow +" and Iterator for Request: " +i );
			    i++;
				
				*/
				
				//*****************one shot tests************
//				if(oneTimeTest){
//				String time ="16:42:00.0";
//				try {
//				requestNewSetpoint(2000,"TU_Company1_SRL",DATE+time);
//				Thread.sleep(39);
//				startBalancing("TU_Company1_SRL", dateNowString,DATE+time);
//				Thread.sleep(39);
//				enableFR("TU_Company6_SOL");
//				Thread.sleep(39);
//				blockFR("TU_Company7_SOL");
//				Thread.sleep(39);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				oneTimeTest = false;
//				}
				
				
				//******************** SIMULATION SCENARIO **********
				//State machine for hour blocks
//				/*
				if(!stateActivated) {
					stateActivated = true;
					String activationDay = DATE;
					try {
					switch(step){
					//********** Starting the Activation Simulation (t - 3 hours) **********
					case 0: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: before 21.02.2022 00:00" );
							System.out.println( "***** Planning Phase *****" );
							startScheduling("PRL","2022-02-21 00:00:00.0","2022-02-22 00:00:00.0", "2022-02-20 00:00:00.0");
						}else if(quarterMinute == 2) {
							startScheduling("SRL","2022-02-21 00:00:00.0","2022-02-22 00:00:00.0", "2022-02-20 00:00:00.0");
						}
						break;
					case 1:
						if(quarterMinute == 0) {
							startScheduling("SOL","2022-02-21 00:00:00.0","2022-02-22 00:00:00.0", "2022-02-20 00:00:00.0");
						}else if(quarterMinute == 2) {
							startScheduling("MRL","2022-02-21 00:00:00.0","2022-02-22 00:00:00.0", "2022-02-20 00:00:00.0");
						}
						break;
					case 2: 
						if(quarterMinute == 0) {
							startScheduling("SNL","2022-02-21 00:00:00.0","2022-02-22 00:00:00.0", "2022-02-20 00:00:00.0");
						}else if(quarterMinute == 2) {
							startScheduling("FLEX","2022-02-21 00:00:00.0","2022-02-22 00:00:00.0", "2022-02-20 00:00:00.0");
						}	
						break;
					//********** Starting the Activation Simulation (t = 00:00) **********
					case 3:
						if(quarterMinute == 0) {
							System.out.println( "***** Planning Phase Ended *****" );
							System.out.println( "***** Activation Phase started *****" );
							System.out.println( "Timestamp: 21.02.2022 00:00");
							startBalancing("TU_Company1_PRL", dateNowString,activationDay+"04:15:00.0");
							Thread.sleep(39);
							startBalancing("TU_Company1_SRL", dateNowString,activationDay+"23:59:59.0");
							Thread.sleep(39);
							requestNewSetpoint(2000,"TU_Company1_SRL",activationDay+"05:30:00.0");
							Thread.sleep(39);

						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 00:15");
							startBalancing("TU_Company28_FLEX", dateNowString,activationDay+"03:30:00.0");
							Thread.sleep(39);
							requestNewSetpoint(6000,"TU_Company28_FLEX",activationDay+"03:30:00.0");
		
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 00:30");
							startBalancing("TU_Company26_FLEX", dateNowString,activationDay+"03:30:00.0");
							Thread.sleep(39);
							requestNewSetpoint(-3800,"TU_Company26_FLEX",activationDay+"03:30:00.0");
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 00:45");
							startBalancing("TU_Company15_SNL", dateNowString,activationDay+"04:45:00.0");
							Thread.sleep(39);
							requestNewSetpoint(5000,"TU_Company15_SNL",activationDay+"04:45:00.0");
						}
						break;
					case 4:
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 01:00");							
							startBalancing("TU_Company19_FLEX", dateNowString,activationDay+"02:30:00.0");
							Thread.sleep(39);
							requestNewSetpoint(13000,"TU_Company19_FLEX",activationDay+"02:30:00.0");
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 01:15");
							
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 01:30");
												
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 01:45");
							

						}
						break;
					case 5: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 02:00");		
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 02:15");
							startBalancing("TU_Company30_FLEX", dateNowString,activationDay+"09:30:00.0");
							Thread.sleep(39);
							requestNewSetpoint(12000,"TU_Company30_FLEX",activationDay+"09:30:00.0");		
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 02:30");
												
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 02:45");
							

						}
						
						break;
					case 6:
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 03:00");							
							startBalancing("TU_Company24_FLEX", dateNowString,activationDay+"09:30:00.0");
							Thread.sleep(39);
							requestNewSetpoint(5500,"TU_Company24_FLEX",activationDay+"09:30:00.0");
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 03:15");
							startBalancing("TU_Company14_SNL", dateNowString,activationDay+"11:15:00.0");
							Thread.sleep(39);
							requestNewSetpoint(8000,"TU_Company14_SNL",activationDay+"11:15:00.0");			
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 03:30");
							startBalancing("TU_Company2_MRL", dateNowString,activationDay+"04:30:00.0");
							Thread.sleep(39);
							requestNewSetpoint(600,"TU_Company2_MRL",activationDay+"04:30:00.0");				
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 03:45");
							startBalancing("TU_Company29_FLEX", dateNowString,activationDay+"06:30:00.0");
							Thread.sleep(39);
							requestNewSetpoint(-43000,"TU_Company29_FLEX",activationDay+"06:30:00.0");
							

						}
						break;
					case 7: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 04:00");							
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 04:15");
							startBalancing("TU_Company21_FLEX", dateNowString,activationDay+"09:00:00.0");
							Thread.sleep(39);
							requestNewSetpoint(-100000,"TU_Company21_FLEX",activationDay+"09:00:00.0");
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 04:30");
							startBalancing("TU_Company3_PRL", dateNowString,activationDay+"08:00:00.0");
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 04:45");
							

						}
						break;
					case 8:
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 05:00");							
							startBalancing("TU_Company5_MRL_B", dateNowString,activationDay+"12:00:00.0");
							Thread.sleep(39);
							requestNewSetpoint(-2500,"TU_Company5_MRL_B",activationDay+"12:00:00.0");	
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 05:15");
							startBalancing("TU_Company2_PRL", dateNowString,activationDay+"23:59:59.0");
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 05:30");
												
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 05:45");


						}
						break;
					case 9: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 06:00");							
							startBalancing("TU_Company3_SRL", dateNowString,activationDay+"12:00:00.0");
							Thread.sleep(39);
							requestNewSetpoint(35000,"TU_Company3_SRL",activationDay+"12:00:00.0");
							Thread.sleep(39);
							broadcastLTW(2021);
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 06:15");
							
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 06:30");
							startBalancing("TU_Company18_FLEX", dateNowString,activationDay+"10:45:00.0");
							Thread.sleep(39);
							requestNewSetpoint(6900,"TU_Company18_FLEX",activationDay+"10:45:00.0");					
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 06:45");
							startBalancing("TU_Company1_MRL", dateNowString,activationDay+"11:15:00.0");
							Thread.sleep(39);
							requestNewSetpoint(2000,"TU_Company1_MRL",activationDay+"11:15:00.0");
						}
						break;
					case 10:
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 07:00");							
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 07:15");
							startBalancing("TU_Company6_SOL", dateNowString,activationDay+"08:15:00.0");
							Thread.sleep(39);
							enableFR("TU_Company6_SOL");
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 07:30");
							startBalancing("TU_Company4_SRL", dateNowString,activationDay+"09:00:00.0");
							Thread.sleep(39);
							requestNewSetpoint(-350,"TU_Company4_SRL",activationDay+"09:00:00.0");
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 07:45");
							

						}
						break;
					case 11: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 08:00");							
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 08:15");
							blockFR("TU_Company6_SOL");
							Thread.sleep(39);
							startBalancing("TU_Company25_FLEX", dateNowString,activationDay+"13:00:00.0");
							Thread.sleep(39);
							requestNewSetpoint(5900,"TU_Company25_FLEX",activationDay+"13:00:00.0");
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 08:30");
							startBalancing("TU_Company5_MRL_C", dateNowString,activationDay+"20:15:00.0");
							Thread.sleep(39);
							requestNewSetpoint(3000,"TU_Company5_MRL_C",activationDay+"20:15:00.0");						
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 08:45");
							

						}
						break;
					case 12:
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 09:00");							
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 09:15");
							
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 09:30");
												
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 09:45");
							

						}
						break;
					case 13: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 10:00");							
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 10:15");
							
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 10:30");
							blockFR("TU_Company7_SOL");
							Thread.sleep(39);
							blockFR("TU_Company8_SOL");
							Thread.sleep(39);
							startBalancing("TU_Company20_FLEX", dateNowString,activationDay+"16:00:00.0");
							Thread.sleep(39);
							requestNewSetpoint(-2000,"TU_Company20_FLEX",activationDay+"16:00:00.0");
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 10:45");
							

						}
						break;
					case 14:
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 11:00");							
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 11:15");
							
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 11:30");
												
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 11:45");
							

						}
						break;
					case 15: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 12:00");	
							broadcastLTW(2021);
						
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 12:15");
							
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 12:30");
							startBalancing("TU_Company3_MRL", dateNowString,activationDay+"17:30:00.0");
							Thread.sleep(39);
							requestNewSetpoint(1000,"TU_Company3_MRL",activationDay+"17:30:00.0");
						

							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 12:45");
							startBalancing("TU_Company5_SRL", dateNowString,activationDay+"18:00:00.0");
							Thread.sleep(39);
							requestNewSetpoint(-450,"TU_Company5_SRL",activationDay+"18:00:00.0");
							
						}
						break;
					case 16:
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 13:00");							
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 13:15");
						
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 13:30");
							startBalancing("TU_Company4_MRL", dateNowString,activationDay+"20:00:00.0");
							Thread.sleep(39);
							requestNewSetpoint(4000,"TU_Company4_MRL",activationDay+"20:00:00.0");						
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 13:45");
							

						}
						break;
					case 17: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 14:00");							
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 14:15");
							startBalancing("TU_Company8_SOL", dateNowString,activationDay+"18:00:00.0");	
							Thread.sleep(39);
							enableFR("TU_Company8_SOL");

						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 14:30");
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 14:45");
							startBalancing("TU_Company13_SNL", dateNowString,activationDay+"19:45:00.0");
							requestNewSetpoint(4000,"TU_Company13_SNL",activationDay+"19:45:00.0");

						}
						break;
					case 18:
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 15:00");							
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 15:15");
							startBalancing("TU_Company24_FLEX", dateNowString,activationDay+"19:45:00.0");
							Thread.sleep(39);
							requestNewSetpoint(5500,"TU_Company24_FLEX",activationDay+"19:45:00.0");
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 15:30");
												
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 15:45");
							

						}
						break;
					case 19: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 16:00");							
							startBalancing("TU_Company2_SRL", dateNowString,activationDay+"23:59:59.0");
							Thread.sleep(39);
							requestNewSetpoint(30000,"TU_Company2_SRL",activationDay+"23:59:59.0");
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 16:15");
							
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 16:30");
							startBalancing("TU_Company9_SOL", dateNowString,activationDay+"20:15:00.0");		
							Thread.sleep(39);
							enableFR("TU_Company9_SOL");
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 16:45");
							

						}
						break;
					case 20:
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 17:00");
							startBalancing("TU_Company5_MRL_A", dateNowString,activationDay+"23:15:00.0");
							Thread.sleep(39);
							requestNewSetpoint(2000,"TU_Company5_MRL_A",activationDay+"23:15:00.0");	
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 17:15");
							
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 17:30");
												
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 17:45");
							

						}
						break;
					case 21: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 18:00");							
							blockFR("TU_Company8_SOL");
							Thread.sleep(39);
							startBalancing("TU_Company25_FLEX", dateNowString,activationDay+"21:00:00.0");
							Thread.sleep(39);
							requestNewSetpoint(5900,"TU_Company25_FLEX",activationDay+"21:00:00.0");
							Thread.sleep(39);
							broadcastLTW(2021);
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 18:15");

							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 18:30");
							startBalancing("TU_Company27_FLEX", dateNowString,activationDay+"22:15:00.0");
							Thread.sleep(39);
							requestNewSetpoint(-8000,"TU_Company27_FLEX",activationDay+"22:15:00.0");
												
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 18:45");
							startBalancing("TU_Company16_FLEX", dateNowString,activationDay+"22:00:00.0");
							Thread.sleep(39);
							requestNewSetpoint(-350000,"TU_Company16_FLEX",activationDay+"22:00:00.0");

						}
						break;
					case 22:
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 19:00");							
							startBalancing("TU_Company11_SNL", dateNowString,activationDay+"23:59:59.0");
							Thread.sleep(39);
							requestNewSetpoint(3000,"TU_Company11_SNL",activationDay+"23:59:59.");	
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 19:15");
							startBalancing("TU_Company29_FLEX", dateNowString,activationDay+"20:45:00.0");
							Thread.sleep(39);
							requestNewSetpoint(-43000,"TU_Company29_FLEX",activationDay+"20:45:00.0");
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 19:30");
												
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 19:45");
							startBalancing("TU_Company23_FLEX", dateNowString,activationDay+"21:15:00.0");
							Thread.sleep(39);
							requestNewSetpoint(4000,"TU_Company23_FLEX",activationDay+"21:15:00.0");

						}
						break;
					case 23: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 20:00");							
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 20:15");
							blockFR("TU_Company9_SOL");
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 20:30");
												
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 20:45");
							

						}
						break;
					case 24:
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 21:00");							
							startBalancing("TU_Company6_SOL", dateNowString,activationDay+"23:59:59.0");
							Thread.sleep(39);
							enableFR("TU_Company6_SOL");
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 21:15");
							startBalancing("TU_Company7_SOL", dateNowString,activationDay+"22:30:00.0");
							Thread.sleep(39);
							enableFR("TU_Company7_SOL");
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 21:30");
												
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 21:45");
							

						}
						break;
					case 25: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 22:00");							
							startBalancing("TU_Company18_FLEX", dateNowString,activationDay+"23:15:00.0");
							Thread.sleep(39);
							requestNewSetpoint(6900,"TU_Company18_FLEX",activationDay+"23:15:00.0");
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 22:15");
							startBalancing("TU_Company17_FLEX", dateNowString,activationDay+"22:45:00.0");
							Thread.sleep(39);
							requestNewSetpoint(6900,"TU_Company17_FLEX",activationDay+"22:45:00.0");
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 22:30");
												
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 22:45");
							

						}
						break;
					case 26: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 21.02.2022 23:00");	
							startBalancing("TU_Company10_SNL", dateNowString,activationDay+"23:30:00.0");
							Thread.sleep(39);
							requestNewSetpoint(3000,"TU_Company10_SNL",activationDay+"23:30:00.0");	
							
						}else if(quarterMinute == 1) {
							System.out.println( "Timestamp: 21.02.2022 23:15");
							
							
						}else if(quarterMinute == 2) {
							System.out.println( "Timestamp: 21.02.2022 23:30");
												
							
						}else if(quarterMinute == 3) {
							System.out.println( "Timestamp: 21.02.2022 23:45");
							

						}
						break;
					case 27: 
						if(quarterMinute == 0) {
							System.out.println( "Timestamp: 22.02.2022");							
							blockFR("TU_Company6_SOL");
							Thread.sleep(39);
							blockFR("TU_Company7_SOL");
						}else if(quarterMinute == 1) {
							
							
							
						}else if(quarterMinute == 2) {
							
												
							
						}else if(quarterMinute == 3) {
						
							done = true;

						}
						break;
						
					
					default:
						step = 0;
					
					}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
//				*/
				}
			}
			
		}
		
		
		
		

		
		
		
		
		public static void startScheduling(String _serviceDescription, String _startTime, String _endTime, String _expiration ) {
		    String referenceID = "21_02_2021";
		    String serviceDescription = "SD";
		    String startTime = "0000-00-00 00:00:00.0";
		    String endTime = "0000-00-00 00:00:00.0";
		    String expiration = "0000-00-00 00:00:00.0";
		    serviceDescription = _serviceDescription;
		    startTime = _startTime;
		    endTime = _endTime;
		    expiration = _expiration;
		    referenceID = referenceID+serviceDescription;
		    	    
		    InterfacePayloadPlanning payload = new InterfacePayloadPlanning(startTime,endTime, expiration,serviceDescription, referenceID );
	    	ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
			putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.STARTSCHEDULING,payload);	
		}
		
		
		public static void startBalancing(String _tuName, String _startTime, String _endTime ) {
		    String tuName = "noName";
		    String startTime = "0000-00-00 00:00:00.0";
		    String endTime = "0000-00-00 00:00:00.0";
		    String referenceID;
		    startTime = _startTime;
		    endTime = _endTime;
		    tuName = _tuName;
		    referenceID = tuName+startTime;
		    InterfacePayloadBalancing payload = new InterfacePayloadBalancing(startTime,endTime, tuName, referenceID );
	    	ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
			putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.BALANCING,payload);	
		}
		
		public static void startBalancingUpdateRate(String _tuName, String _startTime, String _endTime, int _updateRate ) {
		    String tuName = "noName";
		    String startTime = "0000-00-00 00:00:00.0";
		    String endTime = "0000-00-00 00:00:00.0";
		    String referenceID;
		    int updateRate = _updateRate;
		    startTime = _startTime;
		    endTime = _endTime;
		    tuName = _tuName;
		    referenceID = tuName+startTime;
		    InterfacePayloadBalancing payload = new InterfacePayloadBalancing(startTime,endTime, tuName, referenceID, updateRate );
	    	ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
			putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.BALANCING,payload);	
		}
		
		public static void requestNewSetpoint(int maxPower, String tuName, String _endTime) {
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				int count = 0;
				boolean negative = false;
//				int limit = _limitInS;
				Date _dateNow = new Date();
				String endTime = _endTime;
				int upperbound = maxPower;
				int lowerbound = (int)(maxPower*0.9);
				Date endTimeDate;
			    @Override
			    public void run() {
			    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
			    	formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
					try {
						endTimeDate = formatter.parse(endTime);
					} catch (ParseException e) {
							e.printStackTrace();
					}
					if(upperbound < 0) {
						upperbound = upperbound * (-1);
						lowerbound = lowerbound * (-1);
						negative = true;
					}
					int newSetpoint = ThreadLocalRandom.current().nextInt(lowerbound, upperbound);  
					if(negative) {
						newSetpoint = newSetpoint * (-1);
					}
					
					InterfacePayloadNewSetpoint payload = new InterfacePayloadNewSetpoint(newSetpoint,tuName);
			    	ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
					putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.SETPOINT,payload);
//				    count++;
//				     if (count >= limit) {
//				         timer.cancel();
//				         timer.purge();
//				         return;
//				     }
				    _dateNow = new Date();
				    //request new setpoint so gestalten, dass einfach enddatum uebergeben werden kann
				     if(_dateNow.after(endTimeDate)) {
				    	 timer.cancel();
//				         timer.purge();
//				         System.out.println( "Timer ended");
				         return; 
				     }
			    }
			}, 0, 1000);
		}
		
		
		
		public static void requestNewSetpointLoadProfile(int maxPower, String tuName, String _endTime) {
			Timer timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				boolean negative = false;
//				int limit = _limitInS;
				Date _dateNow = new Date();
				String endTime = _endTime;
				int upperbound = maxPower;
				int lowerbound = (int)(maxPower*0.9);
				Date endTimeDate;
				
				
			    @Override
			    public void run() {
					//generating random energy value
			    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.GERMAN);
			    	formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
					try {
						endTimeDate = formatter.parse(endTime);
					} catch (ParseException e) {
						e.printStackTrace();
					}
					if(upperbound < 0) {
						upperbound = upperbound * (-1);
						lowerbound = lowerbound * (-1);
						negative = true;
					}
					int newSetpoint = ThreadLocalRandom.current().nextInt(lowerbound, upperbound);    
					if(negative) {
						newSetpoint = newSetpoint * (-1);
					}
					InterfacePayloadNewSetpoint payload = new InterfacePayloadNewSetpoint(newSetpoint,tuName);
			    	ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
					putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.LOADPROFILE,payload);
//				    count++;
//				     if (count >= limit) {
//				         timer.cancel();
//				         timer.purge();
//				         return;
//				     }
				    _dateNow = new Date();
				    //request new setpoint so gestalten, dass einfach enddatum uebergeben werden kann
				     if(_dateNow.after(endTimeDate)) {
				    	 timer.cancel();
				         timer.purge();
//				         System.out.println( "Timer ended");
				         return; 
				     }
				     
			    }
			}, 0, 1000);
		}
		
		
		public static void enableFR(String _tuName) {
		    String tuName = "noName";
		    tuName = _tuName;
		    InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(tuName);
	    	ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
			putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.FRENABLEREQUEST,payload);	
		}
		
		public static void blockFR(String _tuName) {
		    String tuName = "noName";
		    tuName = _tuName;
		    InterfacePayloadAgentReference payload = new InterfacePayloadAgentReference(tuName);
	    	ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
			putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.FRBLOCKREQUEST,payload);	
		}
		
		public static void broadcastLTW(int tarifNumber) {
		    String loadTimeWindowsReference = "Tariff"+tarifNumber;
		    String windowHighBegin = "2022-01-18 05:00:00.0";
		    String windowHighEnd = "2022-03-18 07:00:00.0";
		    String windowLowBegin = "2022-06-18 05:00:00.0";
		    String windowLowEnd = "2022-08-18 07:00:00.0";
		    InterfacePayloadLoadTimeWindows payload = new InterfacePayloadLoadTimeWindows(loadTimeWindowsReference, windowHighBegin, windowHighEnd, windowLowBegin,  windowLowEnd);
	    	ConsumingRest_VPP putInstance = new ConsumingRest_VPP();
			putInstance.putNodeRed(Addresses.URL_NODERED, PutVariable.LOADTIMEWINDOWSINFORM,payload);	
		}
		
		
		
}
