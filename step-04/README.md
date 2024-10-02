# Step 4: Deploying the CR

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

