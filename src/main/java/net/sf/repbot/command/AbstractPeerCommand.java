package net.sf.repbot.command;

import net.sf.repbot.db.UserDB;
import net.sf.repbot.server.Server;
import net.sf.repbot.server.command.Spell;

/** A command requiring information about a peer.
 *
 *  @author Avi Kivity
 */
public abstract class AbstractPeerCommand extends AbstractCommand {
    
    /** Creates a new instance of PeerCommand */
    public AbstractPeerCommand(Server server, UserDB db) {
    	super(server, db);
    }
     
    /** Executes the command.
     *  @param user  command requestor
     *  @param arguments capture groups from the pattern.  */
	@Override
    public void execute(final String from, final String command, String[] arguments) {
        if (arguments.length != 1) {
            server.tell(from, 
                        "ERROR: try 'tell RepBot " + command + " <username>'");
            return;
        }
        final String about = arguments[0];
        server.spell(about, new Spell.ReplyListener() {
            public void onReply(String name, String spelledName) {
                onPeerInfo(from, spelledName, "");
            }
            public void onNoUser(String who) {
                respondNegative(from, about, "");
            }
            public void onTimeout(String who) {
            }
        });
    }

    protected void respondNegative(String from, String about, String rest) {
        server.tell(from, "ERROR: user " + about + " does not exist on FIBS.");
    }

    protected abstract void onPeerInfo(String from, String about, String rest);
}
