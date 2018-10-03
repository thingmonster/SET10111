package edu.marketplace;

import java.util.ArrayList;
import java.util.Hashtable;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BookAdvertiserAgent extends Agent {
	
	private Hashtable directory;
	
	protected void setup() {	
		
		System.out.println("Advertiser agent "+getAID().getName()+" is ready.");
		
		directory = new Hashtable();
		
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("book-advertising");
		sd.setName("Purpose-Unknown");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		this.addBehaviour(new BookSubmissionServer());
	}

	protected void takeDown() {
		System.out.println("Advertiser agent "+getAID().getName()+" terminating.");
	}

	private class BookSubmissionServer extends CyclicBehaviour {

		private String title;
		
		private int step = 0;
		MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage msg;
		
		public void action() {
			switch (step) {
			case 0:

				msg = myAgent.receive(messageTemplate);
				if (msg != null) {
					
					title = msg.getContent();
					System.out.println("Advertiser agent "+getAID().getName()+" received book: "+title);
					
					ACLMessage reply = msg.createReply();	
					reply.setPerformative(ACLMessage.CONFIRM);
					reply.setContent("");					
					myAgent.send(reply);
					
					step = 1;
					
				} else {
					block();
				}
				break;
			case 1:
				
				msg = myAgent.receive(messageTemplate);
				if (msg != null) {
					
					AID seller = msg.getSender();
					int price = Integer.parseInt(msg.getContent());
					System.out.println("Advertiser agent "+getAID().getName()+" received price: "+price);

					if (!directory.containsKey(seller)) {
						directory.put(seller, new Hashtable());
					}
					
					((Hashtable) directory.get(seller)).put(title, price);
					
					System.out.print(directory.toString());
					
				} else {
					block();
				}
				break;
			}
		}
	}  // End of inner class BookSubmissionServer
}
