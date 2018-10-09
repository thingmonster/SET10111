

package edu.sealedbid;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class AuctioneerAgent extends Agent {

	private Hashtable catalogue;
	
	protected void setup() {

		Object[] args = getArguments();
		if (args != null && args.length > 0) {
			catalogue = (Hashtable<String, Float>) args[0];
			System.out.println(catalogue.toString());
		}
	}

	protected void takeDown() {
		
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		System.out.println("Seller-agent "+getAID().getName()+" terminating.");
	}
}