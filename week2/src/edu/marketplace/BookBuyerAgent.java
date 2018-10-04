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
//							if (pending.size() > 0) {
								myAgent.addBehaviour(new BuyBooks());
//							}
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
	
	private class BuyBooks extends Behaviour {
		
		private int step = 0;
		
		public void action() {
			System.out.println("buyer action");
			switch (step) {
			case 0:
				
				System.out.println("Buyer sending query");
				ACLMessage queryBook = new ACLMessage(ACLMessage.CFP);
				queryBook.addReceiver(advertiserAgents[0]);
				queryBook.setContent("book");
				queryBook.setConversationId("book");
				queryBook.setReplyWith("submitBook"+System.currentTimeMillis()); 
				myAgent.send(queryBook);

				ACLMessage queryBudget = new ACLMessage(ACLMessage.CFP);
				queryBudget.addReceiver(advertiserAgents[0]);
				queryBudget.setContent("5");
				queryBudget.setConversationId("book");
				queryBudget.setReplyWith("submitBook"+System.currentTimeMillis()); 
				myAgent.send(queryBudget);
				
				step = 1;
				
				break;
			case 1:
				MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
				ACLMessage msg = myAgent.receive(messageTemplate);
				
				if (msg != null) {
					step = 2;
					
				} else {
					block();
				}
				break;
					
			}
			
		}
		
		public boolean done() {
			return step == 2;
		}
	}
}