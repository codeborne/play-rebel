package play.rebel;

import play.Logger;
import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.ApplicationClassloader;
import play.classloading.RebelClassloader;

import java.util.List;

public class PlayRebelAntiEnhancerPlugin extends PlayPlugin {
  public PlayRebelAntiEnhancerPlugin() {
    if (enabled()) {
      Logger.info(" *** REBEL: Play compilation and enhancers disabled *** ");
      Play.classloader = new RebelClassloader();
      compileSources();
    }
    else {
      Logger.info(" *** REBEL: Play compilation and enhancers are active *** ");
    }
  }

  @Override public void onConfigurationRead() {
    if (enabled()) {
      Logger.info(" *** REBEL: Play compilation and enhancers disabled *** ");
      Play.classloader = new RebelClassloader();
      resetContextClassloader(Play.classloader);
    }
    else {
      Logger.info(" *** REBEL: Play compilation and enhancers are active *** ");
    }
  }

  private boolean enabled() {
    return Play.mode.isDev() && !Play.usePrecompiled && !Play.forceProd && System.getProperty("precompile") == null;
  }

  private void resetContextClassloader(ApplicationClassloader classloader) {
    Thread thread = Thread.currentThread();
    if (thread.getContextClassLoader() instanceof ApplicationClassloader)
      thread.setContextClassLoader(classloader);
  }

  @Override public final boolean compileSources() {
    if (enabled()) {
      List<Class<?>> allClasses = JavaClasses.allClassesInProject();

      for (Class<?> javaClass : allClasses) {
        ApplicationClass appClass = new ApplicationClass(javaClass.getName());
        appClass.javaClass = javaClass;
        Play.classes.add(appClass);
      }

      return true;
    }
    else {
      return false;
    }
  }
}
