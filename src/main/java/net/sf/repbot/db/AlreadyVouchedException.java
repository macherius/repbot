package net.sf.repbot.db;

@SuppressWarnings("serial")
public class AlreadyVouchedException extends Exception {
    public AlreadyVouchedException() { super(); }
    public AlreadyVouchedException( String s ) { super( s ); }
}

