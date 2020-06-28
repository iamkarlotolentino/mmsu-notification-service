package org.alitaptap.mmsu_ns.service;

import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class NotificationService {

  // 0 - User ID
  // 1 - Chat block name
  private static String TRIGGER_API_URL =
      "https://api.chatfuel.com/bots/5eede8d0a6ce1372e24625f0/users/%s/send?chatfuel_token=mELtlMAHYqR0BvgEiMq8zVek3uYUK3OJMbtyrdNPTrQB9ndV0fM7lWTFZbM4MZvD&chatfuel_message_tag=ACCOUNT_UPDATE&chatfuel_block_name=%s";
  private String PUSH_TRIGGER_API_URL = "TRIGGER_API_URL";

  public void push(String chatUserId, ResponseType responseType) {
    PUSH_TRIGGER_API_URL = String.format(TRIGGER_API_URL, chatUserId, responseType.get());
    pushNotification();
  }

  private void pushNotification() {
    try {
      String USER_AGENT =
          "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
      Jsoup.connect(PUSH_TRIGGER_API_URL)
          .header("Content-Type", "application/json")
          .ignoreContentType(true)
          .userAgent(USER_AGENT)
          .post();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public enum ResponseType {
    GRADE_POSTED("grade_posted"),
    GRADE_UPLOADED("grade_uploaded"),
    SERVICE_LOGIN_SUCCESS("service_login_success"),
    SERVICE_LOGIN_FAILED("service_login_failed"),
    SERVICE_USER_CREATION("service_register_user"),
    SERVICE_USER_EXISTING("service_existing_user"),
    SERVICE_USER_MISSING("service_user_missing"),
    SERVICE_USER_DELETION("service_unregister_user"),
    SERVER_INTERRUPTED("server_interrupted"),
    SERVER_ERROR("server_error");

    private String responseBody;

    ResponseType(String responseBody) {
      this.responseBody = responseBody;
    }

    public String get() {
      return responseBody;
    }
  }
}
