package net.sf.repbot.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.junit.*;

import static org.junit.Assert.assertEquals;

public class LastLoginInfoTest {

	String t1 = "** Last login: Wed Feb 21 08:47:31 2018  from host.domain.tld";
	
	public LastLoginInfoTest() {}

	@Test
	public void test() {
		LastLoginInfo lli = new LastLoginInfo(t1);
		
		ZonedDateTime loginDate = ZonedDateTime.of(2018, 2, 21, 8, 47, 31, 0, 
				ZoneId.of("UTC"));
		assertEquals(loginDate, lli.getTime());
		assertEquals("host.domain.tld", lli.getHost());		
	}

}
