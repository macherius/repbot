package net.sf.repbot.command; 

import net.sf.repbot.util.DisclaimerAppender;
import net.sf.repbot.db.*;
import net.sf.repbot.server.Server;
import java.util.*;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AskCommand extends AbstractPeerCommand {
	
	@Inject
    public AskCommand(Server server, UserDB db) {
        super(server, db);
    }
    
	@Override
	protected void onPeerInfo(String from, String about, String rest) {
        respondPositive(from, about);
    }

    private void respondPositive(String from, String who) {
        List<String> replies = new ArrayList<>();
        try {
            String reply;
            try {
            	// 20130205 change as requested by Patti. Bots should not be subject to opinions and default to 0 reputation
                int rep = db.isBot(who) ? 0 : db.getReputation(who);
                reply = who + "'s reputation is " + rep + (rep < 0 ? " (BAD)" : " (GOOD)");
            } catch (NoOpinionException e) {
                reply = "User " + who + " has no complainers or vouchers";
            }
            replies.add(reply);
            String botstr = getBotString(who);
            if (botstr != null)
                replies.add(botstr);
        } catch (UserDBException e) {
            e.printStackTrace();
            replies.clear();
            replies.add("Internal error, my apologies");
        }
        DisclaimerAppender.appendDisclaimerRandomly(replies);
        server.tell(from, replies);
    }
}
