package net.sf.repbot.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class DisclaimerAppender  {

	private final static float DISCLAIMER_OUTPUT_PROBABILITY = 0.1f;
	private final static Random rng = new Random();
	
	final private static List<String> disclaimer;

	private DisclaimerAppender() {};
	
	static {
		disclaimer = loadTextFromHelpProperties("disclaimer");
	}

	private static List<String> loadTextFromHelpProperties(String topic) {
		Properties props = new Properties();
		ArrayList<String> reply = new ArrayList<>();
		try (InputStream is = DisclaimerAppender.class.getResourceAsStream("/help.properties")){
			props.load(is);
		} catch (IOException e) {
			reply.add("Internal error, failed to load resource '/help.properties'.");
			return reply;
		}
		int line = 1;
		String key = topic + ".1";
		while (props.get(key) != null) {
			reply.add(props.getProperty(key));
			key = topic + '.' + Integer.toUnsignedString(++line);
		}
		reply.trimToSize();
		return Collections.unmodifiableList(reply);
	}
	
	public static List<String> appendDisclaimerRandomly(List<String> messages) {
		if (rng.nextFloat() < DISCLAIMER_OUTPUT_PROBABILITY) {
			messages.addAll(disclaimer);
		}
		return messages;
	}

	
}
