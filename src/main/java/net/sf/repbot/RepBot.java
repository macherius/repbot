package net.sf.repbot;

import javax.inject.Inject;
import net.sf.repbot.command.NullCommand;
import net.sf.repbot.dagger.RepbotComponent;
import net.sf.repbot.dagger.DaggerRepbotComponent;
import net.sf.repbot.dagger.RepBotModule;
import net.sf.repbot.db.UserDB;
import net.sf.repbot.db.UserDBException;
import net.sf.repbot.preferences.Preferences;
import net.sf.repbot.preferences.PreferencesImpl;
import net.sf.repbot.server.FibsListener;
import net.sf.repbot.server.Monitor;
import net.sf.repbot.services.CommandDispatcher;
import net.sf.repbot.services.ConnectionActivity;
import net.sf.repbot.services.Executor;
import net.sf.repbot.services.Logger;
import net.sf.repbot.services.MatchEventListener;
import net.sf.repbot.services.NewsListener;
import net.sf.repbot.services.NoiseGenerator;
import net.sf.repbot.user.NewUserMonitor;

public class RepBot {

	@Inject
	Executor cron;
	@Inject
	FibsListener fibs;
	@Inject
	UserDB userDB;
	@Inject
	Logger logger;
	@Inject 
	Monitor monitor;
	@Inject
	Preferences prefs;
	@Inject
	NewsListener newsListener;
	@Inject
	CommandDispatcher dispatcher;
	@Inject
	MatchEventListener matchEventListener;
	@Inject
	NoiseGenerator noiseGenerator;
	@Inject
	NewUserMonitor newUserReaper;

	RepbotComponent repBotComponent;

	public RepBot(String prefsFilePath) {
		repBotComponent = DaggerRepbotComponent.builder().repBotModule(new RepBotModule(prefsFilePath)).build();
		repBotComponent.inject(this);

		registerLineListeners();
		registerCommands();
		registerMonitorListeners();

		/*
		 * Run a garbage collection on match events every hour
		 */
		Runnable matchEventGarbageCollector = () -> {
			try {
				userDB.garbageCollectMatchEvents();
			} catch (UserDBException e) {
				logger.logTimestampedLine(e);
			}
		};

		cron.scheduleWithFixedDelay(matchEventGarbageCollector, 0,
				prefs.getLong("net.sf.repbot.reaper.timer.matchevent"),
				prefs.getTimeUnit("net.sf.repbot.reaper.timer.matchevent"));

	
		/*
		 * Fork off the network thread and register network level listeners
		 */
		// cron.execute(fibs);
		fibs.run();

	}

	/*
	 * Create the low level fibs message monitor and register listeners
	 */

	void registerMonitorListeners() {
		monitor.addEventListener(matchEventListener);
		// disabled shouts via the Conversation class, below
		// m.addEventListener(new Conversation(server, fl));
		monitor.addEventListener(newUserReaper);

	}

	void registerLineListeners() {
		// fibs.addLineListener(new ConnectionActivity(1, TimeUnit.MINUTES, fibs,
		// cron));
		// Keepalive timeout 4.5 minutes (20100601, as per new 5 min timeout rule on
		// load)
		fibs.addLineListener(noiseGenerator);
		fibs.addLineListener(dispatcher);
		fibs.addLineListener(newsListener);
		fibs.addLineListener(monitor);
	}

	/*
	 * Register the commands available to users on fibs.
	 */

	void registerCommands() {
		dispatcher.register("h|help", repBotComponent.help());
		dispatcher.register("a|ask", repBotComponent.ask());
		dispatcher.register("f|friends", repBotComponent.friends());
		dispatcher.register("l|list", repBotComponent.list());
		dispatcher.register("c|complain", repBotComponent.complain());
		dispatcher.register("v|vouch", repBotComponent.vouch());
		dispatcher.register("w|withdraw", repBotComponent.withdraw());
		dispatcher.register("alert", repBotComponent.alert());
		dispatcher.register("thump", new NullCommand());

		final String[] hello = { "Hi!  I'm RepBot, designed to help you avoid droppers and"
				+ " abusive players.  'tell RepBot help' for more information." };
		final String[] thanks = { "You're most welcome", "Thank you!" };
		final String[] tell = { "You shouldn't use 'tell Repbot', as your fibs client apprently adds "
				+ "it automatically. Try the command again without 'tell RepBot'." };

		dispatcher.register("thanks|thank|tx|ty|tu|thx", repBotComponent.conversation().responses(thanks));
		dispatcher.register("hi|hello", repBotComponent.conversation().responses(hello));
		dispatcher.register("tell", repBotComponent.conversation().responses(tell));
	}

	public static void main(String args[]) {
		final Preferences uPrefs;
		final String prefsPath;
		if (args.length == 0) {
			uPrefs = new PreferencesImpl();
			prefsPath = null;
		} else if (args.length == 1) {
			uPrefs = new PreferencesImpl(args[0]);
			prefsPath = args[0];
		} else {
			System.out.println("Usage: java " + RepBot.class.getCanonicalName() + " file.properties");
			return;
		}
		if (uPrefs.getBoolean("net.sf.repbot.debug")) {
			uPrefs.list(System.out);
		}
		new RepBot(prefsPath);
	}
}
