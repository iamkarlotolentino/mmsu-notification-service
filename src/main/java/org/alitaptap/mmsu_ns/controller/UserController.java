package org.alitaptap.mmsu_ns.controller;

import org.alitaptap.mmsu_ns.model.UserPayload;
import org.alitaptap.mmsu_ns.service.NotificationService;
import org.alitaptap.mmsu_ns.worker.ActiveServiceDirectory;
import org.alitaptap.mmsu_ns.worker.ActiveUsersDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static org.alitaptap.mmsu_ns.service.NotificationService.ResponseType.*;

@RestController
public class UserController {

  private final ActiveUsersDirectory mUsers = ActiveUsersDirectory.getInstance();
  private Logger LOG = LoggerFactory.getLogger(UserController.class);
  @Autowired private NotificationService notify;

  @PostMapping("/service_task_user/create")
  public String newUserRequest(@RequestBody UserPayload userPayload) {
    LOG.info("Received user registration request from " + userPayload.getChatUserId());
    ActiveUsersDirectory.Response response = mUsers.registerUser(userPayload);

    switch (response) {
      case USER_EXISTING:
        LOG.info("Res: USER_EXISTING, " + userPayload.getChatUserId() + " is notified.");
        notify.push(userPayload.getChatUserId(), SERVICE_USER_EXISTING);
        return "User is already existing";
      case USER_CREATED:
        LOG.info("Res: USER_CREATED, " + userPayload.getChatUserId() + "is notified.");
        notify.push(userPayload.getChatUserId(), SERVICE_USER_CREATED);
        notify.push(userPayload.getChatUserId(), SERVICE_LOGIN_SUCCESS);
        return "User has been registered!";
      case USER_INVALID:
        LOG.info("Res: USER_INVALID, " + userPayload.getChatUserId() + "is notified.");
        notify.push(userPayload.getChatUserId(), SERVICE_LOGIN_FAILED);
        return "Given account details is incorrect";
      default:
        LOG.error("Response from GET[/new_user]received unknown response.");
        notify.push(userPayload.getChatUserId(), SERVER_ERROR);
        return "The system encountered error.";
    }
  }

  @PostMapping("/service_task_user/delete")
  public String unregisterUser(@RequestBody UserPayload userPayload) {
    LOG.info("Received request to unregister user from " + userPayload.getChatUserId());
    ActiveUsersDirectory.Response response = mUsers.unregisterUser(userPayload);

    switch (response) {
      case USER_MISSING:
        LOG.info("Res: USER_MISSING, removal request from " + userPayload.getChatUserId());
        notify.push(userPayload.getChatUserId(), SERVICE_USER_MISSING);
        return "The user is not registered in the directory";
      case USER_REMOVED:
        LOG.info("Res: USER_REMOVED, removal request from " + userPayload.getChatUserId());
        notify.push(userPayload.getChatUserId(), SERVICE_USER_DELETION);
        return "The user has been unregistered.";
      default:
        LOG.error("Response from POST[/unregister_user]received unknown response.");
        notify.push(userPayload.getChatUserId(), SERVER_ERROR);
        return "The system has encountered an error";
    }
  }

  @GetMapping("/grade_feedback")
  public String feedback(@RequestParam String userId) {
    ActiveUsersDirectory activeUsersDirectory = ActiveUsersDirectory.getInstance();
    ActiveServiceDirectory activeServiceDirectory = ActiveServiceDirectory.getInstance();

    // Validate that student exists in the server
    boolean exists = activeUsersDirectory.isRegistered(userId);

    if (exists) {
      return activeServiceDirectory
          .getServiceTask(userId)
          .getLatestFeedback();
    } else {
      return "You do not have access to this API.";
    }
  }
}
