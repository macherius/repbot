package net.sf.repbot.command;

import java.util.Random;

import net.sf.repbot.server.Server;

/** Some users "talk" to repbot, saying things like 'hello', 'thanks', etc.
 *  This command implements proper responses.
 *
 *  @author  Avi Kivity
 */
public class ConversationCommand implements Command {
    
    private Server server;
    private String[] responses;
    private Random random = new Random();
    
    /** Creates a new instance of ConversationCommand */
    public ConversationCommand(Server server) {
        this.server = server;
    }
    
    /** Executes the command.
     *  @param user  command requestor
     *  @param arguments command arguments.  */
    @Override
    public void execute(String user, String command, String[] arguments) {
        server.tell(user, responses[random.nextInt(responses.length)]);
    }
    
    public ConversationCommand responses(String[] responses) {
        this.responses = responses;
        return this;
    }
}
