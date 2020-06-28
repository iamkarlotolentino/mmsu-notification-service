package org.alitaptap.mmsu_ns.controller;

import org.alitaptap.mmsu_ns.model.StudentAccount;
import org.alitaptap.mmsu_ns.model.payload.UserPayload;
import org.alitaptap.mmsu_ns.service.NotificationService;
import org.alitaptap.mmsu_ns.worker.ActiveUsersDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.alitaptap.mmsu_ns.service.NotificationService.ResponseType.*;

@RestController
public class UserController {

  @Autowired private NotificationService notify;

  private ActiveUsersDirectory mUserDirectory = ActiveUsersDirectory.getInstance();

  @PostMapping("/new_user")
  public String newUserRequest(@RequestBody UserPayload userPayload) {
    StudentAccount studentAccount = userPayload.getStudentAccount();
    ActiveUsersDirectory.Response response = mUserDirectory.registerUser(studentAccount);

    switch (response) {
      case USER_EXISTING:
        notify.push(userPayload.getChatUserId(), SERVICE_USER_EXISTING);
        return "User is already existing";
      case USER_CREATED:
        notify.push(userPayload.getChatUserId(), SERVICE_USER_CREATION);
        return "User has been registered!";
      default:
        notify.push(userPayload.getChatUserId(), SERVER_ERROR);
        return "The system encountered error.";
    }
  }

  @PostMapping("/unregister_user")
  public String unregisterUser(@RequestBody UserPayload userPayload) {
    ActiveUsersDirectory.Response response =
        mUserDirectory.unregisterUser(userPayload.getStudentAccount());

    switch (response) {
      case USER_MISSING:
        notify.push(userPayload.getChatUserId(), SERVICE_USER_MISSING);
        return "The user is not registered in the directory";
      case USER_REMOVED:
        notify.push(userPayload.getChatUserId(), SERVICE_USER_DELETION);
        return "The user has been unregistered.";
      default:
        notify.push(userPayload.getChatUserId(), SERVER_ERROR);
        return "The system has encountered an error";
    }
  }
}
