package org.alitaptap.mmsu_ns.worker;

import org.alitaptap.mmsu_ns.model.StudentAccount;

import java.util.HashMap;

public class ActiveUsersDirectory {

  public enum Response {
    USER_EXISTING,
    USER_MISSING,
    USER_CREATED,
    USER_REMOVED
  }

  private static ActiveUsersDirectory instance = new ActiveUsersDirectory();
  private HashMap<String, StudentAccount> userDirectoryMap = new HashMap<>();

  private ActiveUsersDirectory() {}

  public Response registerUser(StudentAccount studentAccount) {
    if (userDirectoryMap.containsKey(studentAccount.getUserId())) return Response.USER_EXISTING;

    userDirectoryMap.put(
        studentAccount.getUserId(),
        new StudentAccount(studentAccount.getUserId(), studentAccount.getPassword()));
    return Response.USER_CREATED;
  }

  public Response unregisterUser(StudentAccount studentAccount) {
    if (!userDirectoryMap.containsKey(studentAccount.getUserId())) return Response.USER_MISSING;

    userDirectoryMap.remove(studentAccount.getUserId());
    return Response.USER_REMOVED;
  }

  public StudentAccount getUser(String userId) {
    if (userDirectoryMap.containsKey(userId)) return userDirectoryMap.get(userId);
    return null;
  }

  public static ActiveUsersDirectory getInstance() {
    return instance;
  }
}
