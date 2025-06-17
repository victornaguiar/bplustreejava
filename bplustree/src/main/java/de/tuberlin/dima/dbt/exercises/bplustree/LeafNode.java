package de.tuberlin.dima.dbt.exercises.bplustree;

import java.util.Arrays;

public class LeafNode extends Node {

    private String[] values;
    private LeafNode nextLeaf;

    public LeafNode(int capacity) {
        this(new Integer[0], new String[0], capacity);
        this.currentSize = 0;
    }

    public LeafNode(Integer[] keys, String[] values, int capacity) {
        super(Arrays.copyOf(keys, capacity + 1), capacity);
        assert keys.length == values.length : "Keys and values must match in length";
        this.values = Arrays.copyOf(values, capacity + 1);
        this.currentSize = keys.length;
    }

    public String[] getValues() {
        return values;
    }

    public void setValues(String[] values) {
        this.values = Arrays.copyOf(values, this.values.length);
        this.currentSize = (int) Arrays.stream(keys).filter(k -> k != null).count();
    }

    public LeafNode getNextLeaf() {
        return nextLeaf;
    }

    public void setNextLeaf(LeafNode nextLeaf) {
        this.nextLeaf = nextLeaf;
    }

    @Override
    public Object[] getPayload() {
        return getValues();
    }

    @Override
    public void setPayload(Object[] payload) {
        setValues((String[]) payload);
    }

    public String toString() {
        return new BPlusTreePrinter(this).toString();
    }
}
