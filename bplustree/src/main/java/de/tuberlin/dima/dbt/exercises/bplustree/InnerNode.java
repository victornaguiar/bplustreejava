package de.tuberlin.dima.dbt.exercises.bplustree;

import java.util.Arrays;
import java.util.stream.Collectors;

public class InnerNode extends Node {

    private Node[] children;

    private int childrenCurrentSize;

    public InnerNode(int capacity) {
        this(new Integer[] {}, new Node[] {null}, capacity);
        this.children = new Node[capacity + 1];
        this.currentSize = 0;
        this.childrenCurrentSize = 0;
        this.childrenCurrentSize = children.length;
    }

    public InnerNode(Integer[] keys, Node[] children, int capacity) {
        super(keys, capacity);
        assert keys.length == children.length - 1;
        this.children = Arrays.copyOf(children, capacity + 1);
        this.currentSize = keys.length;
        this.childrenCurrentSize = children.length;
    }

    public Node[] getChildren() {
        return children;
    }

    public int getChildrenCurrentSize() {
        return childrenCurrentSize;
    }

    public void setChildrenCurrentSize(int childrenCurrentSize) {
        this.childrenCurrentSize = childrenCurrentSize;
    }

    public void setChildren(Node[] children) {
        this.children = Arrays.copyOf(children, this.children.length);
        this.currentSize = Arrays.stream(keys).filter(k -> k != null).toArray().length;
        this.childrenCurrentSize = children.length;
    }

    @Override
    public Object[] getPayload() {
        return getChildren();
    }

    @Override
    public void setPayload(Object[] payload) {
        setChildren((Node[]) payload);
    }

    public String toString() {
        String keyList = Arrays.stream(keys).map(String::valueOf)
                .collect(Collectors.joining(", "));
        String childrenList = Arrays.stream(children).map(String::valueOf)
                .collect(Collectors.joining(", "));
        return "keys: [" + keyList + "]; " + "children: [" + childrenList + "]";
    }

}
