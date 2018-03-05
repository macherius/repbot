package net.sf.repbot.server;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import net.sf.repbot.server.FibsListener.LoginConnection;
import net.sf.repbot.server.command.Message;
import net.sf.repbot.server.command.MessageImpl;
import net.sf.repbot.server.command.Shout;
import net.sf.repbot.server.command.ShoutImpl;
import net.sf.repbot.server.command.Spell;
import net.sf.repbot.server.command.SpellImpl;
import net.sf.repbot.server.command.Tell;
import net.sf.repbot.server.command.TellImpl;
import net.sf.repbot.server.command.Version;
import net.sf.repbot.server.command.VersionImpl;
import net.sf.repbot.server.command.Who;
import net.sf.repbot.server.command.WhoImpl;
import net.sf.repbot.server.command.Whois;
import net.sf.repbot.server.command.WhoisImpl;

/**
 * Implementation of the server interface.
 *
 * @author Avi Kivity
 * @author Ingo Macherius
 */
public class ServerImpl implements Server {

	private Tell tell;
	private Whois whois;
	private Spell spell;
	private Shout shout;
	private Who who;
	private Message message;
	private Version version;

	/** Creates a new instance of ServerImpl */
	public ServerImpl(FibsListener connection) {
	}

	/** Tell a user something. */
	@Override
	public void tell(String who, String what) {
		tell(who, what, new Tell.DefaultReplyListener());
	}

	/** Tells a user something. */
	@Override
	public void tell(String who, String what, Tell.ReplyListener listener) {
		tell.tell(who, what, listener);
	}

	/** Tells a user many things. */
	@Override
	public void tell(String who, Collection<String> messages, Tell.ReplyListener listener) {
		new SerialTell(who, messages, listener).onTell();
	}

	/** Tells a user many things. */
	@Override
	public void tell(String who, Collection<String> messages) {
		tell(who, messages, new Tell.DefaultReplyListener());
	}

	/** Messages a user something. */
	@Override
	public void message(String who, String what, Message.ReplyListener listener) {
		message.message(who, what, listener);
	}

	/** Messages a user something. */
	@Override
	public void message(String who, String what) {
		message(who, what, new Message.DefaultReplyListener());
	}

	/** Queries user info. */
	@Override
	public void whois(String who, Whois.ReplyListener listener) {
		whois.query(who, listener, 2000);
	}

	/** Correct spelling of a user's name. Faster than whois. */
	@Override
	public void spell(String who, Spell.ReplyListener listener) {
		spell.query(who, listener, 3000);
	}

	/** Shout something. */
	@Override
	public void shout(String what, Shout.ReplyListener listener) {
		shout.shout(what, listener);
	}

	/** Shout something. */
	@Override
	public void shout(String what) {
		shout(what, new Shout.DefaultReplyListener());
	}

	/** Gets information about all online users. */
	@Override
	public void who(Who.ReplyListener listener) {
		who.query(listener); // TODO: timeout 3s
	}

	@Override
	public void version(Version.ReplyListener listener) {
		version.version(listener);
	}

	@Override
	public void version() {
		version(new Version.DefaultReplyListener());
	}

	private class SerialTell implements Tell.ReplyListener {

		private String who;
		private Iterator<String> messages;
		private Tell.ReplyListener listener;

		public SerialTell(String who, Collection<String> what, Tell.ReplyListener listener) {
			this.who = who;
			messages = (what == null) ? Collections.emptyIterator() : what.iterator();
			this.listener = listener;
		}

		/** Called on timeout. */
		@Override
		public void onNoUser() {
			listener.onNoUser();
		}

		/** Called when the tell has been executed. */
		@Override
		public void onTell() {
			if (messages.hasNext())
				tell(who, messages.next(), this);
			else
				listener.onTell();
		}

		/** Called on timeout. */
		@Override
		public void onTimeout() {
			listener.onTimeout();
		}
	}

	@Override
	public void onConnect(LoginConnection connection) {
		tell = new TellImpl(connection.getQueue());
		whois = new WhoisImpl(connection.getQueue());
		spell = new SpellImpl(whois);
		shout = new ShoutImpl(connection.getQueue());
		who = new WhoImpl(connection.getQueue());
		version = new VersionImpl(connection.getQueue());
		message = new MessageImpl(connection.getQueue());
	}

	@Override
	public void onDisconnect(LoginConnection connection) {
		// FIXME: do we need to clean up something here?
	}
}
