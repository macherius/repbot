/*
 * @since 20130307
 * @author Ingo Macherius
 */
package net.sf.repbot.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ListFormatterMulti {

	private final static String delimiter = " ";

	private final List<ListFormatter> lists;
	private final boolean allowSingleLine;
	private boolean isSingleLine = false;

	public ListFormatterMulti(List<ListFormatter> lists) {
		this(lists, true);
	}

	public ListFormatterMulti(List<ListFormatter> lists, boolean allowSingleLine) {
		this.allowSingleLine = allowSingleLine;
		this.lists = lists;
		if (allowSingleLine) {
			// delimiting spaces, -1 for empty lists is OK
			isSingleLine = (lists.size() - 1) * delimiter.length()
					+ lists.stream().unordered().mapToInt(lfs -> lfs.getSingleLineLength()).reduce(0,
							Integer::sum) < ListFormatter.MAX_FIBS_LINE_LENGTH;
		}
	}

	public List<String> toList() {
		if (isSingleLine()) {
			return Collections.singletonList(this.toString());
		}
		return lists.stream().flatMap(lf -> lf.toList().stream()).collect(Collectors.toList());
	}

	public boolean isSingleLine() {
		return allowSingleLine && isSingleLine;
	}

	@Override
	public String toString() {
		return lists.stream().map(lf -> lf.toString()).collect(Collectors.joining(delimiter));
	}
}
