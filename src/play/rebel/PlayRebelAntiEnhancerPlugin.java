package play.rebel;

import play.Play;
import play.PlayPlugin;
import play.classloading.ApplicationClasses;
import play.classloading.ApplicationClassloader;
import play.classloading.RebelClassloader;

import java.util.List;

public class PlayRebelAntiEnhancerPlugin extends PlayPlugin {
  public PlayRebelAntiEnhancerPlugin() {
    Play.classloader = new RebelClassloader();
    compileSources();
  }

  @Override public void onConfigurationRead() {
    Play.classloader = new RebelClassloader();
    resetClassloaders(Play.classloader);
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
