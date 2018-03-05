package net.sf.repbot.server;

@FunctionalInterface
public interface NoUserListener {

	/** Called on a timeout. */
    void onNoUser();    
}
