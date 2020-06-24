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

  public void cannotLogin() {}

  public void gradeUpdated(String userId) {
    PUSH_TRIGGER_API_URL = String.format(TRIGGER_API_URL, userId, "cbot_grade_posted");
    pushNotification();
  }

  public void pushNotification() {
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
}
