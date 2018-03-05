package net.sf.repbot.db;

@SuppressWarnings("serial")
public class UserDBException extends Exception {
    public UserDBException() {}
    public UserDBException( String s ) { super( s ); }
    public UserDBException( String s, Throwable t ) { super( s, t ); }

}
