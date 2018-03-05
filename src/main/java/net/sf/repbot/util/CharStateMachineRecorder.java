package net.sf.repbot.util;

public class CharStateMachineRecorder extends CharStateMachine {
	
	StringBuilder recorder = new StringBuilder(1024);

	public CharStateMachineRecorder(char[] sequence) {
		super(sequence);
	}

	public CharStateMachineRecorder(String sequence) {
		super(sequence);
	}

	@Override
	protected void perCharCallback(char c) {
		recorder.append(c);
	}
	
	public String getRecording() {
		if (!isFinalState) {
			throw new IllegalStateException("Sequence to search ssnot detected.");
		}
		return recorder.toString();
	}
}
