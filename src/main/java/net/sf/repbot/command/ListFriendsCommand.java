package net.sf.repbot.command;

import net.sf.repbot.db.FriendsOpinion;
import net.sf.repbot.db.UserDB;
import net.sf.repbot.server.Server;
import net.sf.repbot.server.command.Tell;
import net.sf.repbot.util.ListFormatter;
import net.sf.repbot.util.ListFormatterMulti;
import net.sf.repbot.util.DisclaimerAppender;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ListFriendsCommand extends AbstractPeerCommand {

	@Inject
	public ListFriendsCommand(Server server, UserDB db) {
		super(server, db);
	}

	@Override
	protected void onPeerInfo(String from, String who, String rest) {
		List<String> replies = new ArrayList<>();
		try {
			FriendsOpinion friendsOpinons = db.getFriendsOpinion(from, who);
			if (friendsOpinons.getComplainers().isEmpty()
					&& friendsOpinons.getVouchers().isEmpty()) {
				replies.add(who + " has no complainers or vouchers");
			} else {
				List<ListFormatter> lists = new ArrayList<>(2);
				lists.add(new ListFormatter(friendsOpinons.getComplainers(),
						who, "complainers"));
				lists.add(new ListFormatter(friendsOpinons.getVouchers(), who,
						"vouchers"));
				replies.addAll(new ListFormatterMulti(lists).toList());
			}
		} catch (Exception e) {
			e.printStackTrace();
			server.tell(from, "ERROR: Can't produce friends for " + who + ".",
					new Tell.DefaultReplyListener());
		}
		String botstr = getBotString(who);
		if (botstr != null)
			replies.add(botstr);
		DisclaimerAppender.appendDisclaimerRandomly(replies);
		server.tell(from, replies);
	}
}
