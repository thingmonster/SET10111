

package edu.sealedbid;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class BidderAgent extends Agent {
	
	private Hashtable<String, Float> shoppingList = new Hashtable<String, Float>();
	private AID auctioneer;
	
	protected void setup() {
		
		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			shoppingList = (Hashtable<String, Float>) args[0];
			System.out.println(shoppingList.toString());

			DFAgentDescription template = new DFAgentDescription();
			ServiceDescription sd = new ServiceDescription();
			sd.setType("book-auction");
			template.addServices(sd);
			try {
				DFAgentDescription[] result = DFService.search(this, template); 
				if (result.length > 0) {
					auctioneer = result[0].getName();
					System.out.println("auctioneer found" + auctioneer.getName());
					addBehaviour(new Register());
				}
			}
			catch (FIPAException fe) {
				fe.printStackTrace();
			}


		}
	}

	protected void takeDown() {
		
		System.out.println("Bidder agent "+getAID().getName()+" terminating.");
	}
	

	private class Register extends Behaviour {

		public void action() {
			
				ACLMessage register = new ACLMessage(ACLMessage.REQUEST);
				register.addReceiver(auctioneer);
				register.setContent("register");
				register.setReplyWith("request"+System.currentTimeMillis()); // Unique value
				myAgent.send(register);
		}

		public boolean done() {
			return true;
		}
	}
}
	
	
