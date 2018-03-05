/*
 * State machine to recognize a subsequence in a sequence of chars fed into it
 * sequentially.
 * @author Ingo Macherius
 */
package net.sf.repbot.util;

public class CharStateMachine {

	final private char[] sequenceToDetect;
	private int state = 0;
	protected boolean isFinalState = false;

	/*
	 */

	public CharStateMachine(char[] sequence) {
		this.sequenceToDetect = sequence;
	}

	public CharStateMachine(String sequence) {
		this(sequence.toCharArray());
	}
	
	/*
	 * @param nextSymbol The next char in the sequence
	 * 
	 * @return true if the state machine reached a final state, false otherwise
	 */

	public boolean transition(char nextSymbol) {
		if (isFinalState) {
			throw new IllegalStateException("Already in a final state.");
		}
		if (nextSymbol == sequenceToDetect[state++]) {
			isFinalState = state == sequenceToDetect.length;
		} else {
			state = 0;
			isFinalState = false;
		}
		perCharCallback(nextSymbol);
		return isFinalState;
	}

	/*
	 * Whether or not the last transition lead to a final state of the FSM. The next
	 * transition will reset the state again.
	 * 
	 * @return true if the
	 */
	public boolean isFinalState() {
		return isFinalState;
	}
	
	protected void perCharCallback(char c) {
		// nothing in the base class
	}

}
