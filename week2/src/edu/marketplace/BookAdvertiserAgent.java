package edu.marketplace;

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

		MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage msg;
		
		Hashtable conversations = new Hashtable();
		
		public void action() {

			msg = myAgent.receive(messageTemplate);
			
			if (msg != null) {
				
				// get seller ID
				AID seller = msg.getSender();
				if (!conversations.containsKey(seller)) {
					conversations.put(seller, new Hashtable());
				}
				
				// get conversation ID
				String conversationID = msg.getConversationId();

				// get message and create reply
				String submission = msg.getContent();
				ACLMessage reply = msg.createReply();

				try {
					
					// check if message is a number
				    float price = Float.parseFloat(submission);
				    System.out.println("Advertiser agent "+getAID().getName()+" received price: "+submission);
				    
				    // check if this conversation has already been started
				    if (((Hashtable) conversations.get(seller)).containsKey(conversationID)) {
				    	
				    	// save title and price to directory
				    	String title = (String) ((Hashtable) conversations.get(seller)).get(conversationID);
						if (!directory.containsKey(seller)) {
							directory.put(seller, new Hashtable());
						}						
						((Hashtable) directory.get(seller)).put(title, price);						
						reply.setPerformative(ACLMessage.CONFIRM);				
						
				    } else {	
				    	
				    	// or refuse to accept price because title has not been submitted
						reply.setPerformative(ACLMessage.REFUSE);						
				    }

				    System.out.println("");
				    System.out.println("Advertiser directory now looks like this:");
				    System.out.println(directory.toString());
				    System.out.println("");

				    System.out.println("");
				    System.out.println("Advertiser conversations now looks like this:");
				    System.out.println(conversations.toString());
				    System.out.println("");
				    
				    ((Hashtable) conversations.get(seller)).remove(conversationID);

				    System.out.println("");
				    System.out.println("Advertiser conversations now looks like this:");
				    System.out.println(conversations.toString());
				    System.out.println("");
				    
				} catch (NumberFormatException e) {
					
					// if message isn't a number it must be a title
					System.out.println("Advertiser agent "+getAID().getName()+" received book: "+submission);
					
					// add title and conversation ID to conversations hash table
					if (!conversations.containsKey(seller)) {
						Hashtable books = new Hashtable();
						books.put(conversationID, submission);
						conversations.put(seller, books);
					} else { 
						((Hashtable) conversations.get(seller)).put(conversationID, submission);
					}
					
					reply.setPerformative(ACLMessage.CONFIRM);
					
				}

				reply.setContent("");					
				myAgent.send(reply);
				
				
				
				
			} else {
				block();
			}
			

		}
	}  // End of inner class BookSubmissionServer
}
