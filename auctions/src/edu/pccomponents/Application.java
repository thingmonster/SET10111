package edu.pccomponents;

import java.util.Hashtable;

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

			Hashtable<String, Float> components = new Hashtable<String, Float>();
			components.put("Keyboard", (float) 8);
			components.put("CPU", (float) 8);
			components.put("Monitor", (float) 8);
			components.put("Memory Module", (float) 8);
			components.put("Case", (float) 8);
			components.put("Mouse", (float) 8);
			components.put("SSD", (float) 8);
			components.put("Motherboard", (float) 8);

			AgentController auctioneer = myContainer.createNewAgent(
					"Alan", AuctioneerAgent.class.getCanonicalName(), null
			); 

			AgentController bidder1 = myContainer.createNewAgent(
					"Bob", BidderAgent.class.getCanonicalName(), new Object[] {components}
			); 

			AgentController bidder2 = myContainer.createNewAgent(
					"Ben", BidderAgent.class.getCanonicalName(), new Object[] {components}
			); 

			AgentController bidder3 = myContainer.createNewAgent(
					"Bill", BidderAgent.class.getCanonicalName(), new Object[] {components}
			); 

			AgentController bidder4 = myContainer.createNewAgent(
					"Bert", BidderAgent.class.getCanonicalName(), new Object[] {components}
			); 

			AgentController bidder5 = myContainer.createNewAgent(
					"Bart", BidderAgent.class.getCanonicalName(), new Object[] {components}
			); 

			auctioneer.start();
			bidder1.start();
//			bidder2.start();
//			bidder3.start();
//			bidder4.start();
//			bidder5.start();
			
			
		} catch (Exception e){
			System.out.println("Exception starting agent: " + e.toString());
		}
	}   
}
