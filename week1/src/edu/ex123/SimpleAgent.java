package edu.ex123;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import java.util.Random;

public class SimpleAgent extends Agent {


	Random rand = new Random();
	int limit = rand.nextInt(5000) + 10000;
	long startTime = System.currentTimeMillis();
	
	
	protected void setup() {
		
		System.out.println("Hello! Agent "+getAID().getName()+" is ready.");

		addBehaviour(new TickerBehaviour(this, 3000) { 
			
			protected void onTick() {
				if (System.currentTimeMillis() - startTime > limit) {
					System.out.println("Bye from "+getAID().getName());
					myAgent.doDelete();
				} else {
					System.out.println("Hello from "+getAID().getName());
				}
			} 
		}); 
		
		
		
	} 
}
