package net.sf.repbot.command; 


/** A Command that does nothing, mainly to ignore ill-programmed bots derived
 *  off this codebase. */
public class NullCommand implements Command {

    /** Does nothing. */
	@Override
    public void execute(String user, String command, String[] arguments) {
    }

}
