package net.sf.repbot.db;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

// Remember, these methods are used after confirming that the users involved
// exist on fibs.

public interface UserDB {

    void complain(String fromWho, int experience, String aboutWho)
            throws AlreadyComplainedException, AlreadyVouchedException, UserDBException;

    List<String> getComplainers(String user)
            throws NoSuchUserException, UserDBException;

    List<String> getComplaints(String inputProvider) throws UserDBException;

    List<String> getVouches(String inputProvider) throws UserDBException;

    List<String> getVouchers(String user)
            throws NoSuchUserException, UserDBException;
    
    int getReputation(String user)
            throws NoOpinionException, UserDBException;

    void vouch(String fromWho, int experience, String aboutWho)
            throws AlreadyVouchedException, AlreadyComplainedException, UserDBException;

    void withdraw(String fromWho, String aboutWho)
            throws UserDBException, NoOpinionException;
    
    Collection<String> getUserNames() throws UserDBException;
    
    FriendsOpinion getFriendsOpinion(String asker, String about) throws UserDBException;
    
    Collection<String> getNews(String user) throws UserDBException, NoSuchUserException;
    
    void markAsUser(String user) throws UserDBException;

    boolean isKnownUser(String user) throws UserDBException;
    
    void markNewsAsRead(String user, int items) throws UserDBException, NoSuchUserException;
    
    void setNews(Collection<String> news) throws UserDBException;
    
    boolean getAlert(String user) throws UserDBException;
    
    void setAlert(String user, boolean alert) throws UserDBException;
    
    boolean isBot(String user) throws UserDBException;
    
    String getLastVerifiedUser() throws UserDBException;

    String getLastLogoutZeroExpUser() throws UserDBException;
    
    String getLastLogoutNonzeroExpUser() throws UserDBException;
    
    void setLastVerified(String user, Instant when) throws UserDBException;
    
    void setLastVerifiedExpLogout(String user, Instant verified, int exp, Instant logout)
	throws UserDBException;
    
    void deleteUser(String name) throws UserDBException;
    
    void insertOrUpdateMatchEvent(String player1, String player2) throws UserDBException;
    void garbageCollectMatchEvents() throws UserDBException;
    boolean hasRecentMatchEvent(String user1, String user2) throws UserDBException;    
}

