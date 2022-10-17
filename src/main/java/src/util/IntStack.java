package src.util;

public final class IntStack {
    public IntStack() {
        ints = new int[DEFAULT_SIZE];
    }

    public int peek() {
        return ints[size - 1];
    }

    public int pop() {
        return ints[--size];
    }

    public void push(int n) {
        if (size == ints.length)
            resize();
        ints[size++] = n;
    }

    public void popIncrement() {
        ++ints[(--size) - 1];
    }

    public void popSubtract(int by) {
        ints[(--size)  - 1] -= by;
    }

    private static final int DEFAULT_SIZE = 32, GROWTH_FACTOR = 2;

    private int[] ints;
    private int size = 0;

    private void resize() {
        int[] realloc = new int[ints.length * GROWTH_FACTOR];
        System.arraycopy(ints, 0, realloc, 0, ints.length);
        ints = realloc;
    }
}
