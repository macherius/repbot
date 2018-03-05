package net.sf.repbot.preferences;

import lombok.Getter;
import lombok.ToString;

import java.time.Duration;

@Getter
@ToString
public class UserDBPreferences {
	
	private final int botVoucherWeight;
	private final int botComplaintWeight;
	private final int maxOpinionWeight;
	private final Duration opinionAcceptedAfterMatch;

	private final String jdbcUrl;
	private final String jdbcUser;
	private final String jdbcPpassword;
	
	public UserDBPreferences(Preferences prefs) throws IllegalArgumentException {
		botVoucherWeight = prefs.getUnsignedInteger("net.sf.repbot.rules.weight.bot.voucher");
		botComplaintWeight = prefs.getUnsignedInteger("net.sf.repbot.rules.weight.bot.complaint");
		maxOpinionWeight = prefs.getUnsignedInteger("net.sf.repbot.rules.weight.user.max");
		opinionAcceptedAfterMatch = prefs.getDuration("net.sf.repbot.rules.timer.opinion.accepted");
		jdbcUrl = prefs.getString("net.sf.repbot.jdbc.url");
		jdbcUser = prefs.getString("net.sf.repbot.jdbc.user");
		jdbcPpassword = prefs.getString("net.sf.repbot.jdbc.password");
	}
}
