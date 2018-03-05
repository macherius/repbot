package net.sf.repbot.user;

import java.time.Duration;
import net.sf.repbot.db.UserDBException;

/**
 * A reaper queue for non zero experience players
 *
 * @author Avi Kivity
 */
public class NonZeroExpGarbageCollector extends AbstractUserGarbageCollector {

	public NonZeroExpGarbageCollector(UserManager userManager, Duration delay) {
		super(userManager, delay);
	}

	@Override
	public String getNextCandidate() throws UserDBException {
		return reaper.getDB().getLastLogoutNonzeroExpUser();
	}
}
