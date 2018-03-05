package net.sf.repbot.dagger;

import javax.inject.Singleton;

import dagger.Component;
import net.sf.repbot.RepBot;
import net.sf.repbot.command.AlertCommand;
import net.sf.repbot.command.AskCommand;
import net.sf.repbot.command.ComplainCommand;
import net.sf.repbot.command.ConversationCommand;
import net.sf.repbot.command.DefaultCommand;
import net.sf.repbot.command.HelpCommand;
import net.sf.repbot.command.ListCommand;
import net.sf.repbot.command.ListFriendsCommand;
import net.sf.repbot.command.VouchCommand;
import net.sf.repbot.command.WithdrawCommand;

@Component(modules = RepBotModule.class)
@Singleton
public interface RepbotComponent {
	
	void inject(RepBot repbot);
	
	/*
	 * Commands
	 */
	
	AskCommand ask();
	ListFriendsCommand friends();
	ListCommand list();
	ComplainCommand complain();
	VouchCommand vouch();
	WithdrawCommand withdraw();
	AlertCommand alert();
	DefaultCommand defaultCommand();
	HelpCommand help();
	ConversationCommand conversation();
	
}
