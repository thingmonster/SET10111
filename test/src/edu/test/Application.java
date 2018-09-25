package edu.test;

import jade.core.Profile; 
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Runtime; 

public class Application {
	public static void main(String[] args) {
		  //Setup the JADE environment
		  Profile myProfile = new ProfileImpl();
		  Runtime myRuntime = Runtime.instance(); 
		  ContainerController myContainer = myRuntime.createMainContainer(myProfile);
		  try {
		  //Start the agent controller, which is itself an agent (rma)    
		    AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
		    rma.start();
//		    System.out.println("hi");
		  //Now start our own SimpleAgent, called Fred.
		    AgentController myAgent = myContainer.createNewAgent("Fred", TickerAgent.class.getCanonicalName(), null); 
		    myAgent.start();
//		    System.out.println("again");
		    } catch (Exception e){
		    System.out.println("Exception starting agent: " + e.toString());
		    }
		  }   
		} 

