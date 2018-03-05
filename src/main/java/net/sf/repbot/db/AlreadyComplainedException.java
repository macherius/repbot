package net.sf.repbot.db;

@SuppressWarnings("serial")
public class AlreadyComplainedException extends Exception {
    public AlreadyComplainedException() { super(); }
    public AlreadyComplainedException( String s ) { super( s ); }
}

