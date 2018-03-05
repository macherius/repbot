package net.sf.repbot.command;

/** A dispatchable command.
 *
 * @author Avi Kivity
 */
@FunctionalInterface
public interface Command {
    
    /** Executes the command.
     *  @param user  command requestor
     *  @param arguments command arguments. */
    void execute(String user, String command, String[] arguments);
    
}
