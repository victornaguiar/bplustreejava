package de.tuberlin.dima.dbt.exercises.bplustree;

import java.util.Arrays;

import java.util.Deque;
import java.util.LinkedList;

public class BPlusTree {

    private LeafNode findLeafNode(Integer key, Node node, Deque<InnerNode> parents) {
        if (node instanceof LeafNode) {
            return (LeafNode) node;
        } else {
            InnerNode innerNode = (InnerNode) node;
            if (parents != null) {
                parents.push(innerNode);
            }

            int i = 0;
            while (i < innerNode.getCurrentSize() && key >= innerNode.getKeys()[i]) {
                i++;
            }
            Node child = innerNode.getChildren()[i];
            return findLeafNode(key, child, parents);
        }
    }

    private String lookupInLeafNode(Integer key, LeafNode node) {
        for (int i = 0; i < node.getCurrentSize(); i++) {
            Integer currentKey = node.getKeys()[i];
            if (currentKey != null && currentKey.equals(key)) {
                return node.getValues()[i];
            }
        }
        return null;
    }

    private void insertIntoLeafNode(Integer key, String value, LeafNode node, Deque<InnerNode> parents) {
        int insertPos = 0;
        while (insertPos < node.getCurrentSize() && node.getKeys()[insertPos] < key) {
            insertPos++;
        }

        for (int i = node.getCurrentSize(); i > insertPos; i--) {
            node.getKeys()[i] = node.getKeys()[i - 1];
            node.getValues()[i] = node.getValues()[i - 1];
        }

        node.getKeys()[insertPos] = key;
        node.getValues()[insertPos] = value;
        node.setCurrentSize(node.getCurrentSize() + 1);

        System.out.println("Inserted key " + key + " at position " + insertPos + " in LeafNode.");
        System.out.println("LeafNode keys after insertion: " + Arrays.toString(node.getKeys()));
        System.out.println("LeafNode values after insertion: " + Arrays.toString(node.getValues()));
        System.out.println("LeafNode currentSize after insertion: " + node.getCurrentSize());

        if (node.getCurrentSize() > node.getCapacity()) {
            LeafNode newLeaf = new LeafNode(node.getCapacity());
            int mid = node.getCurrentSize() / 2;

            for (int i = mid; i < node.getCurrentSize(); i++) {
                newLeaf.getKeys()[i - mid] = node.getKeys()[i];
                newLeaf.getValues()[i - mid] = node.getValues()[i];
                node.getKeys()[i] = null;
                node.getValues()[i] = null;
            }
            newLeaf.setCurrentSize(node.getCurrentSize() - mid);
            node.setCurrentSize(mid);

            System.out.println("After splitting LeafNode:");
            System.out.println("Original LeafNode keys: " + Arrays.toString(node.getKeys()));
            System.out.println("Original LeafNode values: " + Arrays.toString(node.getValues()));
            System.out.println("Original LeafNode currentSize: " + node.getCurrentSize());

            System.out.println("New LeafNode keys: " + Arrays.toString(newLeaf.getKeys()));
            System.out.println("New LeafNode values: " + Arrays.toString(newLeaf.getValues()));
            System.out.println("New LeafNode currentSize: " + newLeaf.getCurrentSize());

            newLeaf.setNextLeaf(node.getNextLeaf());
            node.setNextLeaf(newLeaf);
            System.out.println("Linked new LeafNode to the leaf chain.");

            Integer separatorKey = newLeaf.getKeys()[0];
            System.out.println("Separator key to be inserted into parent: " + separatorKey);

            insertIntoParent(parents, node, newLeaf, separatorKey);
        }
    }

    private LeafNode splitLeafNode(LeafNode leaf, Deque<InnerNode> parents) {
        LeafNode newLeaf = new LeafNode(leaf.getCapacity());
        int mid = (leaf.getCurrentSize() + 1) / 2;

        for (int i = mid; i < leaf.getCurrentSize(); i++) {
            newLeaf.getKeys()[i - mid] = leaf.getKeys()[i];
            newLeaf.getValues()[i - mid] = leaf.getValues()[i];

            leaf.getKeys()[i] = null;
            leaf.getValues()[i] = null;
        }

        newLeaf.setCurrentSize(leaf.getCurrentSize() - mid);
        leaf.setCurrentSize(mid);

        newLeaf.setNextLeaf(leaf.getNextLeaf());
        leaf.setNextLeaf(newLeaf);

        Integer separatorKey = newLeaf.getKeys()[0];
        System.out.println("Separator key to be inserted into parent: " + separatorKey);

        insertIntoParent(parents, leaf, newLeaf, separatorKey);

        return newLeaf;
    }

    public void insert(Integer key, String value) {
        Deque<InnerNode> parents = new LinkedList<>();

        if (root instanceof LeafNode) {
            LeafNode leaf = (LeafNode) root;
            if (leaf.getCurrentSize() == leaf.getCapacity()) {
                LeafNode newLeaf = splitLeafNode(leaf, parents);

                InnerNode newRoot = new InnerNode(leaf.getCapacity());
                newRoot.getKeys()[0] = newLeaf.getKeys()[0];
                newRoot.getChildren()[0] = leaf;
                newRoot.getChildren()[1] = newLeaf;
                newRoot.setCurrentSize(1);
                newRoot.setChildrenCurrentSize(2);

                this.root = newRoot;
            }
        }

        LeafNode targetLeaf = findLeafNode(key, root, parents);
        insertIntoLeafNode(key, value, targetLeaf, parents);
    }

    private void insertInternal(InnerNode node, Integer key, String value, Deque<InnerNode> parents) {
        int i = 0;
        while (i < node.getCurrentSize() && key >= node.getKeys()[i]) {
            i++;
        }

        Node child = node.getChildren()[i];

        if (child instanceof InnerNode) {
            InnerNode innerChild = (InnerNode) child;
            if (innerChild.getCurrentSize() == innerChild.getCapacity()) {
                splitInnerNode(innerChild, parents);
                i = 0;
                while (i < node.getCurrentSize() && key >= node.getKeys()[i]) {
                    i++;
                }
                child = node.getChildren()[i];
            }
            insertInternal((InnerNode) child, key, value, parents);
        } else {
            LeafNode leafChild = (LeafNode) child;
            if (leafChild.getCurrentSize() == leafChild.getCapacity()) {
                splitLeafNode(leafChild, parents);

                if (key >= leafChild.getKeys()[leafChild.getCurrentSize() - 1]) {
                    leafChild = (LeafNode) node.getChildren()[i + 1];
                }
            }
            insertIntoLeafNode(key, value, leafChild, parents);
        }
    }

    private void insertIntoParent(Deque<InnerNode> parents,
                                  Node leftChild, Node rightChild, Integer separatorKey) {
        if (parents == null || parents.isEmpty()) {
            InnerNode newRoot = new InnerNode(this.root.getCapacity());
            newRoot.getKeys()[0] = separatorKey;
            newRoot.getChildren()[0] = leftChild;
            newRoot.getChildren()[1] = rightChild;
            newRoot.setCurrentSize(1);
            newRoot.setChildrenCurrentSize(2);
            this.root = newRoot;
            System.out.println("Created new root InnerNode with keys: " + Arrays.toString(newRoot.getKeys()));
            return;
        }

        InnerNode parent = parents.pop();

        if (parent.getCurrentSize() == parent.getCapacity()) {
            System.out.println("Parent is full, splitting before insert.");
            splitInnerNode(parent, parents);
        }

        int insertPos = 0;
        while (insertPos < parent.getCurrentSize() && parent.getKeys()[insertPos] < separatorKey) {
            insertPos++;
        }

        for (int i = parent.getCurrentSize(); i > insertPos; i--) {
            parent.getKeys()[i] = parent.getKeys()[i - 1];
        }
        parent.getKeys()[insertPos] = separatorKey;
        parent.setCurrentSize(parent.getCurrentSize() + 1);

        for (int i = parent.getChildrenCurrentSize(); i > insertPos + 1; i--) {
            parent.getChildren()[i] = parent.getChildren()[i - 1];
        }
        parent.getChildren()[insertPos + 1] = rightChild;
        parent.setChildrenCurrentSize(parent.getChildrenCurrentSize() + 1);

        System.out.println("Inserted separator key " + separatorKey + " at position " + insertPos + " in parent.");
        System.out.println("Parent keys after insertion: " + Arrays.toString(parent.getKeys()));
        System.out.println("Parent children after insertion: " + Arrays.toString(parent.getChildren()));

        if (parent.getCurrentSize() > parent.getCapacity()) {
            System.out.println("Parent overflow detected. Splitting InnerNode.");
            splitInnerNode(parent, parents);
        }
    }

    private void splitInnerNode(InnerNode node, Deque<InnerNode> parents) {
        InnerNode newInner = new InnerNode(node.getCapacity());

        int mid = node.getCurrentSize() / 2;

        Integer separatorKey = node.getKeys()[mid];
        System.out.println("Splitting InnerNode. Separator key: " + separatorKey);

        int numKeysToMove = node.getCurrentSize() - (mid + 1);
        for (int i = 0; i < numKeysToMove; i++) {
            newInner.getKeys()[i] = node.getKeys()[mid + 1 + i];
            node.getKeys()[mid + 1 + i] = null;
        }
        newInner.setCurrentSize(numKeysToMove);
        node.setCurrentSize(mid);

        int numChildrenToMove = numKeysToMove + 1;
        for (int i = 0; i < numChildrenToMove; i++) {
            newInner.getChildren()[i] = node.getChildren()[mid + 1 + i];
            node.getChildren()[mid + 1 + i] = null;
        }
        newInner.setChildrenCurrentSize(numChildrenToMove);
        node.setChildrenCurrentSize(mid + 1);

        System.out.println("After splitting InnerNode:");
        System.out.println("Original InnerNode keys: " + Arrays.toString(node.getKeys()));
        System.out.println("Original InnerNode children: " + Arrays.toString(node.getChildren()));
        System.out.println("Original InnerNode currentSize: " + node.getCurrentSize());

        System.out.println("New InnerNode keys: " + Arrays.toString(newInner.getKeys()));
        System.out.println("New InnerNode children: " + Arrays.toString(newInner.getChildren()));
        System.out.println("New InnerNode currentSize: " + newInner.getCurrentSize());

        insertIntoParent(parents, node, newInner, separatorKey);
    }

    private String deleteFromLeafNode(Integer key, LeafNode node, Deque<InnerNode> parents) {
        int index = -1;
        for (int i = 0; i < node.getCurrentSize(); i++) {
            if (key.equals(node.getKeys()[i])) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return null;
        }

        String oldValue = node.getValues()[index];

        for (int i = index; i < node.getCurrentSize() - 1; i++) {
            node.getKeys()[i] = node.getKeys()[i + 1];
            node.getValues()[i] = node.getValues()[i + 1];
        }
        int last = node.getCurrentSize() - 1;
        node.getKeys()[last] = null;
        node.getValues()[last] = null;
        node.setCurrentSize(node.getCurrentSize() - 1);

        int minKeys = this.capacity / 2;
        if (node == this.root) {
            return oldValue;
        }

        if (node.getCurrentSize() < minKeys) {
            handleLeafUnderflow(node, parents);
        }

        return oldValue;
    }

    private void handleLeafUnderflow(LeafNode node, Deque<InnerNode> parents) {
        InnerNode parent = parents.isEmpty() ? null : parents.peek();
        if (parent == null) {
            return;
        }

        int childIndex = findChildIndex(parent, node);

        LeafNode leftSibling  = (childIndex > 0) ? (LeafNode) parent.getChildren()[childIndex - 1] : null;
        LeafNode rightSibling = (childIndex < parent.getChildrenCurrentSize() - 1)
                ? (LeafNode) parent.getChildren()[childIndex + 1] : null;

        if (leftSibling != null && leftSibling.getCurrentSize() > capacity / 2) {
            borrowFromLeftLeaf(node, leftSibling, parent, childIndex);
            return;
        }
        if (rightSibling != null && rightSibling.getCurrentSize() > capacity / 2) {
            borrowFromRightLeaf(node, rightSibling, parent, childIndex);
            return;
        }

        if (leftSibling != null) {
            mergeLeaves(leftSibling, node, parent, childIndex - 1);
        } else if (rightSibling != null) {
            mergeLeaves(node, rightSibling, parent, childIndex);
        }
    }

    private void borrowFromLeftLeaf(LeafNode underflowNode, LeafNode leftSibling,
                                    InnerNode parent, int parentIndex) {
        int borrowedIndex = leftSibling.getCurrentSize() - 1;
        Integer borrowedKey = leftSibling.getKeys()[borrowedIndex];
        String borrowedVal = leftSibling.getValues()[borrowedIndex];

        leftSibling.getKeys()[borrowedIndex] = null;
        leftSibling.getValues()[borrowedIndex] = null;
        leftSibling.setCurrentSize(leftSibling.getCurrentSize() - 1);

        for (int i = underflowNode.getCurrentSize(); i > 0; i--) {
            underflowNode.getKeys()[i] = underflowNode.getKeys()[i - 1];
            underflowNode.getValues()[i] = underflowNode.getValues()[i - 1];
        }

        underflowNode.getKeys()[0] = borrowedKey;
        underflowNode.getValues()[0] = borrowedVal;
        underflowNode.setCurrentSize(underflowNode.getCurrentSize() + 1);

        parent.getKeys()[parentIndex - 1] = borrowedKey;
    }

    private void borrowFromRightLeaf(LeafNode underflowNode, LeafNode rightSibling,
                                     InnerNode parent, int parentIndex) {
        Integer borrowedKey = rightSibling.getKeys()[0];
        String borrowedValue = rightSibling.getValues()[0];

        int insertPos = underflowNode.getCurrentSize();
        underflowNode.getKeys()[insertPos] = borrowedKey;
        underflowNode.getValues()[insertPos] = borrowedValue;
        underflowNode.setCurrentSize(underflowNode.getCurrentSize() + 1);

        for (int i = 0; i < rightSibling.getCurrentSize() - 1; i++) {
            rightSibling.getKeys()[i] = rightSibling.getKeys()[i + 1];
            rightSibling.getValues()[i] = rightSibling.getValues()[i + 1];
        }
        int last = rightSibling.getCurrentSize() - 1;
        rightSibling.getKeys()[last] = null;
        rightSibling.getValues()[last] = null;
        rightSibling.setCurrentSize(rightSibling.getCurrentSize() - 1);

        if (rightSibling.getCurrentSize() > 0) {
            parent.getKeys()[parentIndex] = rightSibling.getKeys()[0];
        } else {
            parent.getKeys()[parentIndex] = borrowedKey;
        }
    }

    private void mergeLeaves(LeafNode leftLeaf, LeafNode rightLeaf,
                             InnerNode parent, int parentIndex) {
        int start = leftLeaf.getCurrentSize();
        for (int i = 0; i < rightLeaf.getCurrentSize(); i++) {
            leftLeaf.getKeys()[start + i] = rightLeaf.getKeys()[i];
            leftLeaf.getValues()[start + i] = rightLeaf.getValues()[i];
        }
        leftLeaf.setCurrentSize(leftLeaf.getCurrentSize() + rightLeaf.getCurrentSize());

        leftLeaf.setNextLeaf(rightLeaf.getNextLeaf());

        for (int i = parentIndex + 1; i < parent.getChildrenCurrentSize() - 1; i++) {
            parent.getChildren()[i] = parent.getChildren()[i + 1];
        }
        parent.getChildren()[parent.getChildrenCurrentSize() - 1] = null;
        parent.setChildrenCurrentSize(parent.getChildrenCurrentSize() - 1);

        for (int i = parentIndex; i < parent.getCurrentSize() - 1; i++) {
            parent.getKeys()[i] = parent.getKeys()[i + 1];
        }
        parent.getKeys()[parent.getCurrentSize() - 1] = null;
        parent.setCurrentSize(parent.getCurrentSize() - 1);

        if (parent.getCurrentSize() < capacity / 2 && parent != root) {
            handleInnerUnderflow(parent, (Deque<InnerNode>) parent);
        }
    }
    private void handleInnerUnderflow(InnerNode node, Deque<InnerNode> parents) {
        if (node == root) {
            if (node.getCurrentSize() == 0 && node.getChildrenCurrentSize() == 1) {
                Node onlyChild = node.getChildren()[0];
                this.root = onlyChild;
            }
            return;
        }

        InnerNode parent = (parents.isEmpty()) ? null : parents.peek();
        if (parent == null) {
            return;
        }

        int indexInParent = findChildIndex(parent, node);

        InnerNode leftSibling  = (indexInParent > 0)
                ? (InnerNode) parent.getChildren()[indexInParent - 1]
                : null;
        InnerNode rightSibling = (indexInParent < parent.getChildrenCurrentSize() - 1)
                ? (InnerNode) parent.getChildren()[indexInParent + 1]
                : null;

        int minKeys = capacity / 2;
        if (leftSibling != null && leftSibling.getCurrentSize() > minKeys) {
            borrowFromLeftInner(node, leftSibling, parent, indexInParent);
            return;
        }

        if (rightSibling != null && rightSibling.getCurrentSize() > minKeys) {
            borrowFromRightInner(node, rightSibling, parent, indexInParent);
            return;
        }

        if (leftSibling != null) {
            mergeInnerNodes(leftSibling, node, parent, indexInParent - 1, parents);
        } else if (rightSibling != null) {
            mergeInnerNodes(node, rightSibling, parent, indexInParent, parents);
        }
    }

    private void borrowFromLeftInner(InnerNode underflowNode, InnerNode leftSibling,
                                     InnerNode parent, int indexInParent) {
        for (int i = underflowNode.getCurrentSize(); i > 0; i--) {
            underflowNode.getKeys()[i] = underflowNode.getKeys()[i - 1];
        }
        for (int i = underflowNode.getChildrenCurrentSize(); i > 0; i--) {
            underflowNode.getChildren()[i] = underflowNode.getChildren()[i - 1];
        }

        Integer parentKey = parent.getKeys()[indexInParent - 1];
        underflowNode.getKeys()[0] = parentKey;
        underflowNode.setCurrentSize(underflowNode.getCurrentSize() + 1);

        underflowNode.getChildren()[0] = leftSibling.getChildren()[leftSibling.getChildrenCurrentSize() - 1];
        underflowNode.setChildrenCurrentSize(underflowNode.getChildrenCurrentSize() + 1);
        leftSibling.getChildren()[leftSibling.getChildrenCurrentSize() - 1] = null;
        leftSibling.setChildrenCurrentSize(leftSibling.getChildrenCurrentSize() - 1);

        Integer borrowedKey = leftSibling.getKeys()[leftSibling.getCurrentSize() - 1];
        parent.getKeys()[indexInParent - 1] = borrowedKey;

        leftSibling.getKeys()[leftSibling.getCurrentSize() - 1] = null;
        leftSibling.setCurrentSize(leftSibling.getCurrentSize() - 1);
    }

    private void borrowFromRightInner(InnerNode underflowNode, InnerNode rightSibling,
                                      InnerNode parent, int indexInParent) {
        int insertIndex = underflowNode.getCurrentSize();
        Integer parentKey = parent.getKeys()[indexInParent];

        underflowNode.getKeys()[insertIndex] = parentKey;
        underflowNode.setCurrentSize(underflowNode.getCurrentSize() + 1);

        underflowNode.getChildren()[underflowNode.getChildrenCurrentSize()] = rightSibling.getChildren()[0];
        underflowNode.setChildrenCurrentSize(underflowNode.getChildrenCurrentSize() + 1);

        Integer borrowedKey = rightSibling.getKeys()[0];
        parent.getKeys()[indexInParent] = borrowedKey;

        for (int i = 0; i < rightSibling.getCurrentSize() - 1; i++) {
            rightSibling.getKeys()[i] = rightSibling.getKeys()[i + 1];
        }
        rightSibling.getKeys()[rightSibling.getCurrentSize() - 1] = null;
        rightSibling.setCurrentSize(rightSibling.getCurrentSize() - 1);

        for (int i = 0; i < rightSibling.getChildrenCurrentSize() - 1; i++) {
            rightSibling.getChildren()[i] = rightSibling.getChildren()[i + 1];
        }
        rightSibling.getChildren()[rightSibling.getChildrenCurrentSize() - 1] = null;
        rightSibling.setChildrenCurrentSize(rightSibling.getChildrenCurrentSize() - 1);
    }


    private void mergeInnerNodes(InnerNode leftNode, InnerNode rightNode,
                                 InnerNode parent, int parentIndex, Deque<InnerNode> parents) {
        int leftSize = leftNode.getCurrentSize();

        Integer separatorKey = parent.getKeys()[parentIndex];
        leftNode.getKeys()[leftSize] = separatorKey;
        leftNode.setCurrentSize(leftSize + 1);

        for (int i = 0; i < rightNode.getCurrentSize(); i++) {
            leftNode.getKeys()[leftNode.getCurrentSize() + i] = rightNode.getKeys()[i];
        }
        leftNode.setCurrentSize(leftNode.getCurrentSize() + rightNode.getCurrentSize());

        int leftChildrenSize = leftNode.getChildrenCurrentSize();
        for (int i = 0; i < rightNode.getChildrenCurrentSize(); i++) {
            leftNode.getChildren()[leftChildrenSize + i] = rightNode.getChildren()[i];
        }
        leftNode.setChildrenCurrentSize(leftChildrenSize + rightNode.getChildrenCurrentSize());

        for (int i = parentIndex; i < parent.getCurrentSize() - 1; i++) {
            parent.getKeys()[i] = parent.getKeys()[i + 1];
        }
        parent.getKeys()[parent.getCurrentSize() - 1] = null;
        parent.setCurrentSize(parent.getCurrentSize() - 1);

        for (int i = parentIndex + 1; i < parent.getChildrenCurrentSize() - 1; i++) {
            parent.getChildren()[i] = parent.getChildren()[i + 1];
        }
        parent.getChildren()[parent.getChildrenCurrentSize() - 1] = null;
        parent.setChildrenCurrentSize(parent.getChildrenCurrentSize() - 1);

        if (parent != root && parent.getCurrentSize() < capacity / 2) {
            handleInnerUnderflow(parent, parents);
        }
    }

    private int findChildIndex(InnerNode parent, Node child) {
        for (int i = 0; i < parent.getChildrenCurrentSize(); i++) {
            if (parent.getChildren()[i] == child) {
                return i;
            }
        }
        return -1;
    }

    public String lookup(Integer key) {
        LeafNode leafNode = findLeafNode(key, root);
        return lookupInLeafNode(key, leafNode);
    }

    public void insert(int key, String value) {
        Deque<InnerNode> parents = new LinkedList<>();
        LeafNode leafNode = findLeafNode(key, root, parents);
        insertIntoLeafNode(key, value, leafNode, parents);
    }

    public String delete(Integer key) {
        Deque<InnerNode> parents = new LinkedList<>();
        LeafNode leafNode = findLeafNode(key, root, parents);
        return deleteFromLeafNode(key, leafNode, parents);
    }

    private int capacity = 0;

    private Node root;

    public BPlusTree(int capacity) {
        this(new LeafNode(capacity), capacity);
    }

    public BPlusTree(Node root, int capacity) {
        assert capacity % 2 == 0;
        this.capacity = capacity;
        this.root = root;
    }

    public Node rootNode() {
        return root;
    }

    public String toString() {
        return new BPlusTreePrinter(this).toString();
    }

    private LeafNode findLeafNode(Integer key, Node node) {
        return findLeafNode(key, node, null);
    }

}
