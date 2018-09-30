package edu.booktrading;

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
			String[] books = {"Java for Dummies"};
			AgentController myAgent = myContainer.createNewAgent(
				"Fred", 
				BookBuyerAgent.class.getCanonicalName(), 
				books
			); 
			AgentController myAgent2 = myContainer.createNewAgent(
					"Tom", 
					BookSellerAgent.class.getCanonicalName(), 
					null
				); 
			myAgent.start(); 
			myAgent2.start(); 
		} catch (Exception e){
			System.out.println("Exception starting agent: " + e.toString());
		}
	}   
}
