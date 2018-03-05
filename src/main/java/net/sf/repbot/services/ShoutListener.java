package net.sf.repbot.services;

import net.sf.repbot.server.Monitor;
import net.sf.repbot.server.Server;

import java.util.regex.*;

import javax.inject.Inject;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.Instant;

/** Lets RepBot join shout conversations on fibs.
 *
 *  @author  Avi Kivity
 */
public class ShoutListener extends Monitor.DefaultListener {
    
    private static final Duration quietTime = Duration.ofHours(6);
    private static final String RESSOURCE_PATH = "/conversation.txt";
        
    private Pattern repbot = Pattern.compile("\\brepbot\\b", Pattern.CASE_INSENSITIVE);
    private Instant wakeup = Instant.now();
    private Random random = new Random();
    private List<String> messages = loadMessages();
    
    @Inject
    Server server;
    @Inject
    Logger logger;
    @Inject
    Executor cron;

    /** Creates a new instance of Conversation */
    public ShoutListener() {
        messages = loadMessages();
    }

    /** Checks if a shout is about repbot and outside the quiet period, and,
     *  if so, replies after a "natural" delay. */
	@Override
	public void onShout(final String user, final String message) {
		if (Instant.now().isBefore(wakeup)) {
			return;
		}
		if (repbot.matcher(message).find()) {
			wakeup = Instant.now().plus(quietTime);
			cron.addTimeout(
					// TimeoutListener as closure
					() -> {
						reply(user);
					}, 2L, TimeUnit.SECONDS);
		}
	}
    
    /** Shouts a canned response. */
    private void reply(String user) {
        String reply = messages.get(random.nextInt(messages.size()));
        reply = reply.replaceAll("\\$", user);
        server.shout(reply);
    }
    
	private List<String> loadMessages() {
		try (BufferedReader r = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream(RESSOURCE_PATH), Charset.forName("US-ASCII")));) {
			List<String> messages = new ArrayList<>();
			String line;
			while ((line = r.readLine()) != null) {
				if (line.matches("#.*|\\s*"))
					continue;
				messages.add(line);
			}
			return messages;
		} catch (IOException e) {
			logger.logTimestampedLine("Failed reading " + RESSOURCE_PATH + "from jar.", e);
			return Collections.singletonList("Me RepBot, you $!");
		}
	}    
}
