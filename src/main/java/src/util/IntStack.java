package src.util;

import java.util.Arrays;
import java.util.NoSuchElementException;

/**
 * <p>
 *     Basic implementation of an {@code int} stack.
 * </p>
 * <p>
 *     Advantages over {@link java.util.Deque} include absence of auto-boxing and unboxing.
 * </p>
 */
public final class IntStack {
    /**
     * Large enough to hold a reasonable amount of integers used to hold a value per recursive method call.
     */
    private static final int DEFAULT_CAPACITY = 32;

    private static final int GROWTH_FACTOR = 2;

    private static String underflowAt(int size) {
        return "Stack underflow at " + size + ".";
    }

    private int[] ints;
    private int size = 0;

    /**
     * Constructs a new {@code IntStack} whose capacity is the default capacity.
     */
    public IntStack() {
        ints = new int[DEFAULT_CAPACITY];
    }

    /**
     * Increments top of stack.
     *
     * @throws NoSuchElementException stack is empty
     */
    public void increment() {
        try {
            ++ints[size - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NoSuchElementException(underflowAt(size - 1), e);
        }
    }

    /**
     * @return top of stack
     * @throws NoSuchElementException stack is empty
     */
    public int peek() {
        try {
            return ints[size - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NoSuchElementException(underflowAt(size - 1), e);
        }
    }

    /**
     * Pops top integer from stack.
     *
     * @return previous top of stack
     * @throws NoSuchElementException stack is empty
     */
    public int pop() {
        try {
            return ints[--size];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NoSuchElementException(underflowAt(size), e);
        }
    }

    /**
     * Pushes {@code n} to top of stack.
     */
    public void push(int n) {
        if (size == ints.length)
            resize();
        ints[size++] = n;
    }

    /**
     * Pops top integer from stack. Increments new top of stack.
     */
    public void popIncrement() {
        try {
            ++ints[(--size) - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new NoSuchElementException(underflowAt(size), e);
        }
    }

    /**
     * Resizes underlying array, multiplying by {@link #GROWTH_FACTOR}.
     */
    private void resize() {
        int[] realloc = new int[ints.length * GROWTH_FACTOR];
        System.arraycopy(ints, 0, realloc, 0, ints.length);
        ints = realloc;
    }
}
