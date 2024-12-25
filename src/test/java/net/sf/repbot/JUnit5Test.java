package net.sf.repbot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("fast")
public class JUnit5Test {

	@Test
	@DisplayName("My 1st JUnit 5 test! 😎")
	public void justAnExample() {
		assertEquals(3, 2 + 1);
//		assertEquals("My 1st JUnit 5 test! 😎", testInfo.getDisplayName(), () -> "TestInfo is injected correctly");
		System.out.println("This test method should be run");
	}
}