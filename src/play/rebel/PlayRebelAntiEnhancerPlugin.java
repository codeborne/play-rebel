package play.rebel;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses;
import play.classloading.ApplicationClassloader;
import play.classloading.RebelClassloader;

import java.util.List;

public class PlayRebelAntiEnhancerPlugin extends PlayPlugin {
  public PlayRebelAntiEnhancerPlugin() {
    if (enabled()) {
      Logger.info(" *** REBEL: disable enhancers *** ");
      Play.classloader = new RebelClassloader();
      compileSources();
    }
    else {
      Logger.info(" *** REBEL: still sleeping *** ");
    }
  }

  @Override public void onConfigurationRead() {
    if (enabled()) {
      Logger.info(" *** REBEL: disable enhancers *** ");
      Play.classloader = new RebelClassloader();
      resetClassloaders(Play.classloader);
    }
    else {
      Logger.info(" *** REBEL: still sleeping *** ");
    }
  }

  private boolean enabled() {
    return Play.mode.isDev();
  }

  static void resetClassloaders(ApplicationClassloader classloader) {
    Thread thread = Thread.currentThread();
    if (thread.getContextClassLoader() instanceof ApplicationClassloader)
      thread.setContextClassLoader(classloader);
  }

  @Override public final boolean compileSources() {
    List<Class<?>> allClasses = JavaClasses.allClassesInProject();

    for (Class<?> javaClass : allClasses) {
      ApplicationClasses.ApplicationClass appClass = new ApplicationClasses.ApplicationClass(javaClass.getName());
      appClass.javaClass = javaClass;
      Play.classes.add(appClass);
    }

    return true;
  }
}
