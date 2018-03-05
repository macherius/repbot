package net.sf.repbot.services;

import net.sf.repbot.db.*;
import net.sf.repbot.server.Server;
import net.sf.repbot.server.command.*;

import java.util.*;
import java.io.*;
import java.nio.charset.Charset;

/** Sends users news about new features. Each news item is sent at most once
 *  per user.
 *
 *  @author Avi Kivity
 */
public class NewsServer {
	
	private final static String NEWS_PATH="/news.txt";
    
    private Server server;
    private UserDB db;
    private Logger logger;
    
    /** Creates a new instance of NewsServer */
    public NewsServer(Server server, UserDB db, Logger logger) {
        this.server = server;
        this.db = db;
        this.logger = logger;
        readNews();
    }
    
    /** Sends a user news if they did not receive it already. */
    public void send(String user) {
		try {
			Collection<String> news = db.getNews(user);
			if (news.isEmpty())
				return;
			server.tell(user, news, new TellDoneListener(user, news.size()));
		} catch (UserDBException | NoSuchUserException e) {
			e.printStackTrace();
		}
	}
    
    /** At the end of the tell, marks the news as read. */
    private class TellDoneListener extends Tell.DefaultReplyListener {
        
        private String user;
        private int count;
        
        public TellDoneListener(String user, int count) {
            this.user = user;
            this.count = count;
        }
        
        public void onTell() {
            try {
                db.markNewsAsRead(user, count);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
	/** Read news from a file and insert into a database. */
	private void readNews() {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(getClass().getResourceAsStream(NEWS_PATH), Charset.forName("US-ASCII")));) {
			Collection<String> news = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.matches("\\s*(#.*)?"))
					continue;
				news.add(line);
			}
			db.setNews(news);
		} catch (IOException | UserDBException e) {
			logger.logTimestampedLine("Failed reading news from '" + NEWS_PATH + "'.", e);
		}
	}
    
}
