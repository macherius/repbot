package net.sf.repbot.user;

import java.time.Duration;
import net.sf.repbot.db.UserDBException;

/**
 * A round-robin reaper queue
 *
 * @author Avi Kivity
 */
public class RoundRobinGarbageCollector extends AbstractUserGarbageCollector {

	public RoundRobinGarbageCollector(UserManager userManager, Duration delay) {
		super(userManager, delay);
	}

	@Override
	public String getNextCandidate() throws UserDBException {
		return reaper.getDB().getLastVerifiedUser();
	}
}
