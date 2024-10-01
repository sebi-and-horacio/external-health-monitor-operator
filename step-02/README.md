# Step 2: **Define the Custom Resource (CRD)**

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
