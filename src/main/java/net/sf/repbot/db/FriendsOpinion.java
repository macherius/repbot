package net.sf.repbot.db;

import java.util.List;

/** Represents a user's friends' opinions about a player. 
 *
 *  @author  Avi Kivity
 */
public interface FriendsOpinion {
    
    List<String> getComplainers();
    
    List<String> getVouchers();
    
}
