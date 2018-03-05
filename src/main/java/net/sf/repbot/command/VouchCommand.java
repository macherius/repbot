package net.sf.repbot.command;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.sf.repbot.db.*;
import net.sf.repbot.server.Server;
import net.sf.repbot.server.command.*;

// This command gets queued by the VouchFibsAction, and revisited by each
// ExperienceFibsAction. If it has enough information to finish, it does.
@Singleton
public class VouchCommand extends AbstractNotificationPeerSelfCommand {

	@Inject
	public VouchCommand(Server server, UserDB db) {
		super(server, db);
	}

	@Override
	protected void onPeerInfo(Whois.Info from, String about, String rest) {
		final String name = from.getName();
		try {
			if (name.equals(about)) {
				server.tell(name, "You can't vouch for yourself");
			} else if (db.hasRecentMatchEvent(name, about)) {
				db.vouch(name, from.getExperience(), about);
				server.tell(name, "voucher registered");
				notifyAboutPlayer(name, about);
			} else {
				server.tell(name, "You have no recent match with " + about + ", voucher denied.");
			}
		} catch (AlreadyVouchedException ace) {
			server.tell(name, "You already vouched for this user");
		} catch (AlreadyComplainedException ace) {
			server.tell(name, "You already complained this user");
		} catch (UserDBException e) {
			e.printStackTrace();
			server.tell(name, "Internal error, oops");
		}
	}

	@Override
	protected String getNotifyAboutPlayerMessage(String from) {
		return from + " has vouched for you.";
	}
}
