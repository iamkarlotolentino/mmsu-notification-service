package org.alitaptap.mmsu_ns.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootController {

  @GetMapping("/")
  public String getHome() {
    return "This is a notification service API.";
  }
}
