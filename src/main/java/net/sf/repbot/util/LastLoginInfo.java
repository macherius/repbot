package net.sf.repbot.util;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LastLoginInfo {
	// "** Last login: Tue Feb 20 09:23:08 2018 from host.domain.tld";

	private final static Pattern FIBS_LAST_LOGIN_INFO_PATTERN = Pattern
			.compile("\\*\\*\\s+Last\\s+login:\\s+" + "(Mon|Tue|Wed|Thu|Fri|Sat|Sun)" + "\\s+" // Tue
					+ "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)" + "\\s+" // Feb
					+ "([0-3][0-9])" + "\\s+" // 20
					+ "([0-2][0-9]):([0-5][0-9]):([0-5][0-9])" + "\\s+" // 09:23:08 (two spaces!)
					+ "([0-9]{4})" + "\\s+" // 2018
					+ "from" + "\\s+" + "(.*)$" // from host.domain.tld
	);

	ZonedDateTime time;
	String host;

	public LastLoginInfo(String line) {
		Matcher m = FIBS_LAST_LOGIN_INFO_PATTERN.matcher(line);
		if (m.matches()) {
			this.time = getLastLoginTime(m);
			this.host = m.group(8);
		}
	}

	public ZonedDateTime getTime() {
		return time;
	}

	public String getHost() {
		return host;
	}

	private static ZonedDateTime getLastLoginTime(Matcher m) {
		MatchResult mr = m.toMatchResult();
		try {
			return ZonedDateTime.of(Integer.parseUnsignedInt(mr.group(7)), // year
					getFibsMonth(mr.group(2)), // month
					Integer.parseUnsignedInt(mr.group(3)), // dayOfMonth
					Integer.parseUnsignedInt(mr.group(4)), // hour
					Integer.parseUnsignedInt(mr.group(5)), // minute
					Integer.parseUnsignedInt(mr.group(6)), // second
					0, // nanoOfSecond
					ZoneId.of("UTC"));
		} catch (DateTimeException dte) {
			dte.printStackTrace();
			return ZonedDateTime.now(Clock.systemUTC());
		}
	}

	private static int getFibsMonth(String month) {
		switch (month) {
		case "Jan":
			return 1;
		case "Feb":
			return 2;
		case "Mar":
			return 3;
		case "Apr":
			return 4;
		case "May":
			return 5;
		case "Jun":
			return 6;
		case "Jul":
			return 7;
		case "Aug":
			return 8;
		case "Sep":
			return 9;
		case "Oct":
			return 10;
		case "Nov":
			return 11;
		case "Dec":
			return 12;
		default:
			return 1;
		}
	}
}
