package edu.marketplace;

import jade.core.Agent;

import java.util.ArrayList;
import java.util.Hashtable;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class BookSellerAgent extends Agent {
	
	private AID[] advertiserAgents;
	private BookSellerGui GUI;
	private Hashtable<String, Float> catalogue = new Hashtable<String, Float>();
	private ArrayList<String> pending = new ArrayList<String>();

	protected void setup() {

		System.out.println("Seller agent "+getAID().getName()+" is ready.");

		GUI = new BookSellerGui(this);
		GUI.showGui();
		
		TickerBehaviour searchDF = new TickerBehaviour(this, 3000) {
			protected void onTick() {
				
				if (advertiserAgents == null || advertiserAgents.length == 0) {
					
//					System.out.println("Seller searching for advertiser");
					
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("book-advertising");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
//						System.out.println("Found the following advertiser agents:");
						advertiserAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							advertiserAgents[i] = result[i].getName();
//							System.out.println(advertiserAgents[i].getName());							
						}

						if (advertiserAgents != null && advertiserAgents.length > 0) {
							addBehaviour(new BookRequestServer());
							if (pending.size() > 0) {
								myAgent.addBehaviour(new RegisterBooks());
							}
						}
						
					}

					catch (FIPAException fe) {
						fe.printStackTrace();
					}
				}	
				
			}

		};

		addBehaviour(searchDF);
	}

	protected void takeDown() {
		System.out.println("Seller agent "+getAID().getName()+" terminating.");
	}
	
	public void updateCatalogue(final String title, final float price) {

		catalogue.put(title, price);
		pending.add(title);
		
		if (advertiserAgents != null && advertiserAgents.length > 0) {
			this.addBehaviour(new RegisterBooks());
		}
		
	}
	
	private class RegisterBooks extends Behaviour {
		
		private MessageTemplate messageTemplate;
		private int step = 0;
		private String title;

		public void action() {
			switch (step) {
			case 0:
			
//				System.out.println("Seller agent "+getAID().getName()+" submitting book.");
				
				title = (String) pending.get(pending.size() - 1);
				
				ACLMessage submitBook = new ACLMessage(ACLMessage.INFORM);
				submitBook.addReceiver(advertiserAgents[0]);
				submitBook.setContent(title);
				submitBook.setConversationId(title);
				submitBook.setReplyWith("submitBook"+System.currentTimeMillis()); 
				myAgent.send(submitBook);
				
				step = 1;
				break;

			case 1:				
				ACLMessage reply = myAgent.receive(messageTemplate);
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.CONFIRM) {	

//						System.out.println("Seller agent "+getAID().getName()+" submitting price");						

						title = (String) pending.get(pending.size() - 1);
						float price = (float) catalogue.get(title);
						
						ACLMessage submitPrice = new ACLMessage(ACLMessage.INFORM);
						submitPrice.addReceiver(advertiserAgents[0]);
						submitPrice.setContent(Float.toString(price));
						submitPrice.setConversationId(title);
						submitPrice.setReplyWith("submitPrice"+System.currentTimeMillis()); 
						myAgent.send(submitPrice);
						
						pending.remove(pending.size() - 1);
						if (pending.size() > 0) {
							step = 0;
						} else {
							step = 2;
						}
					}					
				} else {
					block();
				}
				break;
			}
			
		}

		public boolean done() {
			if (step == 2) {
//				System.out.println("Seller agent "+getAID().getName()+" finished");
			}
			return (step == 2);
		}

			
	}
	
	private class BookRequestServer extends CyclicBehaviour {
		
		MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		ACLMessage msg;
		
		public void action() {
			
			msg = myAgent.receive(messageTemplate);
			
			if (msg != null) {

				String message = msg.getContent();
				System.out.println("request received for " + message);
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.CONFIRM);		
				reply.setContent("");					
				myAgent.send(reply);
				
				ACLMessage cfp = new ACLMessage(ACLMessage.CANCEL);
				cfp.addReceiver(advertiserAgents[0]);
				cfp.setContent(message);
				cfp.setConversationId("");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
								
			} else {
				block();
			}
			
		}
		
	}
}