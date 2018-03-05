package net.sf.repbot.services;

import java.util.concurrent.TimeUnit;

import net.sf.repbot.server.FibsListener;
import net.sf.repbot.server.LineListener;
import net.sf.repbot.server.TimeoutListener;

/** This LineListener makes sure there is some activity on the connection,
 *  so we can time it out to discover problems.
 *  <p>Note this is different from {@link Heartbeat}, which generates
 *  activity to keep fibs from logging us out.
 *
 *  @author  Avi Kivity
 */
public class ConnectionActivity implements LineListener, TimeoutListener {

    private long timeout;
    private TimeUnit timeoutUnit;
    private Executor cron;
    private FibsListener fibs;
    private boolean suspected = true;
    
    /** Creates a new instance of ConnectionActivity */
    public ConnectionActivity(long timeout, TimeUnit timeoutUnit, FibsListener fibs, Executor cron) {
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.fibs = fibs;
        this.cron = cron;
        cron.addTimeout(this, timeout, timeoutUnit);
    }
    
    /** Called on a received line.  Reschedules the timeout. */
    @Override
    public void onLine(String line, FibsListener connection) {
        suspected = false;
        cron.removeTimeout(this);
        cron.addTimeout(this, timeout, timeoutUnit);
    }
    
    /** Called on a timeout. Sends some dummy command. */
    @Override
    public void onTimeout() {
        if (suspected)
            fibs.reconnect();
        cron.addTimeout(this, timeout, timeoutUnit);
        suspected = true;
        try {
        	/*
        	 * 
        	 * F I X M E 
        	 * FIXME
        	 * 
        	 * Broken
        	 */
            // fibs.getQueue().send("show max");
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate activity to fibs", e);
        }
    }
    
}
