package net.sf.repbot.command;

import net.sf.repbot.db.UserDBException;
import net.sf.repbot.server.Server;
import net.sf.repbot.db.UserDB;

public abstract class AbstractNotificationPeerSelfCommand extends AbstractPeerSelfCommand {

    /** Creates a new instance of PeerSelfCommand */
    public AbstractNotificationPeerSelfCommand(Server server, UserDB db) {
        super(server, db);
    }

    /**
     * Checks whether a player who was complained/vouched/withdrawn about/for/from wants to be notified of that,
     * and sends a message if this is the case.
     * @param from The player who cast the opinion
     * @param about The player about whom an opinion was cast
     */
    protected void notifyAboutPlayer(String from, String about) {
    	try {
			if (db.getAlert(about)) {
				server.message(about, getNotifyAboutPlayerMessage(from));
			}
		} catch (UserDBException e) {
			// FIXME: handle somehow
			e.printStackTrace();
		}
    }
    
    protected abstract String getNotifyAboutPlayerMessage(String from);	
}
