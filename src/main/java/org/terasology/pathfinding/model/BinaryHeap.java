// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.pathfinding.model;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author schalentier
 */
public class BinaryHeap {
    private static final int DEFAULT_CAPACITY = 1000;
    private static final int DEFAULT_INDEX_SIZE = 1024;
    private int[] heap;
    private int[] itemToIndex;
    private int size;
    private Comparator<Integer> comparator;

    public BinaryHeap(Comparator<Integer> comparator) {
        this(comparator, DEFAULT_CAPACITY, DEFAULT_INDEX_SIZE);
    }

    public BinaryHeap(Comparator<Integer> comparator, int capacity, int indexSize) {
        this.comparator = comparator;
        size = 0;
        heap = new int[capacity];
        itemToIndex = new int[indexSize];
        Arrays.fill(itemToIndex, -1);
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return getSize() == 0;
    }

    public boolean contains(int item) {
        return itemToIndex[item] > -1;
    }

    public void update(int item) {
        resortUp(itemToIndex[item]);
    }

    public void insert(int item) {
        if (heap.length <= size) {
            enlarge(heap.length * 2);
        }

        int last = size;
        size++;
        heap[last] = item;
        itemToIndex[item] = last;
        resortUp(last);
    }

    public int removeMin() {
        int min = heap[0];
        heap[0] = heap[size - 1];
        itemToIndex[min] = -1;
        resortDown(0);
        size--;
        return min;
    }

    protected void resortDown(int index) {
        int currentIndex = index;
        int value = heap[currentIndex];
        while (currentIndex * 2 + 1 < size) {
            int child = currentIndex * 2 + 1;
            if (child < size - 1 && comparator.compare(heap[child + 1], heap[child]) > 0) {
                child++;
            }
            if (comparator.compare(heap[child], value) > 0) {
                heap[currentIndex] = heap[child];
                itemToIndex[heap[child]] = currentIndex;
            } else {
                break;
            }
            currentIndex = child;
        }
        heap[currentIndex] = value;
        itemToIndex[value] = currentIndex;
    }

    protected void resortUp(int index) {
        int currentIndex = index;
        int value = heap[currentIndex];
        while (currentIndex > 0 && comparator.compare(heap[currentIndex / 2], value) < 0) {
            heap[currentIndex] = heap[currentIndex / 2];
            itemToIndex[heap[currentIndex / 2]] = currentIndex;
            currentIndex /= 2;
        }
        heap[currentIndex] = value;
        itemToIndex[value] = currentIndex;
    }

    protected void enlarge(int newSize) {
        int[] array = new int[newSize];
        System.arraycopy(heap, 0, array, 0, size);
        heap = array;

    }

    public void clear() {
        size = 0;
        Arrays.fill(itemToIndex, -1);
    }
}
