package edu.marketplace;

import jade.core.Agent;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class BookBuyerAgent extends Agent {

	private AID[] advertiserAgents;
	private ArrayList<String> pending = new ArrayList<String>();
	private String title = "book";
	private float budget = 9;
	
	protected void setup() {		
		System.out.println("Buyer agent "+getAID().getName()+" is ready.");

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
							if (title.length() > 0) {
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
	
	private class BuyBook extends Behaviour {
		
		private int step = 0;
		MessageTemplate replyTemplate;
		
		public void action() {
			
			switch (step) {
			case 0:
				
				System.out.println("Buyer sending query");
				ACLMessage queryBook = new ACLMessage(ACLMessage.CFP);
				queryBook.addReceiver(advertiserAgents[0]);
				queryBook.setContent(title);
				queryBook.setConversationId(title);
				queryBook.setReplyWith("submitBook"+System.currentTimeMillis()); 
				myAgent.send(queryBook);

				ACLMessage queryBudget = new ACLMessage(ACLMessage.CFP);
				queryBudget.addReceiver(advertiserAgents[0]);
				queryBudget.setContent(Float.toString(budget));
				queryBudget.setConversationId(title);
				queryBudget.setReplyWith("submitBook"+System.currentTimeMillis()); 
				myAgent.send(queryBudget);

				replyTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(title),
						MessageTemplate.MatchInReplyTo(queryBudget.getReplyWith()));
				
				step = 1;
				
				break;
			case 1:
				
				ACLMessage msg = myAgent.receive(replyTemplate);
				if (msg != null) {

					if (msg.getPerformative() == ACLMessage.INFORM) {
						System.out.println("Buyer informed");
						step = 4;
					} else if (msg.getPerformative() == ACLMessage.REFUSE) {
						System.out.println("Buyer refused");

						myAgent.addBehaviour(new WakerBehaviour(myAgent, 3000) {
							protected void handleElapsedTimeout() {
								System.out.println("Try again");
								myAgent.addBehaviour(new BuyBook());
							}
						});
						step = 4;
					} else {

						System.out.println("something else");
					}
				} else {
					block();
				}
				
				break;
					
			}
			
		}
		
		public boolean done() {
			return step == 4;
		}
	}
}