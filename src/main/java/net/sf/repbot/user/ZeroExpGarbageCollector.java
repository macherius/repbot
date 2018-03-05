package net.sf.repbot.user;

import java.time.Duration;
import net.sf.repbot.db.UserDBException;

/**
 * A reaper queue for zero experience players
 *
 * @author Avi Kivity
 */
public class ZeroExpGarbageCollector extends AbstractUserGarbageCollector {

	public ZeroExpGarbageCollector(UserManager userManager, Duration delay) {
		super(userManager, delay);
	}

	@Override
	public String getNextCandidate() throws UserDBException {
		return reaper.getDB().getLastLogoutZeroExpUser();
	}

}
