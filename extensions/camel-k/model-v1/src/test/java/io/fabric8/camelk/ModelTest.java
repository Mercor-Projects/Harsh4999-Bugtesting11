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
package io.fabric8.camelk;

import io.fabric8.camelk.v1.Integration;
import io.fabric8.camelk.v1.IntegrationBuilder;
import org.junit.jupiter.api.Test;

class ModelTest {

  @Test
  void shouldCreateIntegration() {
    Integration integration = new IntegrationBuilder()
        .withNewMetadata()
        .withName("my-integration")
        .endMetadata()
        .withNewSpec()
        //.addToFlows("form this to that")
        .endSpec()
        .build();
  }
}
