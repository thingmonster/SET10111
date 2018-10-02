package edu.marketplace;

import jade.core.Agent;

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

	protected void setup() {

		System.out.println("Seller agent "+getAID().getName()+" is ready.");
		
		TickerBehaviour searchDF = new TickerBehaviour(this, 3000) {
			protected void onTick() {
				
				if (advertiserAgents == null || advertiserAgents.length == 0) {
					
					System.out.println("Seller searching for advertiser");
					
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
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}
					if (advertiserAgents != null && advertiserAgents.length > 0) {
						myAgent.addBehaviour(new RegisterBook());
					}
				}	
				
			}

		};

		addBehaviour(searchDF);
	}

	protected void takeDown() {
		System.out.println("Seller agent "+getAID().getName()+" terminating.");
	}
	
	private class RegisterBook extends Behaviour {

		public void action() {
			
			System.out.println("Seller agent "+getAID().getName()+" submitting book.");
			
			ACLMessage cfp = new ACLMessage(ACLMessage.INFORM);
			cfp.addReceiver(advertiserAgents[0]);
			cfp.setContent("book");
			cfp.setConversationId("book-for-sale");
			cfp.setReplyWith("cfp"+System.currentTimeMillis()); 
			myAgent.send(cfp);
			
		}

		public boolean done() {
			return true;
		}

			
	}
	
}