package net.sf.repbot.services;

import java.io.*;
import java.time.Clock;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import net.sf.repbot.preferences.LoggerPreferences;

/** Logger class which manages rotated log files. Replaces System.out and
 *  System,err so normal writes are also logged.
 */
public class Logger {

    private final DateTimeFormatter filePathFormat;
    private final DateTimeFormatter timestampFormat;
    private ZonedDateTime nextLogSwitchDate;
    private final boolean logToStdout;

	public Logger(LoggerPreferences prefs) {
		nextLogSwitchDate = ZonedDateTime.now(Clock.systemUTC());
		logToStdout = prefs.isLogToStdout();
		timestampFormat = DateTimeFormatter.ofPattern(prefs.getTimestampFormat());
		filePathFormat = DateTimeFormatter.ofPattern(prefs.getFilePathFormat());
		reallySwitchLog(nextLogSwitchDate);
	}

	private void reallySwitchLog(ZonedDateTime now) {
		if (logToStdout) {
			return;
		}
		String fname = filePathFormat.format(now);
		PrintStream oldlog = System.out;
		try {
			OutputStream newlog = new FileOutputStream(fname, true);
			System.setOut(new PrintStream(newlog, true, "US-ASCII"));
			System.setErr(new PrintStream(newlog, true, "US-ASCII"));
			oldlog.close();
		} catch (IOException ex) {
			// hope for the best
			System.exit(1);
		}
		nextLogSwitchDate = now.with(TemporalAdjusters.firstDayOfNextMonth()).truncatedTo(ChronoUnit.DAYS);
	}

	private void maybeSwitchLog(ZonedDateTime now) {
		if (now.isAfter(nextLogSwitchDate)) {
			reallySwitchLog(now);
		}
	}

	public void logTimestampedLine(String message) {
		ZonedDateTime now = ZonedDateTime.now(Clock.systemUTC());
		maybeSwitchLog(now);
		System.out.print(timestampFormat.format(now) + ":" + message + "\n");
	}

	public void logTimestampedLine(Exception e) {
		ZonedDateTime now = ZonedDateTime.now(Clock.systemUTC());
		maybeSwitchLog(now);
		System.out.print(timestampFormat.format(now) + ":" + e.getMessage() + "\n");
		e.printStackTrace();
	}

	public void logTimestampedLine(String message, Exception e) {
		ZonedDateTime now = ZonedDateTime.now(Clock.systemUTC());
		maybeSwitchLog(now);
		System.out.print(timestampFormat.format(now) + ":" + message + "\n");
		e.printStackTrace();
	}
}
