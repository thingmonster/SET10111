package edu.marketplace;

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
			String[] books = {"Java"};
			AgentController advertiser = myContainer.createNewAgent(
					"Ted", BookAdvertiserAgent.class.getCanonicalName(), null
				); 
			AgentController buyer = myContainer.createNewAgent(
					"Fred", BookBuyerAgent.class.getCanonicalName(), books
				); 
			AgentController seller = myContainer.createNewAgent(
					"Tom", BookSellerAgent.class.getCanonicalName(),null
				); 
			advertiser.start(); 
			buyer.start(); 
			seller.start(); 
		} catch (Exception e){
			System.out.println("Exception starting agent: " + e.toString());
		}
	}   
}
