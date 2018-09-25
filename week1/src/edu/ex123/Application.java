package edu.ex123;

import jade.core.Profile; 
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.core.Runtime; 

public class Application {
	public static void main(String[] args) {
		Profile myProfile = new ProfileImpl();
		Runtime myRuntime = Runtime.instance(); 
		ContainerController myContainer = myRuntime.createMainContainer(myProfile);
		try {
			AgentController rma = myContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
			rma.start();
			AgentController myAgent1 = myContainer.createNewAgent("Fred", SimpleAgent.class.getCanonicalName(), null); 
			AgentController myAgent2 = myContainer.createNewAgent("Ted", SimpleAgent.class.getCanonicalName(), null); 
			AgentController myAgent3 = myContainer.createNewAgent("Tom", SimpleAgent.class.getCanonicalName(), null); 
			myAgent1.start(); 
			myAgent2.start(); 
			myAgent3.start();
		} catch (Exception e){
			System.out.println("Exception starting agent: " + e.toString());
		}
	}   
}
