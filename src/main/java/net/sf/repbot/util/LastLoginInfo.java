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
            return switch (month) {
                case "Jan" -> 1;
                case "Feb" -> 2;
                case "Mar" -> 3;
                case "Apr" -> 4;
                case "May" -> 5;
                case "Jun" -> 6;
                case "Jul" -> 7;
                case "Aug" -> 8;
                case "Sep" -> 9;
                case "Oct" -> 10;
                case "Nov" -> 11;
                case "Dec" -> 12;
                default -> 1;
            };
	}
}
