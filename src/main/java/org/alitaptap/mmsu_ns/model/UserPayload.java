package org.alitaptap.mmsu_ns.model;

import org.alitaptap.mmsu_ns.model.StudentAccount;

public class UserPayload {

  private StudentAccount studentAccount;
  private int gradePreference;
  private String chatUserId;

  public UserPayload() {}

  public UserPayload(StudentAccount studentAccount, int gradePreference, String chatUserId) {
    this.studentAccount = studentAccount;
    this.gradePreference = gradePreference;
    this.chatUserId = chatUserId;
  }

  public StudentAccount getStudentAccount() {
    return studentAccount;
  }

  public void setStudentAccount(StudentAccount studentAccount) {
    this.studentAccount = studentAccount;
  }

  public int getGradePreference() {
    return gradePreference;
  }

  public void setGradePreference(int gradePreference) {
    this.gradePreference = gradePreference;
  }

  public String getChatUserId() {
    return chatUserId;
  }

  public void setChatUserId(String chatUserId) {
    this.chatUserId = chatUserId;
  }

  @Override
  public String toString() {
    return "NewUserPayload{"
        + "studentAccount="
        + studentAccount
        + ", gradePreference="
        + gradePreference
        + ", userId='"
        + chatUserId
        + '\''
        + '}';
  }
}
