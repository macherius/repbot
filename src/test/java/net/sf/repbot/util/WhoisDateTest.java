package net.sf.repbot.util;


import java.time.Instant;
import java.time.Year;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class WhoisDateTest {

	@Test
	public void test() {
		
		Year year = Year.of(2018);
		
		String test = "Friday, May 18 00:34 CET";
		Instant expect = Instant.parse("2018-05-17T22:34:00Z");
		assertEquals(WhoisDateTimeParser.parseDate(test, year), expect);
		
		test = "Sunday, February 18 01:21 CET";
		expect = Instant.parse("2018-02-18T00:21:00Z");
		assertEquals(WhoisDateTimeParser.parseDate(test, year), expect);

		test = "Wednesday, November 15 00:52 UTC";
		expect = Instant.parse("2017-11-15T00:52:00Z");
		assertEquals(WhoisDateTimeParser.parseDate(test, year), expect);
		
		test = "Saturday, November 19 13:15 UTC";
		expect = Instant.parse("2016-11-19T13:15:00Z");
		assertEquals(WhoisDateTimeParser.parseDate(test, year), expect);

		test = "Sunday, October 01 07:14 CEST";
		expect = Instant.parse("2017-10-01T05:14:00Z");
		assertEquals(WhoisDateTimeParser.parseDate(test, year), expect);		
	}

}
