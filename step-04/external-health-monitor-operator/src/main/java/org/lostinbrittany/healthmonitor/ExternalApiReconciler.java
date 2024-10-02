package org.lostinbrittany.healthmonitor;

import io.quarkus.scheduler.Scheduled;

import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;

public class ExternalApiReconciler implements Reconciler<ExternalApi> {

  private static final Logger LOG = Logger.getLogger(ExternalApiReconciler.class);
  

  // Store the polling interval and external service resources
  private final Map<String, ExternalApi> resourceMap = new ConcurrentHashMap<>();

  @Override
  public UpdateControl<ExternalApi> reconcile(ExternalApi resource, Context<ExternalApi> context) {

    String resourceName = resource.getMetadata().getName();
    resourceMap.put(resourceName, resource);  // Store the resource in the map

    LOG.info("Reconciling resource: " + resourceName);

    // Perform an immediate health check
    checkServiceHealth(resource);

    return UpdateControl.noUpdate();
  }

  // Periodically poll the external services based on polling interval
  @Scheduled(every = "10s")  // You can adjust this based on the smallest polling interval needed
  public void scheduledHealthCheck() {
      for (ExternalApi resource : resourceMap.values()) {
          ApiSpec spec = resource.getSpec();
          if (spec != null && spec.getPollingInterval() > 0) {
              long now = System.currentTimeMillis() / 1000;
              long lastChecked = resource.getStatus() != null
                      ? Instant.parse(resource.getStatus().getLastChecked()).getEpochSecond()
                      : 0;
              
              if (now - lastChecked >= spec.getPollingInterval()) {
                  // Perform the health check if the interval has passed
                  checkServiceHealth(resource);
              }
          }
      }
  }

  // Method to perform the actual health check of the external service
  private void checkServiceHealth(ExternalApi resource) {

    ApiSpec spec = resource.getSpec();
    if (spec == null) {
        return; // No spec, can't do anything
    }

    String serviceUrl = spec.getServiceUrl();
    ApiStatus status = resource.getStatus() != null ? resource.getStatus() : new ApiStatus();

    try {
        long start = System.currentTimeMillis();
        URL url = new URL(serviceUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        long end = System.currentTimeMillis();

        if (responseCode == 200) {
            status.setHealthStatus("Healthy");
        } else {
            status.setHealthStatus("Unhealthy");
        }
        status.setResponseTime((int) (end - start));
        status.setLastChecked(Instant.now().toString());


    } catch (Exception e) {
        status.setHealthStatus("Error: " + e.getMessage());
        status.setLastChecked(Instant.now().toString());
    }

    resource.setStatus(status);
    UpdateControl.updateStatus(resource);
  } 
}