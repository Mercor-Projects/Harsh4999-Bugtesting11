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

package io.fabric8.kubernetes.client.informers.cache;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.client.informers.cache.ProcessorListener.Notification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Wraps a {@link Cache} and a {@link SharedProcessor} to distribute events related to changes and syncs
 */
public class ProcessorStore<T extends HasMetadata> implements SyncableStore<T> {

  private Cache<T> cache;
  private SharedProcessor<T> processor;
  private AtomicBoolean synced = new AtomicBoolean();
  private List<String> deferredAdd = new ArrayList<>();

  public ProcessorStore(Cache<T> cache, SharedProcessor<T> processor) {
    this.cache = cache;
    this.processor = processor;
  }

  @Override
  public void add(T obj) {
    update(obj);
  }

  private Notification<T> updateInternal(T obj) {
    T oldObj = this.cache.put(obj);
    Notification<T> notification = null;
    if (oldObj != null) {
      if (!Objects.equals(oldObj.getMetadata().getResourceVersion(), obj.getMetadata().getResourceVersion())) {
        notification = new ProcessorListener.UpdateNotification<>(oldObj, obj);
      }
    } else if (synced.get()) {
      notification = new ProcessorListener.AddNotification<>(obj);
    } else {
      deferredAdd.add(getKey(obj));
    }
    return notification;
  }

  @Override
  public void update(T obj) {
    Notification<T> notification = updateInternal(obj);
    if (notification != null) {
      this.processor.distribute(notification, false);
    }
  }

  @Override
  public void delete(T obj) {
    Object oldObj = this.cache.remove(obj);
    if (oldObj != null) {
      this.processor.distribute(new ProcessorListener.DeleteNotification<>(obj, false), false);
    }
  }

  @Override
  public List<T> list() {
    return cache.list();
  }

  @Override
  public List<String> listKeys() {
    return cache.listKeys();
  }

  @Override
  public T get(T object) {
    return cache.get(object);
  }

  @Override
  public T getByKey(String key) {
    return cache.getByKey(key);
  }

  @Override
  public void retainAll(Set<String> nextKeys) {
    if (synced.compareAndSet(false, true)) {
      deferredAdd.stream().map(cache::getByKey).filter(Objects::nonNull)
          .forEach(v -> this.processor.distribute(new ProcessorListener.AddNotification<>(v), false));
      deferredAdd.clear();
    }
    List<T> current = cache.list();
    if (nextKeys.isEmpty() && current.isEmpty()) {
      this.processor.distribute(l -> l.getHandler().onNothing(), false);
      return;
    }
    current.forEach(v -> {
      String key = cache.getKey(v);
      if (!nextKeys.contains(key)) {
        cache.remove(v);
        this.processor.distribute(new ProcessorListener.DeleteNotification<>(v, true), false);
      }
    });
  }

  @Override
  public String getKey(T obj) {
    return cache.getKey(obj);
  }

  @Override
  public void resync() {
    this.cache.list()
        .forEach(i -> this.processor.distribute(new ProcessorListener.UpdateNotification<>(i, i), true));
  }

}