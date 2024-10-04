# External Health Monitor Operator

A sample operator built in Java on Quarkus, used for the talk _Operators to the rescue: Manage your external data and legacy APIs from Kubernetes_, by [Sebastien Blanc](https://github.com/sebastienblanc) and [Horacio Gonzalez](https://github.com/lostinbrittany).

Talk given at:

- [Devoxx Morocco](https://devoxx.ma/talk/operators-to-the-rescue-manage-your-external-data-and-legacy-apis-from-kubernetes/) on 2024/10/03.
- LunaConf, on 2024/10/04

## Instructions

### [Step 1](./step-01/): **Set up the Project Using Quarkus CLI**

1. **Install Java SDK & Quarkus**  using [SDKman](https://sdkman.io/), (if you haven't already):

   ```bash
   curl -s "https://get.sdkman.io" | bash
   sdk install java 23-open
   sdk install quarkus
   ```

2. **Create a new Quarkus project** named `external-health-monitor-operator` using the Quarkus CLI:

   ```bash
   quarkus create app external-health-monitor-operator --java 21 --no-code
   ```

   This will create a new project with the following structure:

   ```
   external-health-monitor-operator/
   ├── src/
   ├── pom.xml
   └── ...
   ```

3. **Navigate to the project directory**:
   
   ```bash
   cd external-health-monitor-operator
   ```

4. **Add necessary extensions**:
   
   You'll need the `quarkus-operator-sdk` extension for building the operator with Quarkus. Run the following command to add it:
   
   ```bash
   quarkus ext add quarkus-operator-sdk
   ```

5. **Modify the `pom.xml`**:
   Quarkus should have already added the operator SDK dependencies, but you can double-check by opening `pom.xml` and ensuring it contains the following dependency for Java Operator SDK:

   ```xml
   <dependency>
       <groupId>io.quarkiverse.operatorsdk</groupId>
       <artifactId>quarkus-operator-sdk</artifactId>
   </dependency>
   ```

6. **Launch the Quarkus Dev Mode** to write your operator as it's running:

   ```bash
   quarkus dev
   ```

   This command should start the Quarkus development mode.


### [Step 2](./step-02/): **Define the Custom Resource (CRD)**

1. In the `src/main/java` directory, create a package for your custom resource, e.g., `org.lostinbrittany.healthmonitor`.

1. Create a class for the custom resource specification. This will hold information about the external service to monitor.

    **ApiSpec.java**:
    ```java
    package org.lostinbrittany.healthmonitor;

    public class MySpec {
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
    ```

1. Define the `ApiStatus` class to track the service health state:

    **ApiStatus.java**:
    ```java
    package org.lostinbrittany.healthmonitor;

    public class ApiStatus {
       private String healthStatus;
       private String lastChecked;
       private int responseTime; // in milliseconds

       // Getters and Setters
       public String getHealthStatus() {
           return healthStatus;
       }

       public void setHealthStatus(String healthStatus) {
           this.healthStatus = healthStatus;
       }

       public String getLastChecked() {
           return lastChecked;
       }

       public void setLastChecked(String lastChecked) {
           this.lastChecked = lastChecked;
       }

       public int getResponseTime() {
           return responseTime;
       }

       public void setResponseTime(int responseTime) {
           this.responseTime = responseTime;
       }
    }
    ```

1. Now create the custom resource class itself:

    **ExternalApi.java**:
    ```java
    package org.lostinbrittany.healthmonitor;

    import io.fabric8.kubernetes.client.CustomResource;
    import io.fabric8.kubernetes.api.model.Namespaced;
    import io.fabric8.kubernetes.model.annotation.Group;
    import io.fabric8.kubernetes.model.annotation.ShortNames;
    import io.fabric8.kubernetes.model.annotation.Version;

    @Group("healthmonitor.lostinbrittany.org")
    @Version("v1alpha1")
    @ShortNames("extapi")
    public class ExternalApi extends CustomResource<ApiSpec, ApiStatus> implements Namespaced  {    
    }
    ```

### [Step 3](./step-03/): Create the Reconciler

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
1. Add the **Quarkus Scheduler** extension:

    ```bash
    quarkus ext add quarkus-scheduler
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
    ```

1. Run it in Quarkus dev mode:

    ```bash
    quarkus dev
    ```


### [Step 4](./step-04/): Deploying the CR

Once the reconciler is in place, you'll need to deploy the corresponding Custom Resource Definition (CRD) and the actual custom resources to Kubernetes.

To generate the CRD for your ExternalService resource, use the following command:

```bash
./mvnw install compile
```

This should generate the CRD YAML file in `target/kubernetes/`. You can then apply it to your Kubernetes cluster:

```bash
kubectl apply -f target/kubernetes/externalapis.healthmonitor.lostinbrittany.org-v1.yml
```

Next, create a custom resource to monitor an external service:

**manifests/github-external-api.yml**
```yml
apiVersion: healthmonitor.lostinbrittany.org/v1alpha1
kind: ExternalApi
metadata:
  name: github
spec:
  serviceUrl: "https://api.github.com"
  pollingInterval: 60
```

Apply this resource to Kubernetes:

```bash
kubectl apply -f manifests/github-external-api.yml
```

If Quarkus is running in dev mode, you should see a log message like:

```log
2024-10-02 20:59:30,874 INFO  [org.los.hea.ExternalApiReconciler] (ReconcilerExecutor-externalapireconciler-1281) Reconciling resource: github
```

Then you can check the status of the `github` `ExternalApi` object in the cluster using `kubectl`:

```bash
kubectl get externalapi github -o yaml
```

You should see the status in the response:

```yaml
apiVersion: healthmonitor.lostinbrittany.org/v1alpha1
kind: ExternalApi
metadata:
  annotations:
    kubectl.kubernetes.io/last-applied-configuration: |
      {"apiVersion":"healthmonitor.lostinbrittany.org/v1alpha1","kind":"ExternalApi","metadata":{"annotations":{},"name":"github","namespace":"default"},"spec":{"pollingInterval":60,"serviceUrl":"https://api.github.com"}}
  creationTimestamp: "2024-10-01T09:36:48Z"
  generation: 1
  name: github
  namespace: default
  resourceVersion: "7842"
  uid: d31a6fc8-2d89-4216-a76d-2c53aa358a8d
spec:
  pollingInterval: 60
  serviceUrl: https://api.github.com
status:
  healthStatus: Healthy
  lastChecked: "2024-10-02T18:58:11.204538609Z"
  responseTime: 377
```
