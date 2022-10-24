package src.script;

public class NoSuchChildException extends RuntimeException {
    public NoSuchChildException(int at) {
        super("Attempted access of non-existent child " + at + ".");
    }
}
