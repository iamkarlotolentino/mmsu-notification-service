package org.alitaptap.mmsu_ns.worker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceWorker extends Thread {

  private static final Logger LOG = LoggerFactory.getLogger(ServiceWorker.class);

  // Lists of all registered services
  private static final ActiveServiceDirectory activeServiceDirectory =
      ActiveServiceDirectory.getInstance();

  @Override
  public void run() {
    LOG.info("The service worker is running...");

    // While there are services in the directory.
    // Continue to run tasks
    while (this.isAlive()) {
      activeServiceDirectory.runCurrentTask();
    }
  }
}
