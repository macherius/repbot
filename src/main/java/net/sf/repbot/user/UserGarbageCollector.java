package net.sf.repbot.user;

import net.sf.repbot.db.UserDBException;
import net.sf.repbot.server.TimeoutListener;
import net.sf.repbot.services.Logger;

/**
 * Interface for selecting a candidate to be reaped.
 *
 * @author Avi Kivity
 */

interface UserGarbageCollector extends TimeoutListener {

	void schedule();

	Logger getLogger();

	String getNextCandidate() throws UserDBException;

	default String poll() {
		try {
			return getNextCandidate();
		} catch (UserDBException e) {
			getLogger().logTimestampedLine(
					getClass().getCanonicalName() + " failed to get next reaping candidate from DB", e);
			return null;
		}
	}

}
