package net.sf.repbot.server.command;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of a username speller.
 *
 * @author Avi Kivity
 * @author Ingo Macherius
 */
public class SpellImpl implements Spell {

	private static final String SPELLER_PATH_PLAINFILE = "speller.txt";
	private static final int SPELLER_IO_BUFFERSIZE = 2 << 16; // 131072 byte

	private Whois whois;
	private Map<String, Record> cache;
	private Instant flushTime;
	private boolean dirty;

	/** Creates a new instance of SpellImpl. */

	public SpellImpl(Whois whois) {
		this.whois = whois;
		cache = loadFromTextFile(SPELLER_PATH_PLAINFILE);
		flushTime = Instant.now();
	}

	/** Submits an asynchronous request to the whois database. */
	@Override
	public void query(final String name, final ReplyListener replyListener, long timeout) {
		flush();
		final String key = Record.toKey(name);
		Record record = cache.get(key);
		if (record != null && record.isExpired()) {
			cache.remove(key);
			record = null;
		}
		if (record != null)
			replyListener.onReply(name, record.spelledName);
		else
			// Use name in replies, not key. Listener may have own logic.
			whois.query(name, new Whois.ReplyListener() {
				@Override
				public void onReply(String name, Whois.Info info) {
					cache.put(key, new Record(info.getName()));
					markDirty();
					replyListener.onReply(name, info.getName());
					// FIXME: We have done a whois, also use it to update DB (reaper)
				}

				@Override
				public void onNoUser(String name) {
					replyListener.onNoUser(name);
				}

				@Override
				public void onTimeout(String name) {
					replyListener.onTimeout(name);
				}
			}, timeout);

	}

	/** Flushes the cache to disk, if necessary. */
	private void flush() {
		if (!dirty || Instant.now().isBefore(flushTime.plus(Duration.ofHours(1))))
			return;
		saveToTextFile(cache, SPELLER_PATH_PLAINFILE);
		dirty = false;
		flushTime = Instant.now();
	}

	/** Marks the cache as dirty. */
	private void markDirty() {
		if (!dirty) {
			flushTime = Instant.now();
			dirty = true;
		}
	}

	private Map<String, Record> loadFromTextFile(String filePath) {
		System.out.println("Speller cache load from '" + filePath + "' attempt.");
		final Map<String, Record> spelled = emptyCache();
		int count = 0;
		try (BufferedReader file = new BufferedReader(new FileReader(filePath), SPELLER_IO_BUFFERSIZE)) {
			// 0-9 is not valid with fibs names, but be nice here to for tigergammon & co
			Pattern entry = Pattern.compile("^([a-zA-Z0-9_]+) (\\d+)$");
			for (String line = file.readLine(); line != null; line = file.readLine()) {
				final Matcher m;
				if ((m = entry.matcher(line)).matches()) {
					try {
						final Record rec = new Record(m.group(1), m.group(2));
						if (!rec.isExpired()) {
							spelled.put(rec.toKey(), rec);
							count++;
						}
					} catch (NumberFormatException nfe) {
						// Just ignore, it is only a cache
					}
				}
			}
		} catch (FileNotFoundException fnfe) {
			System.out.println("Not restoring speller cache, '" + filePath + "' not found." + fnfe);
			return spelled;
		} catch (IOException ioe) {
			System.out.println("Not restoring speller cache." + ioe);
		}
		System.out.println("Restored " + count + " entries from speller cache file '" + filePath + "'.");
		return (spelled == null) ? emptyCache() : spelled;
	}

	private void saveToTextFile(Map<String, Record> spelled, String filePath) {
		int count = 0;
		try (BufferedWriter file = new BufferedWriter(new FileWriter(filePath), SPELLER_IO_BUFFERSIZE)) {
			for (Map.Entry<String, Record> r : spelled.entrySet()) {
				Record rec = r.getValue();
				if (!rec.isExpired()) {
					file.write(rec.spelledName + ' ' + rec.timestamp);
					file.newLine();
					count++;
				}
			}
		} catch (IOException ioe) {
			System.out.println("Speller cache flush into file '" + filePath + "' failed." + ioe);
		}
		System.out.println("Saved " + count + " entries into speller cache file '" + filePath + "'.");
	}

	private static <K, V> Map<K, V> emptyCache() {
		return Collections.synchronizedMap(new HashMap<>(512));
	}

	/*
	 * Entry for one spelled user with timestamp of last verification.
	 */

	private final static class Record {
		/*
		 * Written to be memory efficient, not for performance or Object creation
		 * minimization.
		 */
		private static final Duration SPELLER_GRACE = Duration.ofDays(14);

		String spelledName;
		long timestamp;

		Record(String spelledName) {
			this(spelledName, Clock.systemUTC().millis());
		}

		Record(String spelledName, String time) throws NumberFormatException {
			this(spelledName, Long.parseUnsignedLong(time));
		}

		Record(String spelledName, long timestamp) {
			this.spelledName = spelledName;
			this.timestamp = timestamp;
		}

		boolean isExpired() {
			return Instant.now().isAfter(Instant.ofEpochMilli(timestamp).plus(SPELLER_GRACE));
		}

		String toKey() {
			return toKey(spelledName);
		}

		static String toKey(String name) {
			return name.toLowerCase(Locale.US);
		}
	}
}
