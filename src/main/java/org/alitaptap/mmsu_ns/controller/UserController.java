package org.alitaptap.mmsu_ns.controller;

import org.alitaptap.mmsu_ns.model.payload.UserPayload;
import org.alitaptap.mmsu_ns.service.NotificationService;
import org.alitaptap.mmsu_ns.worker.ActiveServiceDirectory;
import org.alitaptap.mmsu_ns.worker.ActiveUsersDirectory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.alitaptap.mmsu_ns.service.NotificationService.ResponseType.*;

@RestController
public class UserController {

  private final ActiveUsersDirectory mUsers = ActiveUsersDirectory.getInstance();
  private NotificationService notify;

  @PostMapping("/new_user")
  public String newUserRequest(@RequestBody UserPayload userPayload) {
    ActiveUsersDirectory.Response response = mUsers.registerUser(userPayload);

    switch (response) {
      case USER_EXISTING:
        notify.push(userPayload.getChatUserId(), SERVICE_USER_EXISTING);
        return "User is already existing";
      case USER_CREATED:
        notify.push(userPayload.getChatUserId(), SERVICE_USER_CREATION);
        notify.push(userPayload.getChatUserId(), SERVICE_LOGIN_SUCCESS);
        return "User has been registered!";
      case USER_INVALID:
        notify.push(userPayload.getChatUserId(), SERVICE_LOGIN_FAILED);
        return "Given account details is incorrect";
      default:
        notify.push(userPayload.getChatUserId(), SERVER_ERROR);
        return "The system encountered error.";
    }
  }

  @PostMapping("/unregister_user")
  public String unregisterUser(@RequestBody UserPayload userPayload) {
    ActiveUsersDirectory.Response response = mUsers.unregisterUser(userPayload);

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
