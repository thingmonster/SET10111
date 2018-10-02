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

public class BookBuyerAgent extends Agent {
	protected void setup() {		
		System.out.println("Buyer agent "+getAID().getName()+" is ready.");
	}

	protected void takeDown() {
		System.out.println("Buyer agent "+getAID().getName()+" terminating.");
	}
}