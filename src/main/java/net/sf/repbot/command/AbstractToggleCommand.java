package net.sf.repbot.command;

import net.sf.repbot.server.Server;

/** A base class for on/off type commands. A toggle command 'x' has three
 *  forms:
 *  <dl>
 *     <dt>x on</dt><dd>turn on the toggle</dd>
 *     <dt>x off</dt><dd>turn off the toggle</dd>
 *     <dt>x</dt><dd>check the toggle status</dd>
 *  </dl>
 *
 *  @author Avi Kivity
 */
public abstract class AbstractToggleCommand implements Command {
    
    private Server server;
    private String onMessage;
    private String offMessage;
    
    /** Creates a new instance of Toggle.
     *  @param onMessage user notification when toggle is turned on
     *  @param ofMessage user notification when toggle is turned off
     */
    public AbstractToggleCommand(Server server, String onMessage, String offMessage) {
        this.server = server;
        this.onMessage = onMessage;
        this.offMessage = offMessage;
    }
    
    /** Executes the command.
     *  @param user  command requestor
     *  @param args command arguments.  */
	@Override
    public void execute(String user, String command, String[] args) {
        try {
            if (args.length == 0)
                showStatus(user);
            else if (args.length == 1 && "on".equalsIgnoreCase(args[0])) {
                setStatusOn(user);
                showStatus(user);
            } else if (args.length == 1 && "off".equalsIgnoreCase(args[0])) {
                setStatusOff(user);
                showStatus(user);
            } else 
                server.tell(user, "I don't understand you. " 
                    + "Try 'tell RepBot " + command + "', "
                    + "'tell RepBot " + command + " on', or "
                    + "'tell RepBot " + command + " off'.");
        } catch (Exception e) {
        	e.printStackTrace();
            server.tell(user, "Internal error, apologies");
        }
    }
        
    /** Shows the current status. */
    private void showStatus(String user) {
        server.tell(user, 
                    getStatus(user) ? onMessage : offMessage);
    }
    
    
    /** Sets the toggle status to 'on'. */
    protected abstract void setStatusOn(String user);
    
    /** Sets the toggle status to 'off'. */
    protected abstract void setStatusOff(String user);
    
    /** Gets the toggle status. */
    protected abstract boolean getStatus(String user);
    
}
