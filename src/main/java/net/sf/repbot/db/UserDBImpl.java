package net.sf.repbot.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import lombok.Getter;
import net.sf.repbot.preferences.UserDBPreferences;

public class UserDBImpl implements UserDB {

	final static String sqlPropertyPath = "/sql.properties.xml";

	UserDBPreferences prefs;

	public UserDBImpl(UserDBPreferences prefs) throws UserDBException {
		this.prefs = prefs;
		init();
	}

	private Properties loadSQL(String sqlPropertiesPath) throws IOException {
		try (InputStream sqlStream = UserDBImpl.class.getResourceAsStream(sqlPropertiesPath);) {
			Properties sql = new Properties();
			sql.loadFromXML(sqlStream);
			String botV = Integer.toString(prefs.getBotVoucherWeight());
			String botC = Integer.toString(prefs.getBotComplaintWeight());
			String maxWeight = Integer.toString(prefs.getMaxOpinionWeight());
			sql.entrySet().stream().filter(e -> ((String) e.getValue()).indexOf('@', 0) >= 0).forEach(e -> {
				String value = (String) e.getValue();
				value = value.replace("@net.sf.repbot.db.sql.voucher@", Opinion.VOUCHER.toString());
				value = value.replace("@net.sf.repbot.db.sql.complaint@", Opinion.COMPLAINT.toString());
				value = value.replace("@net.sf.repbot.rules.weight.bot.voucher@", botV);
				value = value.replace("@net.sf.repbot.rules.weight.bot.complaint@", botC);
				value = value.replace("@net.sf.repbot.rules.weight.user.max@", maxWeight);
				e.setValue(value);
			});
			return sql;
		}
	}

	private void init() throws UserDBException {
		try {
			// connect to sql server, login etc...
			con = DriverManager.getConnection(prefs.getJdbcUrl(), prefs.getJdbcUser(), prefs.getJdbcPpassword());
		} catch (SQLException e) {
			throw new UserDBException("Failed to connect to " + prefs.getJdbcUrl() + " as " + prefs.getJdbcUser(), e);
		}
		try {
			Properties p = loadSQL(sqlPropertyPath);
			queryComplainersPS = con.prepareStatement(p.getProperty("queryComplainersPS"));
			queryVouchersPS = con.prepareStatement(p.getProperty("queryVouchersPS"));
			queryComplaintsPS = con.prepareStatement(p.getProperty("queryComplaintsPS"));
			queryVouchesPS = con.prepareStatement(p.getProperty("queryVouchesPS"));
			queryReputationPS = con.prepareStatement(p.getProperty("queryReputationPS"));
			queryUserPS = con.prepareStatement(p.getProperty("queryUserPS"));
			queryFriendsOpinionPS = con.prepareStatement(p.getProperty("queryFriendsOpinionPS"));
			insertUserPS = con.prepareStatement(p.getProperty("insertUserPS"));
			insertOpinionPS = con.prepareStatement(p.getProperty("insertOpinionPS"));
			deleteOpinionPS = con.prepareStatement(p.getProperty("deleteOpinionPS"));
			queryOpinionPS = con.prepareStatement(p.getProperty("queryOpinionPS"));
			dumpUsersPS = con.prepareStatement(p.getProperty("dumpUsersPS"));
			queryNewsPS = con.prepareStatement(p.getProperty("queryNewsPS"));
			updateNewsLevelPS = con.prepareStatement(p.getProperty("updateNewsLevelPS"));
			queryGlobalNewsLevelPS = con.prepareStatement(p.getProperty("queryGlobalNewsLevelPS"));
			insertGlobalNewsPS = con.prepareStatement(p.getProperty("insertGlobalNewsPS"));
			queryAlertPS = con.prepareStatement(p.getProperty("queryAlertPS"));
			updateAlertPS = con.prepareStatement(p.getProperty("updateAlertPS"));
			queryLastVerifiedPS = con.prepareStatement(p.getProperty("queryLastVerifiedPS"));
			updateLastVerifiedPS = con.prepareStatement(p.getProperty("updateLastVerifiedPS"));
			updateLastVerifiedExpLogoutPS = con.prepareStatement(p.getProperty("updateLastVerifiedExpLogoutPS"));
			queryLastLogoutZeroExpPS = con.prepareStatement(p.getProperty("queryLastLogoutZeroExpPS"));
			queryLastLogoutNonzeroExpPS = con.prepareStatement(p.getProperty("queryLastLogoutNonzeroExpPS"));
			deleteUserPS = con.prepareStatement(p.getProperty("deleteUserPS"));
			queryBotPS = con.prepareStatement(p.getProperty("queryBotPS"));
			upsertMatchEventPS = con.prepareStatement(p.getProperty("upsertMatchEventPS"));
			garbageCollectMatchEventsPS = con.prepareStatement(p.getProperty("garbageCollectMatchEventsPS"));
			queryMatchEventPS = con.prepareStatement(p.getProperty("queryMatchEventPS"));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new UserDBException("Failed to register prepared statements", e);
		} catch (IOException ioe) {
			throw new UserDBException("Failed to getResourceAsStream queries from '" + sqlPropertyPath + "'.\n");
		}
	}

	private void reconnect() throws UserDBException {
		if (con == null)
			init();
	}

	private void invalidateConnection() {
		con = null;
	}

	public void shutdown() throws UserDBException {
		try {
			con.close();
		} catch (SQLException sqle) {
			throw new UserDBException("Failed shutting down UserDB", sqle);
		}
	}

	@Override
	public List<String> getComplainers(String user) throws NoSuchUserException, UserDBException {
		return getUserSet(queryComplainersPS, user, "complainers");
	}

	private List<String> getUserSet(PreparedStatement ps, String user, String description) throws UserDBException {
		reconnect();
		try {
			ps.setString(1, user);
			ResultSet rs = ps.executeQuery();
			List<String> ret = toList(rs);
			rs.close();
			return ret;
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed getting " + description + " for " + user, e);
		}
	}

	@Override
	public Collection<String> getUserNames() throws UserDBException {
		reconnect();
		try (ResultSet rs = dumpUsersPS.executeQuery();) {
			return toList(rs);
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed getting all user names", e);
		}
	}

	private List<String> toList(ResultSet rs) throws SQLException {
		if (rs.next()) {
			List<String> c = new ArrayList<>();
			while (rs.next())
				c.add(rs.getString(1));
			return c;
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getVouchers(String user) throws NoSuchUserException, UserDBException {
		return getUserSet(queryVouchersPS, user, "vouchers");
	}

	@Override
	public List<String> getComplaints(String user) throws UserDBException {
		return getUserSet(queryComplaintsPS, user, "complaints");
	}

	@Override
	public List<String> getVouches(String user) throws UserDBException {
		return getUserSet(queryVouchesPS, user, "vouches");
	}

	@Override
	public int getReputation(String user) throws NoOpinionException, UserDBException {
		reconnect();
		try {
			int rep = 0;
			boolean hasReputation = false;
			queryReputationPS.setString(1, user);
			ResultSet rs = queryReputationPS.executeQuery();
			if (rs.next()) {
				rep = rs.getInt(1);
				hasReputation = !rs.wasNull();
			}
			rs.close();
			if (!hasReputation)
				throw new NoOpinionException(user);
			return rep;
		} catch (SQLException sqle) {
			invalidateConnection();
			throw new UserDBException("Failed getting reputation for " + user, sqle);
		}
	}

	private int getUserID(String name) throws UserDBException {
		reconnect();
		try {
			queryUserPS.setString(1, name);
			ResultSet rs = queryUserPS.executeQuery();
			int id = rs.next() ? rs.getInt(1) : insertUser(name);
			rs.close();
			return id;
		} catch (SQLException sqle) {
			invalidateConnection();
			throw new UserDBException("Failed getting user ID for " + name, sqle);
		}
	}

	@Override
	public boolean isKnownUser(String name) throws UserDBException {
		reconnect();
		try {
			queryUserPS.setString(1, name);
			ResultSet rs = queryUserPS.executeQuery();
			boolean found = rs.next();
			rs.close();
			return found;
		} catch (SQLException sqle) {
			invalidateConnection();
			throw new UserDBException("Failed getting user ID for " + name, sqle);
		}
	}

	private int insertUser(String name) throws UserDBException {
		reconnect();
		try {
			insertUserPS.setString(1, name);
			insertUserPS.setInt(2, getGlobalNewsLevel());
			insertUserPS.executeUpdate();
			return getUserID(name);
		} catch (SQLException sqle) {
			invalidateConnection();
			throw new UserDBException("Failed inserting user " + name, sqle);
		}
	}

	@Override
	public void complain(String fromWho, int fromExperience, String aboutWho)
			throws AlreadyComplainedException, AlreadyVouchedException, UserDBException {
		opinion(fromWho, fromExperience, aboutWho, Opinion.COMPLAINT);
	}

	@Override
	public void vouch(String fromWho, int experience, String aboutWho)
			throws AlreadyComplainedException, AlreadyVouchedException, UserDBException {
		opinion(fromWho, experience, aboutWho, Opinion.VOUCHER);
	}

	private void opinion(String originator, int experience, String target, Opinion type)
			throws AlreadyComplainedException, AlreadyVouchedException, UserDBException {

		reconnect();
		try {
			queryOpinionPS.setString(1, originator);
			queryOpinionPS.setString(2, target);
			ResultSet rs = queryOpinionPS.executeQuery();
			if (rs.next()) {
				if (Opinion.COMPLAINT == Opinion.toEnum(rs.getString(1)))
					throw new AlreadyComplainedException(target);
				throw new AlreadyVouchedException(target);
			}
			rs.close();
			insertOpinionPS.setInt(1, experience);
			insertOpinionPS.setString(2, type.toString());
			insertOpinionPS.setString(3, target);
			insertOpinionPS.setString(4, originator);
			insertOpinionPS.executeUpdate();
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed adding opinion " + type.name() + " of " + originator + " about " + target,
					e);
		}
	}

	@Override
	public void withdraw(String originator, String target) throws NoOpinionException, UserDBException {
		reconnect();
		try {
			deleteOpinionPS.setString(1, target);
			deleteOpinionPS.setString(2, originator);
			if (deleteOpinionPS.executeUpdate() == 0)
				throw new NoOpinionException(originator + " has no opinion about " + target);
		} catch (SQLException sqle) {
			throw new UserDBException("Failed withdrawing opinion of " + originator + " about " + target, sqle);
		}
	}

	@Override
	public FriendsOpinion getFriendsOpinion(String asker, String about) throws UserDBException {
		reconnect();
		try {
			queryFriendsOpinionPS.setString(1, asker);
			queryFriendsOpinionPS.setString(2, about);
			ResultSet rs = queryFriendsOpinionPS.executeQuery();
			List<String> vouchers = new ArrayList<>();
			List<String> complainers = new ArrayList<>();
			while (rs.next()) {
				final String friend = rs.getString(1);
				final String opinion = rs.getString(2);
				if (Opinion.VOUCHER == Opinion.toEnum(opinion))
					vouchers.add(friend);
				else
					complainers.add(friend);
			}
			rs.close();
			return new FriendsOpinionImpl(vouchers, complainers);
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed getting friends' opinions; asker=" + asker + ", about=" + about, e);
		}
	}

	@Override
	public Collection<String> getNews(String user) throws UserDBException, NoSuchUserException {
		reconnect();
		try {
			queryNewsPS.setString(1, user);
			ResultSet rs = queryNewsPS.executeQuery();
			Collection<String> result = new ArrayList<>();
			while (rs.next())
				result.add(rs.getString(1));
			rs.close();
			return result;
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed getting news level for " + user, e);
		}
	}

	@Override
	public void markNewsAsRead(String user, int items) throws UserDBException, NoSuchUserException {
		reconnect();
		try {
			updateNewsLevelPS.setInt(1, items);
			updateNewsLevelPS.setString(2, user);
			if (updateNewsLevelPS.executeUpdate() != 1)
				throw new NoSuchUserException(user);
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed setting news level for " + user, e);
		}
	}

	@Override
	public void setNews(Collection<String> news) throws UserDBException {
		reconnect();
		try {
			int level = getGlobalNewsLevel();
			Iterator<String> i = news.iterator();
			int n = 1;
			while (n <= level) {
				i.next();
				++n;
			}
			while (i.hasNext()) {
				insertGlobalNewsPS.setInt(1, n++);
				insertGlobalNewsPS.setString(2, i.next());
				insertGlobalNewsPS.executeUpdate();
			}
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed adding news: " + news, e);
		}
	}

	private int getGlobalNewsLevel() throws SQLException {
		try (ResultSet rs = queryGlobalNewsLevelPS.executeQuery()) {
			return rs.next() ? rs.getInt(1) : 0;
		}
	}

	@Override
	public boolean getAlert(String user) throws UserDBException {
		reconnect();
		try {
			queryAlertPS.setString(1, user);
			ResultSet rs = queryAlertPS.executeQuery();
			boolean alert = rs.next() && rs.getBoolean(1);
			rs.close();
			return alert;
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed getting alert status for " + user, e);
		}
	}

	@Override
	public void setAlert(String user, boolean alert) throws UserDBException {
		reconnect();
		try {
			updateAlertPS.setBoolean(1, alert);
			updateAlertPS.setString(2, user);
			updateAlertPS.executeUpdate();
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed setting alert status for " + user, e);
		}
	}

	@Override
	public void markAsUser(String user) throws UserDBException {
		getUserID(user);
	}

	@Override
	public boolean isBot(String user) throws UserDBException {
		reconnect();
		try {
			queryBotPS.setString(1, user);
			ResultSet rs = queryBotPS.executeQuery();
			boolean bot = rs.next() && rs.getBoolean(1);
			rs.close();
			return bot;
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed getting bot status for " + user, e);
		}
	}

	@Override
	public String getLastVerifiedUser() throws UserDBException {
		reconnect();
		try (ResultSet rs = queryLastVerifiedPS.executeQuery()) {
			return rs.next() ? rs.getString(1) : null;
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed getting last verified user", e);
		}
	}

	@Override
	public void setLastVerified(String user, Instant when) throws UserDBException {
		reconnect();
		try {
			updateLastVerifiedPS.setTimestamp(1, Timestamp.from(when));
			updateLastVerifiedPS.setString(2, user);
			updateLastVerifiedPS.executeUpdate();
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed setting last verified date for " + user, e);
		}
	}

	@Override
	public void setLastVerifiedExpLogout(String user, Instant verfied, int exp, Instant logout) throws UserDBException {
		reconnect();
		try {
			updateLastVerifiedExpLogoutPS.setTimestamp(1, Timestamp.from(verfied));
			updateLastVerifiedExpLogoutPS.setInt(2, exp);
			updateLastVerifiedExpLogoutPS.setTimestamp(3, Timestamp.from(logout));
			updateLastVerifiedExpLogoutPS.setString(4, user);
			updateLastVerifiedExpLogoutPS.executeUpdate();
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed setting last verified date for " + user, e);
		}
	}

	@Override
	public String getLastLogoutZeroExpUser() throws UserDBException {
		reconnect();
		try (ResultSet rs = queryLastLogoutZeroExpPS.executeQuery()) {
			return rs.next() ? rs.getString(1) : null;
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed getting last logout zero exp user", e);
		}
	}

	@Override
	public String getLastLogoutNonzeroExpUser() throws UserDBException {
		reconnect();
		try (ResultSet rs = queryLastLogoutNonzeroExpPS.executeQuery()) {
			return rs.next() ? rs.getString(1) : null;
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed getting last logout nonzero exp user", e);
		}
	}
	/*
	 * Deletes a user from the The user's opinions and matches are auto-deleted by
	 * the "REFERENCES users (id) ON DELETE CASCADE" constraints in the SQL schema
	 * (postgres and mariadb IF using InnoDB). We rely on that.
	 */

	@Override
	public void deleteUser(String name) throws UserDBException {
		reconnect();
		try {
			deleteUserPS.setString(1, name);
			deleteUserPS.executeUpdate();
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed deleting user " + name, e);
		}
	}

	/*
	 * Record a match event between user1 and user2 at the time of calling this
	 * method While the relation is of course symmetric (if u1 played u2, then u2
	 * played u1), only one record in the order of the method signature is created.
	 * Method hasMatchEvent(String, String, long) takes care of the symmetry.
	 */
	@Override
	public void insertOrUpdateMatchEvent(String user1, String user2) throws UserDBException {
		reconnect();
		try {
			upsertMatchEventPS.setString(1, user1);
			upsertMatchEventPS.setString(2, user2);
			upsertMatchEventPS.execute();
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed updating match event for users " + user1 + " vs. " + user2, e);
		}
	}

	/*
	 * Deletes all match events before epoch instant maxAgeMillis
	 */
	@Override
	public void garbageCollectMatchEvents() throws UserDBException {
		reconnect();
		try {
			garbageCollectMatchEventsPS.setTimestamp(1,
					Timestamp.from(Instant.now().minus(prefs.getOpinionAcceptedAfterMatch())));
			garbageCollectMatchEventsPS.executeUpdate();
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed garbage collecting match event", e);
		}
	}

	/*
	 * Returns true if user1 and user2 played a match after the epoch instant of
	 * maxAgeMillis Order of user1 and user2 does not matter.
	 */
	@Override
	public boolean hasRecentMatchEvent(String user1, String user2) throws UserDBException {
		reconnect();
		try {
			queryMatchEventPS.setString(1, user1);
			queryMatchEventPS.setString(2, user2);
			queryMatchEventPS.setTimestamp(3,
					Timestamp.from(Instant.now().minus(prefs.getOpinionAcceptedAfterMatch())));
			ResultSet rs = queryMatchEventPS.executeQuery();
			boolean hasMatchEvent = rs.next();
			rs.close();
			return hasMatchEvent;
		} catch (SQLException e) {
			invalidateConnection();
			throw new UserDBException("Failed to query match event for users " + user1 + " vs. " + user2, e);
		}
	}

	private Connection con = null;

	private PreparedStatement dumpUsersPS;
	private PreparedStatement insertUserPS;
	private PreparedStatement insertOpinionPS;
	private PreparedStatement deleteOpinionPS;
	private PreparedStatement queryOpinionPS;

	private PreparedStatement queryComplaintsPS;
	private PreparedStatement queryVouchesPS;
	private PreparedStatement queryComplainersPS;
	private PreparedStatement queryVouchersPS;
	private PreparedStatement queryReputationPS;
	private PreparedStatement queryUserPS;
	private PreparedStatement queryFriendsOpinionPS;

	private PreparedStatement queryNewsPS;
	private PreparedStatement updateNewsLevelPS;
	private PreparedStatement queryGlobalNewsLevelPS;
	private PreparedStatement insertGlobalNewsPS;

	private PreparedStatement queryAlertPS;
	private PreparedStatement updateAlertPS;

	private PreparedStatement queryLastVerifiedPS;
	private PreparedStatement updateLastVerifiedPS;
	private PreparedStatement updateLastVerifiedExpLogoutPS;
	private PreparedStatement queryLastLogoutZeroExpPS;
	private PreparedStatement queryLastLogoutNonzeroExpPS;

	private PreparedStatement deleteUserPS;

	private PreparedStatement queryBotPS;

	private PreparedStatement upsertMatchEventPS;
	private PreparedStatement queryMatchEventPS;
	private PreparedStatement garbageCollectMatchEventsPS;

}

@Getter
class FriendsOpinionImpl implements FriendsOpinion {

	private List<String> vouchers;
	private List<String> complainers;

	FriendsOpinionImpl(List<String> vouchers, List<String> complainers) {
		this.vouchers = vouchers;
		this.complainers = complainers;
	}
}

enum Opinion {
	VOUCHER("V"), COMPLAINT("C");
	private String dbCode;

	private Opinion(String dbCode) {
		this.dbCode = dbCode;
	}

	@Override
	public String toString() {
		return dbCode;
	}

	static public Opinion toEnum(String code) {
            return switch (code.charAt(0)) {
                case 'V' -> VOUCHER;
                case 'C' -> COMPLAINT;
                default -> null;
            };
	}
}
