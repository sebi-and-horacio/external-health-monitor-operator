package org.lostinbrittany.healthmonitor;

public class ApiSpec {
  private String serviceUrl;
  private int pollingInterval; // in seconds

  // Getters and Setters
  public String getServiceUrl() {
      return serviceUrl;
  }

  public void setServiceUrl(String serviceUrl) {
      this.serviceUrl = serviceUrl;
  }

  public int getPollingInterval() {
      return pollingInterval;
  }

  public void setPollingInterval(int pollingInterval) {
      this.pollingInterval = pollingInterval;
  }
}