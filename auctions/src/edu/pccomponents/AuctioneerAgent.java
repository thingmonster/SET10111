

package edu.pccomponents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class AuctioneerAgent extends Agent {

	private ArrayList<Component> catalogue = new ArrayList();
	private List<AID> bidders = new ArrayList();
	private AuctioneerGui gui;
	private String currentItem = null;
	private Float currentPrice = null;
	private Hashtable <AID, Float> bids = new Hashtable();
	
	protected void setup() {

		gui = new AuctioneerGui(this);
		gui.showGui();
	
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("book-auction");
		sd.setName("JADE-book-auction");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
			System.out.println("Auctioneer Registered");
		}
		catch (FIPAException fe) {
			System.out.println("Registration failed");
			fe.printStackTrace();
		}

	}

	protected void start() {
		addBehaviour(new LoadCSV());
		addBehaviour(new RegistrationServer());
		addBehaviour(new BidServer());
		addBehaviour(new Pause(this, 3000));
	}
	
	protected void takeDown() {
		

		ACLMessage cancel = new ACLMessage(ACLMessage.CANCEL);
    	for (int i = 0; i < bidders.size(); i++) {
    		cancel.addReceiver(bidders.get(i));
    	}
		this.send(cancel);
		
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}

		gui.dispose();
		
		System.out.println("Seller-agent "+getAID().getName()+" terminating.");
	}
	
	private class LoadCSV extends OneShotBehaviour {

		// code from here:
		// https://www.mkyong.com/java/how-to-read-and-parse-csv-file-in-java/
		
		public void action() {

	        String csvFile = "src/edu/resources/components.csv";
	        BufferedReader br = null;
	        String line = "";
	        String cvsSplitBy = ",";
	        boolean headings = true;

	        try {

	            br = new BufferedReader(new FileReader(csvFile));
	            while ((line = br.readLine()) != null) {

	            	if (!headings) {
		                String[] book = line.split(cvsSplitBy);
		                Component c = new Component(book[1], Float.parseFloat(book[2]));
		                catalogue.add(c);
	            	} else {
	            		headings = false;
	            	}
	            }

	        } catch (FileNotFoundException e) {
	            e.printStackTrace();
	        } catch (IOException e) {
	            e.printStackTrace();
	        } finally {
	            if (br != null) {
	                try {
	                    br.close();
	                } catch (IOException e) {
	                    e.printStackTrace();
	                }
	            }
	        }

		}
		
	}

	private class RegistrationServer extends CyclicBehaviour {
		public void action() {
			
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				
				String message = msg.getContent();
				bidders.add(msg.getSender());
				System.out.println(bidders.toString());
			}
			else {
				block();
			}
		}
	}
	
	private class Pause extends WakerBehaviour {

		public Pause(Agent a, long timeout) {
			super(a, timeout);
		}

		protected void handleElapsedTimeout() {
			
			addBehaviour(new ChooseBook());
			

        }
		
	}

	private class ChooseBook extends OneShotBehaviour {
		public void action() {

			if (catalogue.size() > 0) {
					
				currentItem = catalogue.get(0).getName();
	    		System.out.println("Auction Starting for "+currentItem);	    		
		        addBehaviour(new Auction(myAgent, 1500));
		        
	        } else {
	        	
	        	System.out.println("Auction Finished");
	        	myAgent.doDelete();
	        }
	        	
		}
	}

	private class BidServer extends CyclicBehaviour {

		MessageTemplate mt;
		ACLMessage msg;
		
		public void action() {

			mt = MessageTemplate.and(MessageTemplate.MatchConversationId(currentItem),
					MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
			
			msg = myAgent.receive(mt);
			if (msg != null) {

//				System.out.println("Bid received from " + msg.getSender().getLocalName());
				Float bid = Float.parseFloat(msg.getContent());
				bids.put(msg.getSender(), bid);

			} else {
				block();
			}
		}
		
	}
	
	private class Auction extends TickerBehaviour {

		ACLMessage msg;
		AID leader; 
		
		public Auction(Agent a, long period) {
			super(a, period);
			inform();
			solicit();
		}
		
		private void inform() {
			
			leader = null;
			bids.clear();
			
			ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
        	for (int i = 0; i < bidders.size(); i++) {
        		inform.addReceiver(bidders.get(i));
        	}
        	inform.setContent(currentItem);
        	inform.setConversationId(currentItem);
        	inform.setReplyWith("request"+System.currentTimeMillis()); // Unique value
			myAgent.send(inform);
		}

		private void solicit() {

			ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
        	for (int i = 0; i < bidders.size(); i++) {
        		cfp.addReceiver(bidders.get(i));
        	}
        	cfp.setContent(Float.toString(catalogue.get(0).getPrice()));
        	cfp.setConversationId(currentItem);
        	cfp.setReplyWith("request"+System.currentTimeMillis()); // Unique value
			myAgent.send(cfp);
		}
				
		public void onTick() {
					
			Float max = (float) 0; 
			AID maxBidder = null;
			Set<AID> b = bids.keySet();
	        for(AID bidder: b) {
	        	if (bids.get(bidder) > max) {
	        		max = bids.get(bidder);
	        		maxBidder = bidder;
	        	}
	        }
	        
							
			if (bids.size() > 1) {
				
				if (max > (Float) catalogue.get(0).getPrice()) {

					catalogue.get(0).setPrice(max);
					System.out.println("New price: "+max);
					leader = maxBidder;
					bids.clear();
					solicit();
					
				}
				
			}
			
			else if (bids.size() == 1) {
				
				System.out.println("book '" + currentItem +"' won by "+ maxBidder.getLocalName());
				
				ACLMessage cfp = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
	        	cfp.addReceiver(maxBidder);
	        	cfp.setContent(currentItem);
	        	cfp.setReplyWith("request"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);

				catalogue.remove(0);
				currentItem = null;

				myAgent.addBehaviour(new ChooseBook());
				myAgent.removeBehaviour(this);

				
			}
			
			else if (bids.size() == 0) {
				
				if (leader != null) {
	
					System.out.println("Component '" + currentItem +"' won by "+ leader.getLocalName());

					ACLMessage cfp = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
		        	cfp.addReceiver(leader);
		        	cfp.setContent(currentItem);
		        	cfp.setReplyWith("request"+System.currentTimeMillis()); // Unique value
					myAgent.send(cfp);


				} else {
					System.out.println("book '" + currentItem +"' cancelled");
				}

				
				catalogue.remove(0);
				currentItem = null;
				myAgent.addBehaviour(new ChooseBook());
				myAgent.removeBehaviour(this);

			}
				

				
		}
	}
}