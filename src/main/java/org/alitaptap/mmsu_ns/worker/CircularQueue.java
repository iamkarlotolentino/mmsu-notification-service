package org.alitaptap.mmsu_ns.worker;

import java.util.ArrayList;

public class CircularQueue<T> {

  private int currentIndex = 0;
  private ArrayList<T> list = new ArrayList<>();

  public CircularQueue() {}

  public T next() {
    if (currentIndex == list.size()) currentIndex = 0;
    return list.get(currentIndex++);
  }

  public void enqueue(T item) {
    list.add(item);
  }

  public void dequeue() {
    list.remove(list.size() - 1);
  }

  public int size() {
    return list.size();
  }
}
