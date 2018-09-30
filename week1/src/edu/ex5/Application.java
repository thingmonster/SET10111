package edu.ex5;

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
			AgentController myAgent = myContainer.createNewAgent("Fred", ComplexBehaviourAgent.class.getCanonicalName(), null); 
			myAgent.start(); 
		} catch (Exception e){
			System.out.println("Exception starting agent: " + e.toString());
		}
	}   
}
