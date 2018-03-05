/*
 * @since 20130307
 * @author Ingo Macherius
 */
package net.sf.repbot.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ListFormatter {

	protected final static int MAX_FIBS_LINE_LENGTH = 1024;

	private List<String> opinions;
	private String sl_prefix;
	private final static String sl_suffix = "]";
	private String ml_prefix;
	private final static String ml_suffix = "";
	private String ml_continuationPrefix;
	private final static String ml_continuationPostfix = "";
	private String ml_empty;
	private final static String sl_empty = "[]";
	final static String delimiter = ", ";

	public ListFormatter(List<String> opinions, String user, String role) {
		this(opinions);
		this.sl_prefix = user + ' ' + role + ": [";
		this.ml_prefix = user + "'s " + role + ": ";
		this.ml_continuationPrefix = user + "'s " + role + " (cont'd): ";
		this.ml_empty = user + " has no " + role + ".";
	}

	public ListFormatter(List<String> opinions) {
		this.opinions = (null == opinions) ? this.opinions = Collections.emptyList() : opinions;
	}

	public int getSingleLineLength() {
		return sl_prefix.length()
				+ (opinions.isEmpty() ? 0
						: (opinions.size() - 1) * delimiter.length()
								+ opinions.stream().unordered().mapToInt(user -> user.length()).reduce(0, Integer::sum))
				+ sl_suffix.length();
	}

	@Override
	public String toString() {
		if (opinions.isEmpty()) {
			return sl_empty;
		}
		return opinions.stream().collect(Collectors.joining(delimiter, sl_prefix, sl_suffix));
	}

	/**
	 * Output a long list of players, breaking it into several lines if necessary.
	 */

	public final List<String> toList() {
		if (opinions.isEmpty()) {
			return Collections.singletonList(ml_empty);
		}
		final List<String> replies = new ArrayList<>();
		// use a buffer of MAX_FIBS_LINE_LENGTH + 10% slack
		final StringBuilder sb = new StringBuilder(MAX_FIBS_LINE_LENGTH + (int) (MAX_FIBS_LINE_LENGTH * 0.1));
		sb.append(ml_prefix);
		boolean first = true;
		for (String user : opinions) {
			if (sb.length() > MAX_FIBS_LINE_LENGTH) {
				sb.append(ml_continuationPostfix);
				replies.add(sb.toString());
				sb.setLength(0); // sb = "";
				sb.append(ml_continuationPrefix);
			} else {
				if (first) {
					first = false;
				} else {
					sb.append(delimiter);
				}
			}
			sb.append(user);
		}
		sb.append(ml_suffix);
		replies.add(sb.toString());
		return replies;
	}
}
