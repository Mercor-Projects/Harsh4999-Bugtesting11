/**
 * Copyright (C) 2015 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.fabric8.kubernetes.examples.kubectl.equivalents;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java equivalent of `kubectl get pods --all-namespaces`. Note that this might
 * require Cluster Admin privileges
 */
public class PodListGlobalEquivalent {
  private static final Logger logger = LoggerFactory.getLogger(PodListGlobalEquivalent.class);

  public static void main(String[] args) {
    try (final KubernetesClient k8s = new DefaultKubernetesClient()) {
      k8s.pods().inAnyNamespace().list().getItems()
        .stream()
        .map(Pod::getMetadata)
        .map(ObjectMeta::getName)
        .forEach(logger::info);
    }
  }
}
