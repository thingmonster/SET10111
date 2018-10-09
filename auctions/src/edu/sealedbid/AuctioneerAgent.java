

package edu.sealedbid;

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

	private Hashtable catalogue = new Hashtable();
	private List<AID> bidders = new ArrayList();
	private AuctioneerGui gui;
	
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
		addBehaviour(new Pause(this, 3000));
	}
	
	protected void takeDown() {
		
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

	        String csvFile = "src/edu/resources/catalogue.csv";
	        BufferedReader br = null;
	        String line = "";
	        String cvsSplitBy = ",";

	        try {

	            br = new BufferedReader(new FileReader(csvFile));
	            while ((line = br.readLine()) != null) {

	                // use comma as separator
	                String[] book = line.split(cvsSplitBy);
	                catalogue.put(book[0], book[1]);

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
			
			addBehaviour(new Auction());
			

        }
		
	}

	private class Auction extends Behaviour {
		
		private int step = 0;
		private Hashtable <AID, Float> bids = new Hashtable();
		private String currentItem = null;
		ACLMessage msg;
		MessageTemplate mt;
		
		public void action() {
			switch(step) {
			case 0:

				Set<String> keys = catalogue.keySet();
		        for(String key: keys){
		        	
	    			System.out.println("Auction Starting");
	    			
	    			currentItem = key;

					ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
		        	for (int i = 0; i < bidders.size(); i++) {
		        		cfp.addReceiver(bidders.get(i));
		        	}
		        	cfp.setContent(key);
		        	cfp.setReplyWith("request"+System.currentTimeMillis()); // Unique value
	    			myAgent.send(cfp);
					
	    			break;
		        }
		        
		        step = 1;
		        break;
			
			case 1:
				
				mt = MessageTemplate.MatchPerformative(ACLMessage.PROPOSE);
				msg = myAgent.receive(mt);
				if (msg != null) {
					
					Float bid = Float.parseFloat(msg.getContent());
					bids.put(msg.getSender(), bid);

				}
				else {
				
					mt = MessageTemplate.MatchPerformative(ACLMessage.REFUSE);
					msg = myAgent.receive(mt);
					if (msg != null) {
						
						bids.put(msg.getSender(), (float) 0);
					} 
					else {
						
						block();
					}					
				}
				
				if (bids.size() == bidders.size()) {
					
					Float max = (float) 0; 
					AID maxBidder = null;
					Set<AID> b = bids.keySet();
			        for(AID bidder: b) {
			        	if (bids.get(bidder) > max) {
			        		max = bids.get(bidder);
			        		maxBidder = bidder;
			        	}
			        }
			        
					if (max > 0) {

						ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
						accept.addReceiver(maxBidder);
						accept.setContent(currentItem);
		    			myAgent.send(accept);

					}

					System.out.println("removing "+currentItem+" from catalogue\n");
					catalogue.remove(currentItem);
					bids.clear();						
					
					if (catalogue.isEmpty()) {
						step = 2;
					} else {
						step = 0;
					}

				}
				
				break;
			}
		}
	
		@Override
		public boolean done() {
			
			if (step == 2) {
				myAgent.doDelete();
			}
			return (step == 2);
			
		}
	}
}