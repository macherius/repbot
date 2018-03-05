package net.sf.repbot.command;

import net.sf.repbot.db.UserDB;
import net.sf.repbot.server.Server;
import net.sf.repbot.util.ListFormatterMulti;
import net.sf.repbot.util.ListFormatter;
import net.sf.repbot.util.DisclaimerAppender;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Singleton;

// Someone asks RepBot about someone's details. This queues the command,
// sends the necessary whois commands to FIBS and responds to the user that
// the processing is started.
@Singleton
public class ListCommand extends AbstractPeerCommand {

	@Inject
	public ListCommand(Server server, UserDB db) {
		super(server, db);
	}

	// info will be experience (integer) as a String, or null if no user found
	@Override
	protected void onPeerInfo(String from, String who, String rest) {
		List<String> replies = new ArrayList<>();
		try {
			List<String> complainersUsers = db.getComplainers(who);
			List<String> vouchersUsers = db.getVouchers(who);
			List<String> complaintsUsers = db.getComplaints(who);
			List<String> vouchesUsers = db.getVouches(who);

			if (complainersUsers.isEmpty() && vouchersUsers.isEmpty()
					&& complaintsUsers.isEmpty() && vouchesUsers.isEmpty()) {
				replies.add(who + " has no complainers or vouchers");
			} else {

				List<ListFormatter> lists = new ArrayList<>(4);
				lists.add(new ListFormatter(complainersUsers, who,
						"complainers"));
				lists.add(new ListFormatter(vouchersUsers, who, "vouchers"));
				lists.add(new ListFormatter(complaintsUsers, who, "complaints"));
				lists.add(new ListFormatter(vouchesUsers, who, "vouches"));

				replies.addAll(new ListFormatterMulti(lists).toList());
			}
		} catch (Exception e) {
			e.printStackTrace();
			server.tell(from, "ERROR: Can't produce list for " + who + ".");
		}
		String botstr = getBotString(who);
		if (botstr != null)
			replies.add(botstr);
		DisclaimerAppender.appendDisclaimerRandomly(replies);
		server.tell(from, replies);
	}
}
