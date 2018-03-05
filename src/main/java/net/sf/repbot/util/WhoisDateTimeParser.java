package net.sf.repbot.util;

import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class WhoisDateTimeParser {

	private WhoisDateTimeParser() {
	};

	private final static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("EEEE, MMMM d HH:mm zzz yyyy",
			Locale.US);

	/** Parses a date relative to the current year. */
	public static Instant parseDate(String fibsdate) {
		return parseDate(fibsdate, Year.now(Clock.systemUTC()));
	}

	/**
	 * Parses a date. Note that fibs dates have no year, so we have to provide one
	 */
	public static Instant parseDate(String fibsdate, Year currentYear) {
		ZonedDateTime zdt = null;
		while (zdt == null) {
			try {
				zdt = dateFormat.parse(fibsdate + ' ' + currentYear.getValue(), ZonedDateTime::from);
			} catch (DateTimeParseException dtpe) {
				// assert: zdt.isAfter(ZonedDateTime.now())
				currentYear = currentYear.minusYears(1);
			}
		}
		return zdt.toInstant();
	}
}
