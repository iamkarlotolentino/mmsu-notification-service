package org.alitaptap.mmsu_ns.worker;

import org.alitaptap.mmsu_ns.model.StudentAccount;
import org.alitaptap.mmsu_ns.model.UserPayload;
import org.alitaptap.mmsu_ns.service.NotificationService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Map;

import static org.alitaptap.mmsu_ns.service.NotificationService.ResponseType.*;

public class WebAccessorTask {

  private static final Logger LOG = LoggerFactory.getLogger(WebAccessorTask.class);

  private static final String USER_AGENT =
      "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
  private final UserPayload userPayload;
  @Autowired private NotificationService notify;
  private Map<String, String> authenticatedCookies;
  private JSONArray oldGradeContent;
  private String latestFeedback = "";

  public WebAccessorTask(UserPayload payload) {
    this.userPayload = payload;
  }

  public boolean login() {
    LOG.info("Service Worker: Login Request <" + userPayload.getStudentAccount() + ">");
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
      // 34058 - length of dashboard page, meaning we have authenticated access.
      if (authenticateForm.parse().html().length() == 34058) {
        LOG.info(
            "Service Worker: Login success <" + userPayload.getStudentAccount().getUserId() + ">");
        // Get the authenticated cookies
        if (authenticatedCookies == null) authenticatedCookies = authenticateForm.cookies();
        return true;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    LOG.info("Service Worker: Login failed <" + userPayload.getStudentAccount().getUserId() + ">");
    return false;
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
      // We only grab the grades and don't care about the metadata
      if (oldGradeContent == null)
        oldGradeContent = new JSONObject(viewGradeForm.parse().body().text()).getJSONArray("data");
      else {
        // After getting the initial content to compare,
        // at this point we have a new content to compare for each refresh
        JSONArray newGradeContent =
            new JSONObject(viewGradeForm.parse().body().text()).getJSONArray("data");

        // Compare new received content for every refresh
        // Check if we have an update for the user
        if (!oldGradeContent.similar(newGradeContent)) {

          // Check if what has been updated in the grade.
          // Identify if the grade has been POSTED, UPLOADED, or FINALIZED.
          // UPLOADED  - When the teacher has submitted the grade,
          //             but not yet visible to students.
          // POSTED    - When it is approved by the chairman,
          //             and now visible to students.
          // FINALIZED - When the registrar has verified
          //             or validated the submitted grade.

          // Get the grades data
          // For every grades, - 2 to remove unnecessary data
          for (int i = 0; i < newGradeContent.length() - 2; i++) {
            JSONObject newData = (JSONObject) newGradeContent.get(i);
            JSONObject oldData = (JSONObject) oldGradeContent.get(i);

            // Check if subject has an update.
            if (!newData.similar(oldData)) {
              // Check if what kind of update was it.

              // CASE 1: The grade has been uploaded.
              if (oldData.get("fgrade").equals("") && newData.get("fgrade").equals("----")) {
                notify.push(userPayload.getChatUserId(), GRADE_UPLOADED);
              }

              // CASE 2: The grade has been posted.
              if (oldData.get("fgrade").equals("----") && !newData.get("fgrade").equals("----")) {
                notify.push(userPayload.getChatUserId(), GRADE_POSTED);
              }

              // CASE 3: The grade has been finalized.
              if (oldData.get("color").equals("green") && newData.get("color").equals("black")) {
                notify.push(userPayload.getChatUserId(), GRADE_FINALIZED);
              }

              latestFeedback =
                  String.format("UPDATE: (%s - %s)", newData.get("code"), newData.get("title"));
              notify.push(userPayload.getChatUserId(), GRADE_FEEDBACK);
            }
          }
          // Replace with the new updated content
          oldGradeContent = newGradeContent;
        }
      }
    } catch (IOException e) {
      notify.push(userPayload.getChatUserId(), SERVER_ERROR);
      e.printStackTrace();
    }
  }

  public String getLatestFeedback() {
    return latestFeedback;
  }

  public void call() {
    LOG.info("Service Worker: View grades <" + userPayload.getStudentAccount().getUserId() + ">");
    // Without cookies, we cannot access the grades.
    if (authenticatedCookies == null) login();
    else viewGrades();
  }

  public String getStudentId() {
    return userPayload.getStudentAccount().getUserId();
  }
}
