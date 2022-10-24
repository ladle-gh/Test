package src.util;

import java.util.Arrays;

/**
 * <p>
 *     Basic implementation of an {@code int} stack.
 * </p>
 * <p>
 *     Advantages over {@link java.util.Deque} include:
 *      <ul>
 *          <li>Absence of auto-boxing and unboxing</li>
 *          <li>{@link #pop(int) Multi-pop} functionality</li>
 *      </ul>
 * </p>
 */
public final class IntStack {
    /**
     * Large enough to hold a reasonable amount of integers used to hold a value per recursive method call.
     */
    private static final int DEFAULT_CAPACITY = 32;

    private static final int GROWTH_FACTOR = 2;

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
     * @throws StackUnderflowException stack is empty
     */
    public void increment() {
        try {
            ++ints[size - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new StackUnderflowException(size - 1, e);
        }
    }

    /**
     * @return top of stack
     * @throws StackUnderflowException stack is empty
     */
    public int peek() {
        try {
            return ints[size - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new StackUnderflowException(size - 1, e);
        }
    }

    /**
     * Pops top integer from stack.
     *
     * @return previous top of stack
     * @throws StackUnderflowException stack is empty
     */
    public int pop() {
        try {
            return ints[--size];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new StackUnderflowException(size, e);
        }
    }

    /**
     * Pops {@code count} integers from top of stack.
     */
    public void pop(int count) {
        size -= count;
    }

    /**
     * Pushes {@code n} to top of stack.
     * @throws StackUnderflowException call to {@link #pop(int)} causing underflow made beforehand
     */
    public void push(int n) {
        if (size == ints.length)
            resize();
        try {
            ints[size++] = n;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new StackUnderflowException(size, e);
        }
    }

    /**
     * Pops top integer from stack. Increments new top of stack.
     */
    public void popIncrement() {
        ++ints[(--size) - 1];
    }

    /**
     * @return sum of all integers in stack
     */
    public int sum() {
        return Arrays.stream(ints).sum();
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
