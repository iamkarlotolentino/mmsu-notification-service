package org.alitaptap.mmsu_ns.worker;

import java.util.ArrayList;

/**
 * A structure of data in an never-ending iteration. Once it reaches the end of list, it goes back
 * to first item. This is why it's a circular.
 */
public class CircularServiceTask {

  private final ArrayList<WebAccessorTask> list = new ArrayList<>();
  private int currentIndex = 0;

  public CircularServiceTask() {}

  public WebAccessorTask next() {
    // If reaches end of list, we go back to start.
    if (currentIndex == list.size()) currentIndex = 0;
    return list.get(currentIndex++);
  }

  public void put(WebAccessorTask webAccessorTask) {
    list.add(webAccessorTask);
  }

  public void remove(String studentId) {
    list.removeIf(task -> task.getStudentId().equals(studentId));
  }

  public WebAccessorTask get(String studentId) {
    for (WebAccessorTask task : list) if (task.getStudentId().equals(studentId)) return task;
    return null;
  }

  public int length() {
    return list.size();
  }
}
