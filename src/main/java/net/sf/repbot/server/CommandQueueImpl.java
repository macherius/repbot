package net.sf.repbot.server;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import lombok.Getter;
import net.sf.repbot.preferences.CommandQueuePreferences;
import net.sf.repbot.server.FibsListener.LoginConnection;
import net.sf.repbot.services.Executor;

/** Command queue implementation
 *
 *  @author  Avi Kivity
 */
public class CommandQueueImpl implements CommandQueue {

    private LoginConnection connection;
    private Executor cron;
    private ConcurrentLinkedQueue<Handle> queue = new ConcurrentLinkedQueue<Handle>();
    private HandleImpl current = null;
    private CommandQueuePreferences prefs;
    
    /** Creates a new instance of CommandQueueImpl */
    public CommandQueueImpl(CommandQueuePreferences prefs, LoginConnection connection, Executor cron) {
        this.connection = connection;
        this.cron = cron;
        this.prefs = prefs;
    }
    
    /** Enqueues a command.
     * @param command   complete fibs command
     * @param Listener  will receive fibs responses to the command
     * @return queue handle, which should be acknowledged when the command
     *         completes.  */
    @Override
    public Handle send(String command, Listener listener) {
        Handle handle = new HandleImpl(command, listener);
        queue.offer(handle);
        dispatchNextFromQ();
        return handle;
    }
    
    /** Processes the command queue. */
    private void dispatchNextFromQ() {
        try {
            if (current != null || queue.isEmpty())
                return;
            current = (HandleImpl)queue.poll();
            connection.send(current.getCommand());
            cron.addTimeout(this, prefs.getTimeout(), prefs.getUnit());
        } catch (IOException e) {
        	// FIXME: This means we failed to send a command to fibs via socket
            throw new RuntimeException("Failed to process command queue", e);
        }
    }
    
    /** Called on a received line.  Dispatches the line to the current command
     *  listener. */
    @Override
    public void onLine(String line, FibsListener connection) {
        if (current == null)
            return;
        current.getListener().onLine(line, connection);
    }
    
    /** Called on a timeout.  */
	@Override
    public void onTimeout() {
        try {
            current.getListener().onTimeout();
        } catch (Exception e) {
            e.printStackTrace();
        }
        current = null;
        dispatchNextFromQ();
    }
    
	@Getter
    private class HandleImpl implements Handle {
        
        private final String command;
        private final Listener listener;
        
        public HandleImpl(String command, Listener listener) {
            this.command = command;
            this.listener = listener;
        }
        
        /** Signals that the command has been acknowledged by fibs.  */
        @Override
        public void acknowledge() {
            cron.removeTimeout(CommandQueueImpl.this);
            current = null;
            dispatchNextFromQ();
        }
    }
}
