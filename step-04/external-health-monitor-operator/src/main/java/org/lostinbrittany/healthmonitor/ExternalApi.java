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