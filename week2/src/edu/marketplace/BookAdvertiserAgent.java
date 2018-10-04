package edu.marketplace;

import java.io.IOException;
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
	
	private Hashtable<String, Hashtable<AID, Float>> directory;
	
	protected void setup() {	
		
		System.out.println("Advertiser agent "+getAID().getName()+" is ready.");
		
		directory = new Hashtable<String, Hashtable<AID, Float>>();
		
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
		this.addBehaviour(new BookRemovalServer());
	}

	protected void takeDown() {
		System.out.println("Advertiser agent "+getAID().getName()+" terminating.");
	}

	private class BookSubmissionServer extends CyclicBehaviour {

		MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
		ACLMessage msg;
		
		Hashtable<AID, Hashtable<String, String>> conversations = new Hashtable<AID, Hashtable<String, String>>();
		
		public void action() {

			msg = myAgent.receive(messageTemplate);
			
			if (msg != null) {
				
				// get seller ID
				AID seller = msg.getSender();
				if (!conversations.containsKey(seller)) {
					conversations.put(seller, new Hashtable<String, String>());
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
				    if (((Hashtable<String, String>) conversations.get(seller)).containsKey(conversationID)) {
				    	
				    	// save title and price to directory
				    	String title = (String) ((Hashtable<String, String>) conversations.get(seller)).get(conversationID);
						if (!directory.containsKey(title)) {
							directory.put(title, new Hashtable<AID, Float>());
						}						
						((Hashtable<AID, Float>) directory.get(title)).put(seller, price);	
				    	System.out.println("price received, sending confirmation");					
						reply.setPerformative(ACLMessage.CONFIRM);				
						
				    } else {	
				    	
				    	// or refuse to accept price because title has not been submitted
				    	System.out.println("price received, title has not been submitted");
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
				    
				    ((Hashtable<String, String>) conversations.get(seller)).remove(conversationID);

//				    System.out.println("");
//				    System.out.println("Advertiser conversations now looks like this:");
//				    System.out.println(conversations.toString());
//				    System.out.println("");
				    
				} catch (NumberFormatException e) {
					
					// if message isn't a number it must be a title
//					System.out.println("Advertiser agent "+getAID().getName()+" received book: "+submission);
					
					// add title and conversation ID to conversations hash table
					if (!conversations.containsKey(seller)) {
						Hashtable<String, String> books = new Hashtable<String, String>();
						books.put(conversationID, submission);
						conversations.put(seller, books);
					} else { 
						((Hashtable<String, String>) conversations.get(seller)).put(conversationID, submission);
					}

			    	System.out.println("title received");
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
		
		private Hashtable<AID, Hashtable<String, String>> conversations = new Hashtable<AID, Hashtable<String, String>>();

		MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		ACLMessage msg;
		
		public void action() {

			msg = myAgent.receive(messageTemplate);
			
			if (msg != null) {
				
				String submission = msg.getContent();
				String conversationID = msg.getConversationId();
				AID sender = msg.getSender();
				
				if (!conversations.containsKey(sender)) {
					conversations.put(sender, new Hashtable<String, String>());
				}
				
				if (!((Hashtable<String, String>) conversations.get(sender)).containsKey(conversationID)) {
					
					// add this conversation and title to seller's entry in hashtable if necessary
					((Hashtable<String, String>) conversations.get(sender)).put(conversationID, submission);
					
				} else {
					
					try {
						
						// if message is a number search directory
						float budget = Float.parseFloat(submission);
						String title = conversations.get(sender).get(conversationID);

						AID candidate = null;
						
						if (directory.containsKey(title)) {
			        	Set<AID> sellers = ((Hashtable<AID, Float>) directory.get(title)).keySet();
					        for(AID seller: sellers){
					        	float price = (float) ((Hashtable<AID, Float>) directory.get(title)).get(seller);
					        	if (price < budget) {
						        	System.out.println("book found, sending AID");
					        		candidate = seller;
									ACLMessage reply = msg.createReply();
									reply.setPerformative(ACLMessage.INFORM);
									reply.setContentObject(seller);					
									myAgent.send(reply);
									
					        		break;
					        	}
					        }
						}
						
				        if (candidate == null) {

				        	System.out.println("book not found");
							ACLMessage reply = msg.createReply();
							reply.setPerformative(ACLMessage.REFUSE);
							reply.setContent("no matches");					
							myAgent.send(reply);
							
				        }
					}
					catch (NumberFormatException e) {

						// otherwise it must be a title so add to conversations
			        	System.out.println("not number, must be title");
						((Hashtable<String, String>) conversations.get(sender)).put(conversationID, submission);

					} catch (IOException e) {

			        	System.out.println("found a seller but can't send it");
						ACLMessage reply = msg.createReply();
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("found a seller but can't sent it");					
						myAgent.send(reply);

					}
				}
				
				
				
			} else {
				block();
			}
			

		}
	}  // End of inner class BookRequestServer

	private class BookRemovalServer extends CyclicBehaviour {

		MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
		ACLMessage msg;
		
		public void action() {

			msg = myAgent.receive(messageTemplate);
			
			if (msg != null) {
				
				String title = msg.getContent();
				AID sender = msg.getSender();
				boolean validRequest = false;
				
				if (directory.containsKey(title)) {
					if (directory.get(title).containsKey(sender)) {
						validRequest = true;
					}
				}
				
				if (validRequest) {
					System.out.println("removing "+ title +" sold buy " +sender.getName());
					directory.get(title).remove(sender);
					System.out.println("directory now looks like this:");
					System.out.println(directory.toString());
				}
			} else {
				block();
			}
		}
		
	}


}
