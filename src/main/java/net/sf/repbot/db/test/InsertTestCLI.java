package net.sf.repbot.db.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import net.sf.repbot.db.NoSuchUserException;
import net.sf.repbot.db.UserDBException;
import net.sf.repbot.db.UserDBImpl;
import net.sf.repbot.db.UserDB;
import net.sf.repbot.preferences.Preferences;
import net.sf.repbot.preferences.PreferencesImpl;
import net.sf.repbot.preferences.UserDBPreferences;

public class InsertTestCLI {

	// FIXME: make it JUnit
	public static void main(String args[]) {

		try {
			
			Preferences prefs = new PreferencesImpl();

			UserDB userDB = new UserDBImpl(new UserDBPreferences(prefs));

			// read stdin by line, ignoring comment lines
			String inputLine;
			BufferedReader bin = new BufferedReader(new InputStreamReader(
					System.in, Charset.forName("US-ASCII")));
			String cmd;
			String user;
			while ((inputLine = bin.readLine()) != null) {

				System.out.println(inputLine);
				// System.out.println("LINE:" + inputLine );
				if ('#' != inputLine.charAt(0)) {
					StringTokenizer tokens = new StringTokenizer(inputLine);
					try {
						cmd = tokens.nextToken();
						if (cmd.compareTo("complain") == 0) {
							userDB.complain(tokens.nextToken(),
									Integer.parseUnsignedInt(tokens.nextToken()),
									tokens.nextToken());
						} else if (cmd.compareTo("list") == 0) {
							String _tempUser = tokens.nextToken();
							try {
								System.out.println("complainers:"
										+ userDB.getComplainers(_tempUser));
								System.out.println("vouchers:"
										+ userDB.getVouchers(_tempUser));
							} catch (NoSuchUserException nsue) {
								System.out.println(_tempUser
										+ "not in USER table");
							}
							System.out.println("complaints:"
									+ userDB.getComplaints(_tempUser));
							System.out.println("vouches:"
									+ userDB.getVouches(_tempUser));
						} else if (cmd.compareTo("vouch") == 0) {
							userDB.vouch(tokens.nextToken(),
									Integer.parseUnsignedInt(tokens.nextToken()),
									tokens.nextToken());
						} else if (cmd.compareTo("withdraw") == 0) {
							userDB.withdraw(tokens.nextToken(),
									tokens.nextToken());
						} else if (cmd.compareTo("getReputation") == 0) {
							user = tokens.nextToken();
							System.out.println("Reputation for user " + user
									+ " = " + userDB.getReputation(user));
						}
					} catch (NoSuchElementException tokene) {
						// only catch the nextToken exception here and skip?
					} catch (Exception e) {
						e.printStackTrace();
						// shutdown?
					}

				}
			}
			((UserDBImpl) userDB).shutdown();
		} catch (UserDBException | IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

}
