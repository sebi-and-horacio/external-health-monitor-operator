# Step 3: Create the Reconciler

1. **Create the Reconciler Class**

    Now, we’ll create the logic for monitoring the external service in the reconciler. Create a class `ExternalApiReconciler`. In this class, you will implement the `Reconciler` interface provided by the Quarkus Operator SDK, which handles changes 

    **ExternalApiReconciler.java**:
    ```java
    package org.lostinbrittany.healthmonitor;

    import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
    import io.javaoperatorsdk.operator.api.reconciler.Context;
    import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

    import java.net.HttpURLConnection;
    import java.net.URL;
    import java.time.Instant;

    public class ExternalApiReconciler implements Reconciler<ExternalApi> {

      @Override
      public UpdateControl<ExternalApi> reconcile(ExternalApi resource, Context<ExternalApi> context) {

        return UpdateControl.noUpdate();
      }
    }
    ```
    The `reconcile` method is invoked when the custom resource (`ExternalApi`) is created, updated, or deleted in the Kubernetes cluster.


1. Do the actual check of the External Api, once everytime the `reconcile` method is invoked, to begin with.

    ```java
    package org.lostinbrittany.healthmonitor;

    import io.javaoperatorsdk.operator.api.reconciler.Reconciler;
    import io.javaoperatorsdk.operator.api.reconciler.Context;
    import io.javaoperatorsdk.operator.api.reconciler.UpdateControl;

    import java.net.HttpURLConnection;
    import java.net.URL;
    import java.time.Instant;
    import java.util.Map;

    public class ExternalApiReconciler implements Reconciler<ExternalApi> {

      @Override
      public UpdateControl<ExternalApi> reconcile(ExternalApi resource, Context<ExternalApi> context) {

        // Perform an immediate health check
        checkServiceHealth(resource);

        return UpdateControl.noUpdate();
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
        // Note: We're not triggering an update here immediately because this is polling based.
      } 
    }
    ```

1. Use the **Quarkus Scheduler** to schedule the polling of the External APIs so you check  their status according to the polling interval you defined.

    ```java
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

    public class ExternalApiReconciler implements Reconciler<ExternalApi> {

      @Override
      public UpdateControl<ExternalApi> reconcile(ExternalApi resource, Context<ExternalApi> context) {

        String resourceName = resource.getMetadata().getName();
        resourceMap.put(resourceName, resource);  // Store the resource in the map

        // Perform an immediate health check
        checkServiceHealth(resource);

        return UpdateControl.noUpdate();
      }

      // Periodically poll the external services based on polling interval
      @Scheduled(every = "10s")  // You can adjust this based on the smallest polling interval needed
      public void scheduledHealthCheck() {
          for (ExternalService resource : resourceMap.values()) {
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
        // Note: We're not triggering an update here immediately because this is polling based.
      } 
    }
    ```
