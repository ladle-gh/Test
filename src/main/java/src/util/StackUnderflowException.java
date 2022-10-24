package src.util;

/**
 * Thrown by {@link IntStack} on stack underflow.
 */
public final class StackUnderflowException extends RuntimeException {
    /**
     * @param element non-existent element being accessed
     */
    public StackUnderflowException(int element, Throwable cause) {
        super("Attempted access of stack element " + element + ".", cause);
    }
}
