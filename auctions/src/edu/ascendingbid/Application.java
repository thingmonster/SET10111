package edu.ascendingbid;

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

			Hashtable<String, Float> books = new Hashtable<String, Float>();
			books.put("Java", (float) 8);
			books.put("C", (float) 8);
			books.put("CSS", (float) 8);

			Hashtable<String, Float> books2 = new Hashtable<String, Float>();
			books2.put("Java", (float) 9);
			books2.put("CSS", (float) 7);

			AgentController auctioneer = myContainer.createNewAgent(
					"Alan", AuctioneerAgent.class.getCanonicalName(), null
			); 

			AgentController bidder1 = myContainer.createNewAgent(
					"Bob", BidderAgent.class.getCanonicalName(), new Object[] {books}
			); 

			AgentController bidder2 = myContainer.createNewAgent(
					"Ben", BidderAgent.class.getCanonicalName(), new Object[] {books2}
			); 

			auctioneer.start();
			bidder1.start();
			bidder2.start();
			
			
		} catch (Exception e){
			System.out.println("Exception starting agent: " + e.toString());
		}
	}   
}
