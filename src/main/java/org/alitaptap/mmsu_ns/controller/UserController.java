package org.alitaptap.mmsu_ns.controller;

import org.alitaptap.mmsu_ns.model.StudentAccount;
import org.alitaptap.mmsu_ns.model.payload.UserPayload;
import org.alitaptap.mmsu_ns.worker.ActiveUsersDirectory;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

  private ActiveUsersDirectory mUserDirectory = ActiveUsersDirectory.getInstance();

  @PostMapping("/new_user")
  public String newUserRequest(@RequestBody UserPayload userPayload) {
    StudentAccount studentAccount = userPayload.getStudentAccount();
    ActiveUsersDirectory.Response response = mUserDirectory.registerUser(studentAccount);

    switch (response) {
      case USER_EXISTING:
        return "Already existing!";
      case USER_CREATED:
        return "User has been registered!";
      default:
        return "The system encountered error.";
    }
  }

  @PostMapping("/unregister_user")
  public String unregisterUser(@RequestBody StudentAccount studentAccount) {
    ActiveUsersDirectory.Response response = mUserDirectory.unregisterUser(studentAccount);

    switch (response) {
      case USER_MISSING:
        return "The user is not registered in the directory";
      case USER_REMOVED:
        return "The user has been unregistered.";
      default:
        return "The system has encountered an error";
    }
  }
}
