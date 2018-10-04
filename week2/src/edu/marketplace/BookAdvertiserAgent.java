package edu.marketplace;

import java.util.Hashtable;
import java.util.Set;

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
		this.addBehaviour(new BookRequestServer());
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
//				    System.out.println("Advertiser agent "+getAID().getName()+" received price: "+submission);
				    
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

//				    System.out.println("");
//				    System.out.println("Advertiser conversations now looks like this:");
//				    System.out.println(conversations.toString());
//				    System.out.println("");
				    
				    ((Hashtable) conversations.get(seller)).remove(conversationID);

//				    System.out.println("");
//				    System.out.println("Advertiser conversations now looks like this:");
//				    System.out.println(conversations.toString());
//				    System.out.println("");
				    
				} catch (NumberFormatException e) {
					
					// if message isn't a number it must be a title
//					System.out.println("Advertiser agent "+getAID().getName()+" received book: "+submission);
					
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

	private class BookRequestServer extends CyclicBehaviour {
		
		private Hashtable conversations = new Hashtable();

		MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		ACLMessage msg;
		
		public void action() {

			msg = myAgent.receive(messageTemplate);
			
			if (msg != null) {
				
				String submission = msg.getContent();
				String conversationID = msg.getConversationId();
				AID sender = msg.getSender();
				
				System.out.println("Received query about " + submission);
				
				if (!conversations.containsKey(sender)) {
					conversations.put(sender, new Hashtable());
				}
				
				if (!((Hashtable) conversations.get(sender)).containsKey(conversationID)) {
					((Hashtable) conversations.get(sender)).put(conversationID, submission);
				} else {
					AID candidate = null;
					try {
						float budget = Float.parseFloat(submission);
				        Set<AID> sellers = directory.keySet();
				        for(AID seller: sellers){
				        	System.out.println("seller: "+seller);
				        	Set<String> books = ((Hashtable) directory.get(seller)).keySet();
					        for(String book: books){
					        	System.out.println("book: "+book);
					        	float price = (float) ((Hashtable) directory.get(seller)).get(book);
					        	if (price < budget) {
					        		candidate = seller;
					        		System.out.println("Price of "+book+" is: "+price);
					        		break;
					        	}
					        }
				        }
				        if (candidate == null) {

							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.REFUSE);
							reply.setContent("");					
							myAgent.send(reply);
							
				        }
					}
					catch (NumberFormatException e) {
					
						// refuse
					}
				}
				
				
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.CONFIRM);
				reply.setContent("");					
				myAgent.send(reply);
				
				
			} else {
				block();
			}
			

		}
	}  // End of inner class BookSubmissionServer


}
