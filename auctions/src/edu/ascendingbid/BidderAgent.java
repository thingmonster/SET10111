

package edu.ascendingbid;

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
	private BidderGui gui;
	
	protected void setup() {

		gui = new BidderGui(this);
		gui.showGui();

		System.out.println(shoppingList.toString());

		addBehaviour(new TickerBehaviour(this, 1000) {
			
			protected void onTick() {
				
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("book-auction");
				template.addServices(sd);
				try {
					DFAgentDescription[] result = DFService.search(myAgent, template); 
					if (result.length > 0) {
						auctioneer = result[0].getName();
						System.out.println("auctioneer found" + auctioneer.getName());
						addBehaviour(new Register());
						addBehaviour(new CFPServer());
						addBehaviour(new TransactionServer());
						removeBehaviour(this);
					}
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
			
		});

	}
	
	protected void buyBook(String title, Float budget) {
		shoppingList.put(title, budget);
	}

	protected void takeDown() {

		gui.dispose();
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

	private class CFPServer extends CyclicBehaviour {
		public void action() {
			
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {

				String message = msg.getContent();
				
				ACLMessage reply = msg.createReply();
				if (shoppingList.containsKey(message)) {
					System.out.println(myAgent.getLocalName() + " placed a bid on " + message);
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(shoppingList.get(message).toString());
				} else {
					System.out.println(myAgent.getLocalName() + " has refused " + message);
					reply.setPerformative(ACLMessage.REFUSE);
					reply.setContent("");
				}
				myAgent.send(reply);
				
				
			}
			else {
				block();
			}
		}
	}

	private class TransactionServer extends CyclicBehaviour {
		public void action() {

			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {

				String title = msg.getContent();
				shoppingList.remove(title);
				System.out.println(myAgent.getLocalName() + " has bought " + title);
			}
			else {
				block();
			}
		}
	}
	
}
	
	
