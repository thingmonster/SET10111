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
			String[] books = {"Java"};
			AgentController b1 = myContainer.createNewAgent(
					"Fred", BookBuyerAgent.class.getCanonicalName(), books
				); 
			AgentController b2 = myContainer.createNewAgent(
					"Ted", BookBuyerAgent.class.getCanonicalName(), books
				); 
			AgentController b3 = myContainer.createNewAgent(
					"Red", BookBuyerAgent.class.getCanonicalName(), books
				); 
			AgentController s1 = myContainer.createNewAgent(
					"Tom", BookSellerAgent.class.getCanonicalName(),null
				); 
//			AgentController s2 = myContainer.createNewAgent(
//					"Tim", BookSellerAgent.class.getCanonicalName(),null
//				); 
//			AgentController s3 = myContainer.createNewAgent(
//					"Todd", BookSellerAgent.class.getCanonicalName(),null
//				); 
//			AgentController s4 = myContainer.createNewAgent(
//					"Thor", BookSellerAgent.class.getCanonicalName(),null
//				); 
//			AgentController s5 = myContainer.createNewAgent(
//					"Wednesday", BookSellerAgent.class.getCanonicalName(),null
//				); 
			b1.start(); 
			b2.start(); 
			b3.start(); 
			s1.start(); 
//			s2.start(); 
//			s3.start(); 
//			s4.start(); 
//			s5.start(); 
		} catch (Exception e){
			System.out.println("Exception starting agent: " + e.toString());
		}
	}   
}
