package net.sf.repbot.command;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.sf.repbot.db.UserDB;
import net.sf.repbot.server.Server;

/**
 *
 *  @author Avi Kivity
 */
@Singleton
public class AlertCommand extends AbstractToggleCommand {
    
    private UserDB db;
    private Map<String, Boolean> status = new HashMap<>(); // user -> Boolean
    
    /** Creates a new instance of AlertCommand */
    @Inject
    public AlertCommand(Server server, UserDB db) {
        super(server, 
              "RepBot will alert you when opinions about you are cast.",
              "RepBot will not alert you when opinions about you are cast.");
        this.db = db;
    }

    /** Sets the toggle status to 'on'. */
    @Override
    protected void setStatusOn(String user) {
        try {
            db.setAlert(user, true);
            status.put(user, Boolean.TRUE);
        } catch (Exception e) {
            throw new RuntimeException("Failed setting alert status for " + user, e);
        }
    }
    
    /** Sets the toggle status to 'off'. */
    @Override
    protected void setStatusOff(String user) {
        try {
            db.setAlert(user, false);
            status.put(user, Boolean.FALSE);
        } catch (Exception e) {
            throw new RuntimeException("Failed setting alert status for " + user, e);
        }
    }
    
    /** Gets the toggle status. */
    @Override
    protected boolean getStatus(String user) {
        try {
            if (!status.containsKey(user))
                status.put(user, Boolean.valueOf(db.getAlert(user)));
            return status.get(user).booleanValue();
        } catch (Exception e) {
            throw new RuntimeException("Failed setting alert status for " + user, e);
        }
    }    
}
