

package edu.pccomponents;

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
	private String currentItem;
	
	protected void setup() {

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			shoppingList = (Hashtable<String, Float>) args[0];
		}
		
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
						addBehaviour(new InformServer());
						addBehaviour(new CFPServer());
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

	private class InformServer extends CyclicBehaviour {
		public void action() {
			
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				if (shoppingList.containsKey(msg.getContent())) {
					currentItem = msg.getContent();
				} else {
					currentItem = null;
				}
			}
			else {
				block();
			}
		}
	}

	private class CFPServer extends CyclicBehaviour {
		public void action() {
			
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {

				Float price = Float.parseFloat(msg.getContent());
				
				if ((currentItem != null) && (shoppingList.containsKey(currentItem))) {
						
					Float bid = null;
					String message = null;
					
					if (price < shoppingList.get(currentItem)) {
						bid = price + 1;
						if (bid > shoppingList.get(currentItem)) {
							bid = shoppingList.get(currentItem);
						}
						if (bid > price) {
							message = Float.toString(bid);
						}
					}
					
					if (message != null) {
						
						ACLMessage reply = msg.createReply();
						System.out.println(myAgent.getLocalName() + " placed a bid on " + currentItem + " for £"+message);
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent(message);
						reply.setConversationId(currentItem);
						myAgent.send(reply);
					} else {
						currentItem = null;
					}
				} else {
					currentItem = null;
				}
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
	
	
