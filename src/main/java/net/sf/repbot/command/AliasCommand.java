/*
 * @author inim
 * @since 20130217 
 */

package net.sf.repbot.command;

//import java.util.Collection;
//import java.util.List;
//import java.util.regex.Pattern;
//
//import net.sf.repbot.db.UserDBException;
//import net.sf.repbot.db.UserDBInterface;
//import net.sf.repbot.server.Server;
//import net.sf.repbot.server.Spell;
//import net.sf.repbot.util.ListFormatter;
//import net.sf.repbot.util.RandomDisclaimerAppenderUtil;

public class AliasCommand {}

//public class AliasCommand extends AbstractCommand {
//
//	protected enum SubCommand {
//
//		LIST("l|li|lis|list", 0), LISTUSER("l|li|lis|list", 1), REMOVE(
//				"r|re|rem|remo|remov|remove", 1), ADD("a|ad|add", 2);
//
//		final private int userParametersCount;
//		final private Pattern regex;
//
//		SubCommand(String keywordRegex, int userParametersCount) {
//			this.userParametersCount = userParametersCount + 1;
//			this.regex = Pattern
//					.compile(keywordRegex, Pattern.CASE_INSENSITIVE);
//		}
//
//		public boolean matches(String[] arguments) {
//			if (arguments == null || arguments[0] == null
//					|| arguments.length != userParametersCount) {
//				return false;
//			}
//			return regex.matcher(arguments[0]).matches();
//		}
//	}
//
//	public AliasCommand(Server server, UserDBInterface db) {
//		super(server, db);
//	}
//
//	/*
//	 * Command dispatcher
//	 * 
//	 * @see net.sf.repbot.command.Command#execute(java.lang.String,
//	 * java.lang.String, java.lang.String[])
//	 */
//
//	@Override
//	public void execute(String replyToUser, String command, String[] arguments) {
//
//		SubCommand subcommand = null;
//		for (SubCommand sc : SubCommand.values()) {
//			if (sc.matches(arguments)) {
//				subcommand = sc;
//				break;
//			}
//		}
//
//		if (null == subcommand) {
//			reportErrorByTell(
//					replyToUser,
//					"Synopsis is "
//							+ command
//							+ " add <user> <anotheruser> | remove <user> | list <user> | list'");
//			return;
//		}
//
//		switch (subcommand) {
//		case LIST: {
//			onList(replyToUser);
//			break;
//		}
//		case REMOVE: {
//			onRemoveSpell(replyToUser, arguments[1]);
//			break;
//		}
//		case LISTUSER: {
//			onListUserSpell(replyToUser, arguments[1]);
//			break;
//		}
//		case ADD: {
//			if (arguments[1].equalsIgnoreCase(arguments[2])) {
//				reportErrorByTell(replyToUser, "User " + arguments[1]
//						+ " can not be aliased to himself.");
//				break;
//			}
//			onAddSpellOnce(replyToUser, arguments[1], arguments[2]);
//			break;
//		}
//		}
//
//	}
//
//	/*
//	 * Subcommand implementations
//	 */
//
//	/*
//	 * List
//	 */
//
//	protected void onList(String replyToUser) {
//		try {
//			for (Collection<String> aliasGroup : db.aliasList()) {
//
//			}
//		} catch (UserDBException e) {
//			reportDBError(replyToUser);
//			return;
//		}
//		reportByTell(replyToUser, "onList()");
//	}
//
//	/*
//	 * ListUser
//	 */
//
//	private void onListUserSpell(final String replyToUser, String user) {
//		server.spell(user, new Spell.ReplyListener() {
//			public void onReply(String name, String spelledName) {
//				onListUser(replyToUser, spelledName, name);
//			}
//
//			public void onNoUser(String who) {
//				onListUser(replyToUser, null, who);
//			}
//
//			public void onTimeout(String who) {
//				onListUser(replyToUser, null, who);
//			}
//		});
//	}
//
//	protected void onListUser(String replyToUser, String spelledUser,
//			String unspelledUser) {
//		if (null == spelledUser) {
//			reportNoSuchUser(replyToUser, unspelledUser);
//			return;
//		}
//		ListFormatter lf;
//		try {
//			lf = new ListFormatter(db.aliasList(spelledUser), spelledUser,
//					"aliases");
//		} catch (UserDBException udbe) {
//			udbe.printStackTrace();
//			reportDBError(replyToUser, spelledUser);
//			return;
//		}
//		List<String> replies = lf.toList();
//		RandomDisclaimerAppenderUtil.appendDisclaimerRandomly(replies);
//		server.tell(replyToUser, replies);
//	}
//
//	/*
//	 * Add
//	 */
//
//	private void onAddSpellOnce(final String replyToUser,
//			final String unspelled1, final String unspelled2) {
//		server.spell(unspelled1, new Spell.ReplyListener() {
//			public void onReply(String name, String spelledName) {
//				onAddSpellTwice(replyToUser, spelledName, unspelled2);
//			}
//
//			public void onNoUser(String who) {
//				onAdd(replyToUser, null, who);
//			}
//
//			public void onTimeout(String who) {
//				onAdd(replyToUser, null, who);
//			}
//		});
//	}
//
//	private void onAddSpellTwice(final String replyToUser,
//			final String spelledUser, final String unspelledUser) {
//		server.spell(unspelledUser, new Spell.ReplyListener() {
//			public void onReply(String name, String spelledName) {
//				onAdd(replyToUser, spelledUser, spelledName);
//			}
//
//			public void onNoUser(String who) {
//				onAdd(replyToUser, null, who);
//			}
//
//			public void onTimeout(String who) {
//				onAdd(replyToUser, null, who);
//			}
//		});
//
//	}
//
//	protected void onAdd(String replyToUser, String spelledUser1,
//			String spelledUser2) {
//		if (null == spelledUser1) {
//			reportNoSuchUser(replyToUser, spelledUser2);
//			return;
//		}
//		try {
//			db.aliasAdd(spelledUser1, spelledUser2, replyToUser);
//		} catch (UserDBException udbe) {
//			udbe.printStackTrace();
//			reportDBError(replyToUser, spelledUser1, spelledUser2);
//			return;
//		}
//		reportByTell(replyToUser, "onAdd(" + spelledUser1 + ", " + spelledUser2
//				+ ")");
//	}
//
//	/*
//	 * Remove
//	 */
//
//	private void onRemoveSpell(final String replyToUser, final String user) {
//		server.spell(user, new Spell.ReplyListener() {
//
//			public void onReply(String name, String spelledName) {
//				onRemove(replyToUser, spelledName, name);
//			}
//
//			public void onNoUser(String who) {
//				onRemove(replyToUser, null, who);
//			}
//
//			public void onTimeout(String who) {
//				onRemove(replyToUser, null, who);
//			}
//		});
//	}
//
//	protected void onRemove(String replyToUser, String spelledUser,
//			String unspelledUser) {
//		if (null == spelledUser) {
//			reportNoSuchUser(replyToUser, unspelledUser);
//			return;
//		}
//		try {
//			db.aliasList(spelledUser);
//		} catch (UserDBException udbe) {
//			reportDBError(replyToUser, spelledUser);
//			return;
//		}
//		reportByTell(replyToUser, "onRemove(" + unspelledUser + ") -> "
//				+ spelledUser);
//	}
//
//	/*
//	 * Utilities
//	 */
//
//	private void reportByTell(String replyToUser, String message) {
//		server.tell(replyToUser, message);
//	}
//
//	private void reportErrorByTell(String replyToUser, String message) {
//		reportByTell(replyToUser, "ERROR: " + message);
//	}
//
//	private void reportNoSuchUser(String replyToUser, String user) {
//		reportErrorByTell(replyToUser, "user " + user
//				+ " does not exist on FIBS.");
//	}
//
//	private void reportDBError(String replyToUser) {
//		reportErrorByTell(replyToUser,
//				"Database error. Please contact an administrator.");
//	}
//
//	private void reportDBError(String replyToUser, String user) {
//		reportErrorByTell(replyToUser, "Database error processing user " + user
//				+ ". Please contact an administrator.");
//	}
//
//	private void reportDBError(String replyToUser, String user1, String user2) {
//		reportErrorByTell(replyToUser, "Database error aliasing user " + user1
//				+ " and " + user2 + ". Please contact an administrator.");
//	}
//}