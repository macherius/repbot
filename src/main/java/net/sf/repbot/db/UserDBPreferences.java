package net.sf.repbot.db;

import lombok.Getter;
import lombok.ToString;
import net.sf.repbot.preferences.Preferences;
import java.time.Duration;

@Getter
@ToString

public class UserDBPreferences {
	
	private int botVoucherWeight;
	private int botComplaintWeight;
	private int maxOpinionWeight;
	private Duration opinionAcceptedAfterMatch;

	private String jdbcUrl;
	private String jdbcUser;
	private String jdbcPpassword;
	
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
