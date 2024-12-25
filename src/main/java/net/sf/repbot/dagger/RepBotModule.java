package net.sf.repbot.dagger;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import net.sf.repbot.command.ConversationCommand;
import net.sf.repbot.command.DefaultCommand;
import net.sf.repbot.command.HelpCommand;
import net.sf.repbot.db.UserDB;
import net.sf.repbot.db.UserDBException;
import net.sf.repbot.db.UserDBImpl;
import net.sf.repbot.preferences.AdminPreferences;
import net.sf.repbot.preferences.LoggerPreferences;
import net.sf.repbot.preferences.Preferences;
import net.sf.repbot.preferences.PreferencesImpl;
import net.sf.repbot.preferences.UserDBPreferences;
import net.sf.repbot.server.FibsListener;
import net.sf.repbot.server.Monitor;
import net.sf.repbot.server.MonitorImpl;
import net.sf.repbot.server.Server;
import net.sf.repbot.server.ServerImpl;
import net.sf.repbot.services.CommandDispatcher;
import net.sf.repbot.services.Executor;
import net.sf.repbot.services.Logger;
import net.sf.repbot.services.MatchEventListener;
import net.sf.repbot.services.NewsListener;
import net.sf.repbot.services.NewsServer;
import net.sf.repbot.services.NoiseGenerator;
import net.sf.repbot.user.NewUserMonitor;
import net.sf.repbot.user.NonZeroExpGarbageCollector;
import net.sf.repbot.user.RoundRobinGarbageCollector;
import net.sf.repbot.user.UserManager;
import net.sf.repbot.user.ZeroExpGarbageCollector;

// https://medium.com/@Zhuinden/that-missing-guide-how-to-use-dagger2-ef116fbea97
// https://github.com/codepath/android_guides/wiki/Dependency-Injection-with-Dagger-2

@Module
public class RepBotModule {

	private final String prefsFilePath;

	public RepBotModule(String prefsFilePath) {
		this.prefsFilePath = prefsFilePath;
	}

	@Provides
	@Singleton
	Preferences providePreferences() throws IllegalArgumentException {
		return new PreferencesImpl(prefsFilePath);
	}

	@Provides
	@Singleton
	static UserDB provideUserDB(Preferences prefs) throws IllegalArgumentException {
		try {
			return new UserDBImpl(new UserDBPreferences(prefs));
		} catch (UserDBException udbe) {
			throw new IllegalArgumentException(udbe);
		}
	}

	@Provides
	@Singleton
	static Logger provideLogger(Preferences prefs) throws IllegalArgumentException {
		return new Logger(new LoggerPreferences(prefs));
	}

	@Provides
	@Singleton
	static Executor provideExecutor() {
		return new Executor();
	}

	@Provides
	@Singleton
	static Server provideServer(FibsListener fibs) throws IllegalArgumentException {
		Server server = new ServerImpl(fibs);
		fibs.addConnectListener(server);
		return server;
	}

	@Provides
	@Singleton
	static FibsListener provideFibsListener(Preferences prefs, Executor cron, Logger logger)
			throws IllegalArgumentException {
		return new FibsListener(prefs, cron, logger);
	}

	@Provides
	@Singleton
	static UserManager provideReaper(Preferences prefs, Server server, UserDB userDB, Monitor monitor, Executor cron,
			Logger logger) throws IllegalArgumentException {
		UserManager reaper = new UserManager(userDB, logger, cron, server);
		reaper.addGarbageCollector(new RoundRobinGarbageCollector(reaper, prefs.getDuration("net.sf.repbot.reaper.timer.roundrobin")));
		reaper.addGarbageCollector(new ZeroExpGarbageCollector(reaper, prefs.getDuration("net.sf.repbot.reaper.timer.zeroexp")));
		reaper.addGarbageCollector(new NonZeroExpGarbageCollector(reaper, prefs.getDuration("net.sf.repbot.reaper.timer.nonzeroexp")));
		return reaper;
	}

	@Provides
	@Singleton
	static MatchEventListener provideMatchEventListener(UserDB userDB, Logger logger) {
		return new MatchEventListener(userDB, logger);
	}

	@Provides
	@Singleton
	static NoiseGenerator provideNoiseGenerator(Preferences prefs, Server server, Executor cron, Logger logger) {
		// TODO: Make preferences entry
		long timeout = TimeUnit.MINUTES.toSeconds(4) + 30;
		TimeUnit timeoutUnit = TimeUnit.SECONDS;
		return new NoiseGenerator(timeout, timeoutUnit, server, cron, logger);
	}

	@Provides
	@Singleton
	static NewUserMonitor provideNewUserReaper(UserManager reaper, UserDB db, Logger logger) {
		return new NewUserMonitor(reaper, db, logger);
	}

	@Provides
	@Singleton
	static Monitor provideMonitor() {
		return new MonitorImpl();
	}

	/*
	 * News
	 */

	@Provides
	@Singleton
	static NewsServer provideNewsServer(Server server, UserDB userDB, Logger logger) throws IllegalArgumentException {
		return new NewsServer(server, userDB, logger);
	}

	@Provides
	@Singleton
	static NewsListener provideNewsListener(NewsServer news, Executor cron) {
		return new NewsListener(news, cron);
	}

	/*
	 * Commands
	 */

	@Provides
	@Singleton
	static DefaultCommand provideDefaultCommand(Preferences prefs, Server server, Executor cron)
			throws IllegalArgumentException {
		final Duration d = prefs.getDuration("net.sf.repbot.fibs.command.spammer.timer");
		final int count = prefs.getUnsignedInteger("net.sf.repbot.fibs.command.spammer.counter");
		return new DefaultCommand(server, cron, d, count);
	}

	@Provides
	@Singleton
	static CommandDispatcher provideCommandDispatcher(Preferences prefs, DefaultCommand defaultCommand, UserDB db,
			Logger logger) {
		return new CommandDispatcher(new AdminPreferences(prefs), defaultCommand, db, logger);
	}

	@Provides
	@Singleton
	static HelpCommand provideHelpCommand(Preferences prefs, Server server) {
		return new HelpCommand(new AdminPreferences(prefs), server);
	}

	@Provides
	static ConversationCommand providesConversationCommand(Server server) {
		return new ConversationCommand(server);
	}
}
