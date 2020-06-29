package org.alitaptap.mmsu_ns.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ServiceWorker {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceWorker.class);

  // Lists of all registered services
  private static final ActiveServiceDirectory activeServiceDirectory =
      ActiveServiceDirectory.getInstance();

  @Scheduled(fixedDelay = 1000)
  public void run() {
    LOG.info("The service worker is running...");

    // While there are services in the directory.
    // Continue to run tasks
    while (activeServiceDirectory.length() > 0) {
      activeServiceDirectory.runCurrentTask();
    }

    LOG.info("The service worker has stopped running. No registered services.");
  }
}
