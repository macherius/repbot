package net.sf.repbot.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import net.sf.repbot.db.UserDBException;
import net.sf.repbot.server.Monitor;
import net.sf.repbot.db.UserDB;

public class MatchEventListener extends Monitor.DefaultListener {

	private Map<String, String> inMatch = new HashMap<>();
	private ReentrantLock inMatchLock = new ReentrantLock();

	private final UserDB userDB;
	private final Logger logger;

	public MatchEventListener(UserDB userDB, Logger logger) {
		this.userDB = userDB;
		this.logger = logger;
	}

	@Override
	public void onLogout(String user) {
		onMatchEnd(user);
	}

	@Override
	public void onNetworkError(String user) {
		onMatchEnd(user);
	}

	@Override
	public void onAdminClose(String user, String admin) {
		onMatchEnd(user);
	}

	@Override
	public void onClosedOldConnection(String user) {
		onMatchEnd(user);
	}

	@Override
	public void onConnectionTimedOut(String user) {
		onMatchEnd(user);
	}

	@Override
	public void onDrop(String user) {
		onMatchEnd(user);
	}

	@Override
	public void onEndMatch(String user1, String user2, int length, int points1, int points2) {
		onMatchStart(user1, user2);
	}

	@Override
	public void onResumeMatch(String user1, String user2, int length) {
		onMatchStart(user1, user2);
	}

	@Override
	public void onStartMatch(String user1, String user2, int length) {
		onMatchStart(user1, user2);
	}

	// Utility Functions

	private void onMatchStart(String player1, String player2) {
		inMatchLock.lock();
		try {
			inMatch.put(player1, player2);
			inMatch.put(player2, player1);
			userDB.insertOrUpdateMatchEvent(player1, player2);
		} catch (UserDBException e) {
			inMatch.remove(player2, player1);
			inMatch.remove(player1, player2);
			logger.logTimestampedLine("Failed to register starting match in DB", e);
		} finally {
			inMatchLock.unlock();
		}
	}

	private void onMatchEnd(String player) {
		inMatchLock.lock();
		try {
			String opponent = inMatch.get(player);
			if (opponent != null) {
				inMatch.remove(player, opponent);
				inMatch.remove(opponent, player);
				userDB.insertOrUpdateMatchEvent(player, opponent);
			}
		} catch (UserDBException e) {
			logger.logTimestampedLine("Failed to register ending match in DB", e);
		} finally {
			inMatchLock.unlock();
		}
	}
}
