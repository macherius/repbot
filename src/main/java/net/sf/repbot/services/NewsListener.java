package net.sf.repbot.services;

import java.util.concurrent.TimeUnit;
import java.util.regex.*;

import net.sf.repbot.server.FibsListener;
import net.sf.repbot.server.LineListener;

/** NewsListener waits for the user to say something, then relays any
 *  news to the user.
 *
 *  @author  Avi Kivity
 */
public class NewsListener implements LineListener {
    
    private NewsServer news;
    private Executor cron;
    private Pattern says = Pattern.compile("(\\S+) says:.*");
    
    /** Creates a new instance of NewsCommand */
    public NewsListener(NewsServer news, Executor cron) {
        this.news = news;
        this.cron = cron;
    }
    
    /** Called on a received line.  */
    public void onLine(String line, FibsListener connection) {
        Matcher m;
        if ((m = says.matcher(line)).matches()) {
            // send news after a delay to avoid overloading the user.
            cron.addTimeout(
            		() -> {news.send(m.group(1));},
            		4L, TimeUnit.SECONDS);
        }
    }    
}
