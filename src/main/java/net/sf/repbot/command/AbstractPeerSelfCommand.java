package net.sf.repbot.command;

import net.sf.repbot.db.UserDB;
import net.sf.repbot.server.Server;
import net.sf.repbot.server.command.*;

/** A command requiring both self and peer info.
 *
 * @author  avi
 */
public abstract class AbstractPeerSelfCommand extends AbstractPeerCommand {
    
    /** Creates a new instance of PeerSelfCommand */
    public AbstractPeerSelfCommand(Server server, UserDB db) {
        super(server, db);
    }
    
	@Override
    protected void onPeerInfo(String from, final String about, final String rest) {
        server.whois(from, new Whois.ReplyListener() {
            public void onReply(String name, Whois.Info info) {
                onPeerInfo(info, about, rest);
            }
            public void onNoUser(String who) {
                throw new RuntimeException("Can't get info on caller: " + who);
            }
            public void onTimeout(String who) {
            }
        });
        
    }
    
    protected abstract void onPeerInfo(Whois.Info from, String about, String rest);
    
}
