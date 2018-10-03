package edu.marketplace;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BookAdvertiserAgent extends Agent {
	protected void setup() {		
		
		System.out.println("Advertiser agent "+getAID().getName()+" is ready.");
		
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
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				
				System.out.println("Advertiser agent "+getAID().getName()+" received book");
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				reply.setPerformative(ACLMessage.CONFIRM);
				reply.setContent("");
				
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class BookSubmissionServer
}
