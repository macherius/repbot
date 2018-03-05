package net.sf.repbot.command;

import javax.inject.Inject;
import javax.inject.Singleton;

import net.sf.repbot.db.*;
import net.sf.repbot.server.Server;
import net.sf.repbot.server.command.*;

@Singleton
public class ComplainCommand extends AbstractNotificationPeerSelfCommand {

	@Inject
	public ComplainCommand(Server server, UserDB db) {
		super(server, db);
	}

	@Override
	protected void onPeerInfo(Whois.Info from, String about, String rest) {
		final String name = from.getName();
		try {
			if (name.equals(about)) {
				server.tell(name, "You can't complain about yourself");
			} else if (db.hasRecentMatchEvent(from.getName(), about)) {
				db.complain(name, from.getExperience(), about);
				server.tell(name, "complaint registered");
				notifyAboutPlayer(name, about);
			} else {
				server.tell(name, "You have no recent match with " + about + ", complaint denied.");
			}
		} catch (AlreadyComplainedException ace) {
			server.tell(name, "You have already complained about this user");
		} catch (AlreadyVouchedException ace) {
			server.tell(name, "You have already vouched for this user");
		} catch (UserDBException e) {
			e.printStackTrace();
			server.tell(name, "Internal error, sorry");
		}
	}

	@Override
	protected String getNotifyAboutPlayerMessage(String from) {
		return from + " has complained about you.";
	}
}
