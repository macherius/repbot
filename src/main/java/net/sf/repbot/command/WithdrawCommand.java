package net.sf.repbot.command; 

import javax.inject.Inject;
import javax.inject.Singleton;

import net.sf.repbot.db.*;
import net.sf.repbot.server.Server;
import net.sf.repbot.server.command.*;

@Singleton
public class WithdrawCommand extends AbstractNotificationPeerSelfCommand {


	@Inject
    public WithdrawCommand(Server server, UserDB db) {
        super(server, db);
    }

    @Override
    protected void onPeerInfo(Whois.Info from, String about, String rest) {
        try {
            db.withdraw(from.getName(), about);
            server.tell(from.getName(), 
                        "withdraw successful");
			notifyAboutPlayer(from.getName(), about);
        } catch (NoOpinionException e) {
            server.tell(from.getName(), 
                "You have no complaint against or voucher for " + about);
        } catch (Exception e) {
            e.printStackTrace();
            server.tell(from.getName(), 
                        "Internal error");
        }
    }

	@Override
	protected String getNotifyAboutPlayerMessage(String from) {
		return from + " has withdrawn his opinion about you.";
	}
}

