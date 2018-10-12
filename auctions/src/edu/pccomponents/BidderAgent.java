

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
	private String currentItem;
	private Float currentStartingPrice;
	private Float currentAmount;
	private Float budget = (float) 1000;
	private int PCs = 0;
	private ArrayList<String> basket = new ArrayList<String>();
	Random rand = new Random();

	protected void setup() {

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			shoppingList = (Hashtable<String, Float>) args[0];
		}
		
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
						addBehaviour(new TransactionServer());
						addBehaviour(new FinishServer());
						removeBehaviour(this);
					}
				}
				catch (FIPAException fe) {
					fe.printStackTrace();
				}
			}
			
		});

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

	private class InformServer extends CyclicBehaviour {
		public void action() {
			
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				currentItem = msg.getContent();
				System.out.println(myAgent.getLocalName() + " - "+Integer.toString(PCs) + " - £"+Float.toString(budget)+" "+basket.toString());
				
			} else {
				block();
			}
		}
	}

	private class CFPServer extends CyclicBehaviour {
		public void action() {
			
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId(currentItem),
					MessageTemplate.MatchPerformative(ACLMessage.CFP));
			
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {

				Float price = Float.parseFloat(msg.getContent());
				
				if (currentStartingPrice == null) {
					currentStartingPrice = price;
				}

				if (budget > price + 1) {

					String message = null;
					int state = 0;
					boolean bid = true;
					Set<String> parts;
					
					if (basket != null) {
						parts = new HashSet<String>(basket);
					} else {
						parts = new HashSet<String>();
					}
						
					
					if (Collections.frequency(basket, currentItem) > 1) {
						
						bid = false;
						
					} else if (parts.contains(currentItem)) {
						
						if ((price > currentStartingPrice * 1.2) || (budget < (price + 1) * 10)) {
							bid = false;
						}
						
					} else if (parts.size() > 6) {

						if ((price > currentStartingPrice * 2) || (budget < price + 1)) {
							bid = false;
						}
						
					} else if (parts.size() > 4) {

						if ((price > currentStartingPrice * 1.5) || (budget < (price + 1) * 1.5)) {
							bid = false;
						}
					} else {

						if ((price > currentStartingPrice * 1.3) || (budget < (price + 1) * 5)) {
							bid = false;
						}
					}
					
					if (bid) {

						int raise = rand.nextInt(5) + 1;
						
						Float amount = price + raise;
						if (amount > budget) {
							amount = budget;
						}
						
						currentAmount = amount;
						
						ACLMessage reply = msg.createReply();
						System.out.println(myAgent.getLocalName() + " placed a bid on " + currentItem + " for £"+amount);
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent(Float.toString(amount));
						reply.setConversationId(currentItem);
						myAgent.send(reply);
						
					} else {
						currentItem = null;
						currentStartingPrice = null;
					}
				} else {
					currentItem = null;
					currentStartingPrice = null;
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

				String component = msg.getContent();
				basket.add(component);
				budget -= currentAmount;
				currentAmount = null;
				
				Set<String> parts = new HashSet<String>(basket);
				if (parts.size() == 8) {
					for(String c : parts) {
						int i = basket.indexOf(c);
						basket.remove(i);
					}
					PCs++;
				}
								
			}
			else {
				block();
			}
		}
	}

	private class FinishServer extends CyclicBehaviour {
		public void action() {

			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {

				Float cost = (1000 - budget) / PCs;
				System.out.println(myAgent.getLocalName() + " - "+Integer.toString(PCs) + " - £"+Float.toString(budget)+" "+basket.toString());
				System.out.println(myAgent.getLocalName()+" bought "+PCs+" PCs at £"+cost);
								
			}
			else {
				block();
			}
		}
	}
	
}
	
	
