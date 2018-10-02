

package edu.marketplace;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class BookSellerAgent extends Agent {
	protected void setup() {		
		System.out.println("Seller agent "+getAID().getName()+" is ready.");
	}

	protected void takeDown() {
		System.out.println("Seller agent "+getAID().getName()+" terminating.");
	}
}