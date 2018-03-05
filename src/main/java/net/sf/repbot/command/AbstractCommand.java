/*
 * Base class for all commands
 * @author inim
 * @since 20130218
 */

package net.sf.repbot.command;

import net.sf.repbot.db.UserDBException;
import net.sf.repbot.server.Server;
import net.sf.repbot.db.UserDB;

public abstract class AbstractCommand implements Command {

	protected Server server;
	protected UserDB db;

	public AbstractCommand(Server server, UserDB db) {
		this.server = server;
		this.db = db;
	}

	@Override
	public abstract void execute(String user, String command, String[] arguments);
	
    /** Gets a descriptive string mentioning that the player is a bot, or null
     *  if they ain't. */
	protected String getBotString(String who) {
		try {
			return db.isBot(who) ? "Note: " + who + " is a bot." : null;
		} catch (UserDBException udbe) {
			return null;
		}
	}
}
