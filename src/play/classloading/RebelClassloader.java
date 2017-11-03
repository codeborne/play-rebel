package play.classloading;


import play.Play;
import play.exceptions.RestartNeededException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RebelClassloader extends ApplicationClassloader {
  private static final Map<String, Class<?>> cache = new ConcurrentHashMap<>();
  private static final Map<String, Boolean> unexistingClasses = new ConcurrentHashMap<>();

  @Override
  protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    Class<?> aClass = cache.get(name);
    if (aClass == null) {
      if (unexistingClasses.get(name) != null) return null;

      try {
        aClass = super.loadClass(name, resolve);
        cache.put(name, aClass);
      }
      catch (ClassNotFoundException e) {
        unexistingClasses.put(name, true);
      }
    }
    return aClass;
  }

  @Override public Class<?> loadApplicationClass(String name) {
    ApplicationClasses.ApplicationClass applicationClass = Play.classes.classes.get(name);
    return applicationClass == null ? null : applicationClass.javaClass;
  }

  @Override public void detectChanges() throws RestartNeededException {
  }
}

