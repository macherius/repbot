package net.sf.repbot.preferences;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AdminPreferences {
	
	private final String[] admin;
	private final String[] banned;
	
	public AdminPreferences(Preferences prefs) {
		admin = prefs.getCommaSeparated("net.sf.repbot.fibs.user.admin");
		banned = prefs.getCommaSeparated("net.sf.repbot.fibs.user.banned");
	}

}
