package edu.marketplace;

import jade.core.Agent;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class BookBuyerAgent extends Agent {

	private BookBuyerGui GUI;
	private AID[] advertiserAgents;
	private ArrayList<String> pending = new ArrayList<String>();
	private String title;
	private float budget;
	
	protected void setup() {		
		System.out.println("Buyer agent "+getAID().getName()+" is ready.");

		GUI = new BookBuyerGui(this);
		GUI.showGui();

		TickerBehaviour searchDF = new TickerBehaviour(this, 3000) {
			protected void onTick() {
				
				if (advertiserAgents == null || advertiserAgents.length == 0) {
					
					System.out.println("Buyer searching for advertiser");
					
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("book-advertising");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 
						System.out.println("Found the following advertiser agents:");
						advertiserAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							advertiserAgents[i] = result[i].getName();
							System.out.println(advertiserAgents[i].getName());							
						}

						if (advertiserAgents != null && advertiserAgents.length > 0) {
							if (title != null) {
								myAgent.addBehaviour(new BuyBook());
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
		System.out.println("Buyer agent "+getAID().getName()+" terminating.");
	}

	public void bookRequested(final String t, final Float b) {

		title = t;
		budget = b;
		this.addBehaviour(new BuyBook());
		
		
	}
	

	private class BuyBook extends Behaviour {
		
		private int step = 0;
		MessageTemplate replyTemplate;
		ACLMessage msg;
		
		public void action() {
			
			if (title == null) {
				step = 4;
			}
			
			switch (step) {
			case 0:
				
				// send title to advertiser
				System.out.println("Buyer looking for "+title);
				ACLMessage queryBook = new ACLMessage(ACLMessage.CFP);
				queryBook.addReceiver(advertiserAgents[0]);
				queryBook.setContent(title);
				queryBook.setConversationId(title);
				queryBook.setReplyWith("submitBook"+System.currentTimeMillis()); 
				myAgent.send(queryBook);

				// send budget to advertiser
				ACLMessage queryBudget = new ACLMessage(ACLMessage.CFP);
				queryBudget.addReceiver(advertiserAgents[0]);
				queryBudget.setContent(Float.toString(budget));
				queryBudget.setConversationId(title);
				queryBudget.setReplyWith("submitBook"+System.currentTimeMillis()); 
				myAgent.send(queryBudget);

				// prepare reply template
				replyTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(title),
						MessageTemplate.MatchInReplyTo(queryBudget.getReplyWith()));
				
				step = 1;
				
				break;
			case 1:
				
				msg = myAgent.receive(replyTemplate);
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.INFORM) {
						
						// advertiser has responded with seller AID
						System.out.println("Buyer informed");
						try {
							AID seller = (AID) msg.getContentObject();
							
							// send request to seller
							ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
							cfp.addReceiver(seller);
							cfp.setContent(title);
							cfp.setConversationId(title);
							cfp.setReplyWith("cfp"+System.currentTimeMillis());
							myAgent.send(cfp);

							// prepare reply template
							replyTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(title),
									MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
							
							step = 2;
							
							
						} catch (UnreadableException e) {
							System.out.println("Failed to load seller AID");
							step = 4;
						}
						
					} else if (msg.getPerformative() == ACLMessage.REFUSE) {
						
						// advertiser does not have this book
						System.out.println("Buyer refused");
						
						// try again in three seconds
						myAgent.addBehaviour(new WakerBehaviour(myAgent, 6000) {
							
							protected void handleElapsedTimeout() {
								System.out.println("Try again");
								myAgent.addBehaviour(new BuyBook());								
							}
						});
						step = 4;
					} else {

						System.out.println("reply not recognised");
					}
				} else {
					block();
				}
				
				break;
			case 2:
				

				msg = myAgent.receive(replyTemplate);
				if (msg != null) {
					if (msg.getPerformative() == ACLMessage.CONFIRM) {
						
						System.out.println("purchase confirmed");
						title = null;
						budget = 0;
						step = 4;
						
					}
				}
				break;					
			}
			
		}
		
		public boolean done() {
			return step == 4;
		}
	}
}