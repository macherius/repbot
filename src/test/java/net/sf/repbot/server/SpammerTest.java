package net.sf.repbot.server;

import java.time.Duration;
import java.time.Instant;
import static java.util.Comparator.*;
import java.util.Comparator;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;


public class SpammerTest {

	Comparator<Spammer> comparator = comparing(Spammer::getLastTell).thenComparing(Spammer::getName);
	private SortedSet<Spammer> spammersByAge = new TreeSet<Spammer>(
			Comparator.comparing(Spammer::getLastTell).thenComparing(Spammer::getName));

	@Test
	public void test() {
		String[] tests = { "2018-02-10T19:27:24.772474Z", "2018-02-13T19:27:24.772474Z", "2018-02-20T19:27:24.772474Z",
				"2018-02-09T19:27:24.772474Z", "2018-02-13T19:27:24.772474Z", "2018-02-15T19:27:24.772474Z",
				"2018-02-17T19:27:24.772474Z", "2018-02-01T19:27:24.772474Z", "2018-02-12T19:27:24.772474Z",
				"2018-02-23T19:27:24.772474Z", "2018-02-22T19:27:24.772474Z",
				"2018-02-13T19:27:24.772474Z"		
		
		
		};

		int c = 'a';
		for (String t : tests) {
			Spammer s = new Spammer("" + (char) c++, Instant.parse(t));
			spammersByAge.add(s);
		}
		
		Instant t = Instant.parse("2018-02-13T19:27:24.772474Z");
		spammersByAge.add(new Spammer("zzz", t));
		spammersByAge.add(new Spammer("kkk", t));
		spammersByAge.add(new Spammer("lll", t));
		spammersByAge.add(new Spammer("lll", t));
		spammersByAge.add(new Spammer("lll", t));
		spammersByAge.add(new Spammer("lll", t));
		spammersByAge.add(new Spammer("lll", t));
		spammersByAge.add(new Spammer("lll", t));
		spammersByAge.add(new Spammer("lll", t));
		spammersByAge.add(new Spammer("lll", t));
		spammersByAge.add(new Spammer("lll", t));
		spammersByAge.add(new Spammer("aaa", t));
		spammersByAge.add(new Spammer("yyy", t));

		spammersByAge.forEach(System.out::println);

	}

	class Spammer {

		String name;
		Instant lastTell;
		int nTells;

		Spammer(String name, Instant lastTell) {
			this.name = name != null ? name : "";
			this.lastTell = lastTell;
			// bump will always be called immediately after new
			// we do not set lastTell and nTells because of that
		}

		void bump() {
			lastTell = Instant.now();
			++nTells;
		}

		boolean isSpammer() {
			return nTells >= 3;
		}

		boolean isOld() {
			return lastTell.plus(Duration.ofSeconds(3)).isBefore(Instant.now());
		}

		String getName() {
			return name;
		}

		Instant getLastTell() {
			return lastTell;
		}

		// @Override
		// public int compareTo(Spammer other) {
		// int comp = lastTell.compareTo(other.lastTell);
		// return (comp == 0) ? name.compareTo(other.name) : comp;
		// }
		//
		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (other instanceof Spammer) {
				Spammer o = (Spammer) other;
				return this.lastTell.equals(o.lastTell) && this.name.equals(o.name);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(lastTell, name);
		}

		@Override
		public String toString() {
			return "" + lastTell + " " + name;
		}
	}

}
