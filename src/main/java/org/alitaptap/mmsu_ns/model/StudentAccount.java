package org.alitaptap.mmsu_ns.model;

public class StudentAccount {

  private String userId;
  private String password;

  public StudentAccount() {}

  public StudentAccount(String userId, String password) {
    this.userId = userId;
    this.password = password;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return "StudentAccount{" + "userId='" + userId + '\'' + ", password='" + password + '\'' + '}';
  }
}
