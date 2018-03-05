package net.sf.repbot.server.command;

import java.util.*;
import java.util.regex.*;
import lombok.Getter;
import lombok.ToString;
import net.sf.repbot.server.CommandQueue;
import net.sf.repbot.server.FibsListener;

/** Implementation of an asynchronous who database.
 *
 *  <p>Apparently fibs can only handle one request at a time, so we queue 
 *  the requests and issue them one at a time.
 *
 *  @author  Avi Kivity
 */
public class WhoImpl implements Who {
    
    CommandQueue queue;
    
    /** Creates a new instance of WhoisImpl */
    public WhoImpl(CommandQueue queue) {
    	this.queue = queue;
    }
    
    /** Submits an asynchronous request to the whois database.  */
    @Override
    public void query(ReplyListener replyListener) {
        Request request = new Request(replyListener);
        CommandQueue.Handle handle = queue.send("rawwho", request);
        request.setHandle(handle);
    }
    
    /** A whois request. */
    private static class Request extends AbstractCommandQueueListener {

    	private final static Pattern p_who = Pattern.compile("who: *\\d+\\s+(-|P|R])(-|W)(-|A)\\s+(\\S+)\\s+(\\d+\\.\\d+)\\s+(\\d+)\\s+\\d+:\\d+\\s+\\d+:\\d+\\s+(\\S+)\\s*");
        private final static Pattern p_end = Pattern.compile("who:end");
        
        private Collection<Who.Info> reply = new ArrayList<>();
        @Getter
        private ReplyListener listener;

        public Request(ReplyListener listener) {
            this.listener = listener;
        }
        
        /** Called on a received line.  */
        @Override
        public void onLine(String line, FibsListener connection) {
            try {
                Matcher m;
                m = p_end.matcher(line);
                if (m.matches()) {
                    listener.onReply(reply);
                    handle.acknowledge();
                    return;
                }
                m = p_who.matcher(line);
                if (m.matches()) {
                    InfoImpl record = new InfoImpl();
                    record.ready = "R".equals(m.group(1));
                    record.playing = "P".equals(m.group(1));
                    record.watching = "W".equals(m.group(2));
                    record.away = "A".equals(m.group(3));
                    record.name = m.group(4);
                    record.rating = Double.parseDouble(m.group(5));
                    record.experience = Integer.parseUnsignedInt(m.group(6));
                    record.host = m.group(7);
                    reply.add(record);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                listener.onTimeout();
                handle.acknowledge();
            }
        }
    }
   
    @Getter
    @ToString
    /** A whois reply. */
    private static class InfoImpl implements Info {
        
        /** The user' name as given by the server (capitalization!).  */
        String name;
        /** Is the user ready to play?  */
        boolean ready;
        /** Is the user playing right now?  */
        boolean playing;
        /** Is the user currently watching?  */
        boolean watching;
        /** Is the user away?  */
        boolean away;
        /** The user's rating.  */
        double rating;
        /** The user's experience.  */
        int experience;
        /** The host name from which the user last connected.  */
        String host;
    }    
}
