package org.alitaptap.mmsu_ns.worker;

import org.alitaptap.mmsu_ns.model.StudentAccount;
import org.alitaptap.mmsu_ns.model.payload.UserPayload;
import org.alitaptap.mmsu_ns.service.NotificationService;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

public class WebHandlersTask {

  private static final String USER_AGENT =
      "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";

  @Autowired private NotificationService notify;
  private UserPayload userPayload;
  private Map<String, String> cookies;
  private String contentGrade = "";

  public WebHandlersTask(UserPayload payload) {
    this.userPayload = payload;
  }

  public void login() {
    try {
      StudentAccount studentAccount = userPayload.getStudentAccount();
      // Get the login page
      Connection.Response loginForm =
          Jsoup.connect("https://mys.mmsu.edu.ph/")
              .method(Connection.Method.GET)
              .userAgent(USER_AGENT)
              .execute();

      // Obtain the auth_token
      String token =
          loginForm
              .parse()
              .select("#sign-in > div > div > form > input[type=hidden]")
              .first()
              .attr("value");

      // Post user details to login page
      Connection.Response authenticateForm =
          Jsoup.connect("https://mys.mmsu.edu.ph/login/")
              .data("student_number", studentAccount.getUserId())
              .data("password", studentAccount.getPassword())
              .data("_token", token)
              .data("remember_me", "_")
              .cookies(loginForm.cookies())
              .userAgent(USER_AGENT)
              .method(Connection.Method.POST)
              .execute();

      // Get the authenticated cookies
      if (cookies == null) cookies = authenticateForm.cookies();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void viewGrades() {
    try {
      String portalUrl =
          "https://mys.mmsu.edu.ph/v1/view-grade?pref=" + userPayload.getGradePreference();
      Connection.Response viewGradeForm =
          Jsoup.connect(portalUrl)
              .method(Connection.Method.GET)
              .userAgent(USER_AGENT)
              .cookies(cookies)
              .ignoreContentType(true)
              .execute();

      // Receive the initial content during execution
      if (contentGrade.isEmpty()) contentGrade = viewGradeForm.parse().body().text();
      else {
        // Compare new received content for every refresh
        if (contentGrade.equals(viewGradeForm.parse().body().text())) {
          // Parse the the JSON


          // Notify the user
          notify.gradeUpdated(userPayload.getChatUserId());

          // Set to the updated content
          contentGrade = viewGradeForm.parse().body().text();
        }
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void call() {
    if (cookies == null) login();
    viewGrades();
  }
}
