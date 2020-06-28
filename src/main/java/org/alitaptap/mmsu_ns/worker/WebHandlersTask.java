package org.alitaptap.mmsu_ns.worker;

import org.alitaptap.mmsu_ns.model.StudentAccount;
import org.alitaptap.mmsu_ns.model.payload.UserPayload;
import org.alitaptap.mmsu_ns.service.NotificationService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

import static org.alitaptap.mmsu_ns.service.NotificationService.ResponseType.*;

public class WebHandlersTask {

  private static final String USER_AGENT =
      "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
  private final UserPayload userPayload;
  @Autowired private NotificationService notify;
  private Map<String, String> authenticatedCookies;
  private JSONArray storedGradeJson;

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

      // Obtain the auth_token for authenticating the login form
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

      // Determine if login was successful, otherwise remind the user.
      // TODO: Replace the integer value with the correct length.
      if (authenticateForm.parse().html().length() == 357) {
        // Get the authenticated cookies
        if (authenticatedCookies == null) authenticatedCookies = authenticateForm.cookies();
        notify.push(userPayload.getChatUserId(), SERVICE_LOGIN_SUCCESS);
      } else notify.push(userPayload.getChatUserId(), SERVICE_LOGIN_FAILED);
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
              .cookies(authenticatedCookies)
              .ignoreContentType(true)
              .execute();

      // Receive the initial content during execution
      if (storedGradeJson == null)
        storedGradeJson = new JSONArray(viewGradeForm.parse().body().text());
      else {
        // After getting the initial content to compare,
        // at this point we have a new content to compare for each refresh
        JSONArray newGradeContent = new JSONArray(viewGradeForm.parse().body().text());

        // Compare new received content for every refresh
        // Check if we have an update for the user
        if (!storedGradeJson.similar(newGradeContent)) {

          // Check if what has been updated in the grade.
          // Identify if the grade has been POSTED, or UPLOADED.
          // UPLOADED - When the teacher has submitted the grade,
          //            but not yet visible to students.
          // POSTED   - When it is approved by the chairman,
          //            and now visible to students.
          NotificationService.ResponseType responseType = GRADE_UPLOADED;

          for (Object jsonObject : newGradeContent.toList()) {
            JSONObject content = (JSONObject) jsonObject;
            // TODO: Identify if grades has been posted, or uploaded.
            System.out.println(content);
          }

          // Notify the user
          notify.push(userPayload.getChatUserId(), responseType);

          // Replace with the new updated content
          storedGradeJson = newGradeContent;
        }
      }
    } catch (IOException e) {
      notify.push(userPayload.getChatUserId(), SERVER_ERROR);
      e.printStackTrace();
    }
  }

  public void call() {
    // Without cookies, we cannot access the grades.
    if (authenticatedCookies == null) login();
    else viewGrades();
  }
}
