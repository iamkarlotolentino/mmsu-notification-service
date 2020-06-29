package org.alitaptap.mmsu_ns.worker;

import org.alitaptap.mmsu_ns.model.StudentAccount;
import org.alitaptap.mmsu_ns.model.UserPayload;

import java.util.HashMap;

/**
 * The purpose of this class is to easily identify duplicate student accounts being created in the
 * service. This allows us to form a pre-screening process before creating a task dedicated for the
 * user.
 */
public class ActiveUsersDirectory {

  private static final ActiveUsersDirectory usersDirectory = new ActiveUsersDirectory();
  private final ActiveServiceDirectory tasksDirectory = ActiveServiceDirectory.getInstance();
  private final HashMap<String, StudentAccount> userDirectoryMap = new HashMap<>();

  private ActiveUsersDirectory() {}

  public static ActiveUsersDirectory getInstance() {
    return usersDirectory;
  }

  public Response registerUser(UserPayload userPayload) {
    StudentAccount studentAccount = userPayload.getStudentAccount();
    if (userDirectoryMap.containsKey(studentAccount.getUserId())) return Response.USER_EXISTING;

    // Check the authenticity of the account.
    WebAccessorTask task = new WebAccessorTask(userPayload);
    boolean isSuccessful = task.login();
    if (isSuccessful) {
      userDirectoryMap.put(
          studentAccount.getUserId(),
          new StudentAccount(studentAccount.getUserId(), studentAccount.getPassword()));
      tasksDirectory.register(task);
      return Response.USER_CREATED;
    }
    return Response.USER_INVALID;
  }

  public Response unregisterUser(UserPayload userPayload) {
    StudentAccount studentAccount = userPayload.getStudentAccount();
    if (!userDirectoryMap.containsKey(studentAccount.getUserId())) return Response.USER_MISSING;

    // Remove the task in the service task directory
    tasksDirectory.unregister(studentAccount.getUserId());
    userDirectoryMap.remove(studentAccount.getUserId());
    return Response.USER_REMOVED;
  }

  public boolean isRegistered(String studentId) {
    return userDirectoryMap.containsKey(studentId);
  }

  public enum Response {
    USER_EXISTING,
    USER_MISSING,
    USER_CREATED,
    USER_REMOVED,
    USER_INVALID
  }
}
