package net.sf.repbot.server.command;

import lombok.Getter;
import net.sf.repbot.server.CommandQueue;
import net.sf.repbot.server.FibsListener;

public class VersionImpl implements Version {

	private CommandQueue queue;
	
    public VersionImpl(CommandQueue queue) {
    	this.queue = queue;
    }
    
    private static class Request extends AbstractCommandQueueListener {
       
    	@Getter
        private ReplyListener listener;
        
        public Request(ReplyListener listener) {
            this.listener = listener;
        }
        
        /** Called on a received line.  */
        @Override
        public void onLine(String line, FibsListener connection) {
            if (line.startsWith("** Version ")) {
                handle.acknowledge();
            }
        }        
    }

	@Override
	public void version(ReplyListener listener) {
        Request request = new Request(listener);
        CommandQueue.Handle handle = queue.send("version", request);
        request.setHandle(handle);
	}
}
