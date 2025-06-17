package de.tuberlin.dima.dbt.exercises.bplustree;

import java.util.Arrays;

public abstract class Node {

    protected Integer[] keys;
    protected int capacity;

    protected int currentSize;

    public Node(Integer[] keys, int capacity) {
        assert keys.length <= capacity + 1;
        this.keys = Arrays.copyOf(keys, capacity + 1);
        this.capacity = capacity;
        this.currentSize = keys.length;
    }

    public Integer[] getKeys() {
        return keys;
    }

    public void setKeys(Integer[] keys) {
        this.keys = Arrays.copyOf(keys, this.keys.length);
        this.currentSize = keys.length;
    }

    public int getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(int currentSize) {
        this.currentSize = currentSize;
    }
    public int getCapacity() {
        return capacity;
    }

    public abstract Object[] getPayload();

    public abstract void setPayload(Object[] payload);

}
