package net.sf.repbot.command; 

import net.sf.repbot.preferences.AdminPreferences;
import net.sf.repbot.server.Server;
import net.sf.repbot.util.DisclaimerAppender;

import java.util.*;
import java.io.IOException;
import java.io.InputStream;

// Generic information about RepFibsBot sent back for every tell

public class HelpCommand implements Command {

    // FIXME: This is the inherited type, but we know it is <String, String>
    private Map<Object, Object> help; 
    private final Server server;
    private String[] admin;

    public HelpCommand(AdminPreferences admin, Server server) {
    	this.server = server;
    	this.admin = admin.getAdmin();
        try (InputStream is = getClass().getResourceAsStream("/help.properties");) {
            Properties props = new Properties();
            props.load(is);
            help = props;
        } catch (IOException e) {
            throw new RuntimeException("Failed loading help", e);
        }
    }

    /** Executes the command.
     *  @param user  command requester
     *  @param arguments command arguments.  */
    @Override
    public void execute(String user, String command, String[] arguments) {
        String topic = "general";
        if (arguments.length > 0)
            topic = arguments[0];
        
        // If the topic is unknown, reply with general help
        if (help.get(topic + ".1") == null) {
            topic = "general";
        }
        
        List<String> reply = new ArrayList<>();
        int line = 1;
        
        // Help text for an admin
        if ("general".equals(topic)) {
        	while (help.get("general." + line) != null) {
        		reply.add(help.get(topic + '.' + line++).toString());
        	}
        	if (contains(admin, user)) {
        		line = 1;
            	while (help.get("general.admin." + line) != null) {
            		reply.add(help.get("general.admin." + line++).toString());
            	}
        	}
        	line = 1;
        	while (help.get("general.trailer." + Integer.toString(line)) != null) {
        		reply.add(help.get("general.trailer." + line++).toString());
        	}
        // and for mere mortals
        } else {
        	while (help.get(topic + '.' + line) != null) {
        		reply.add(help.get(topic + '.' + line++).toString());
        	}
        }
        DisclaimerAppender.appendDisclaimerRandomly(reply);
        server.tell(user, reply);
    }
    
    private final static <T> boolean contains(final T[] array, final T v) {
    	if (array.length == 0) {
    		return false;
    	}
        for (final T e : array)
            if (e == v || v != null && v.equals(e))
                return true;

        return false;
    }

}