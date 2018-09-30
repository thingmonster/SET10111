
package edu.ex5;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.FSMBehaviour;

public class FSMAgent extends Agent {
	
	private static final String ASLEEP = "ASLEEP";
	private static final String AWAKE = "AWAKE";
	private static final String EATING = "EATING";
	private static final String DEAD = "DEAD";
	private static final String STATE_E = "E";
	private static final String STATE_F = "F";
	
	protected void setup() {
		FSMBehaviour fsm = new FSMBehaviour(this) {
			public int onEnd() {
				System.out.println("FSM behaviour completed.");
				myAgent.doDelete();
				return super.onEnd();
			}
		};
		
		fsm.registerFirstState(new RandomGenerator(3), ASLEEP);
		fsm.registerState(new RandomGenerator(4), AWAKE);
		fsm.registerState(new RandomGenerator(2), EATING);
		fsm.registerLastState(new NamePrinter(), DEAD);

		// Register the transitions

		fsm.registerDefaultTransition(ASLEEP, ASLEEP);
		fsm.registerDefaultTransition(AWAKE, AWAKE);
		fsm.registerDefaultTransition(EATING, AWAKE);
		
		fsm.registerTransition(ASLEEP, AWAKE, 1);
		fsm.registerTransition(AWAKE, ASLEEP, 1);
		fsm.registerTransition(AWAKE, EATING, 2);
		
		fsm.registerTransition(ASLEEP, DEAD, 2);
		fsm.registerTransition(AWAKE, DEAD, 3);
		fsm.registerTransition(EATING, DEAD, 1);

		addBehaviour(fsm);
	}
	
	private class NamePrinter extends OneShotBehaviour {
		public void action() {
			System.out.println("Executing behaviour "+getBehaviourName());
		}
	}
	
	private class RandomGenerator extends NamePrinter {
		private int maxExitValue;
		private int exitValue;
		
		private RandomGenerator(int max) {
			super();
			maxExitValue = max;
		}
		
		public void action() {
			System.out.println("Executing behaviour "+getBehaviourName());
			exitValue = (int) (Math.random() * maxExitValue);
			System.out.println("Exit value is "+exitValue);
		}
		
		public int onEnd() {
			return exitValue;
		}
	}
}