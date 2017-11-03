package play.classloading;

import org.junit.Before;
import org.junit.Test;
import play.Play;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.exceptions.RestartNeededException;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RebelClassloaderTest {
  RebelClassloader cl;

  @Before
  public void setUp() {
    Play.classes = new ApplicationClasses();
    cl = new RebelClassloader();
  }

  @Test
  public void returnsNullForUnexistingClass() throws ClassNotFoundException {
    assertThat(cl.loadClass("Script1BeanInfo", true), equalTo(null));
  }

  @Test
  public void doesNotTryToLoadUnexistingClassMultipleTimes() throws ClassNotFoundException {
    cl.loadClass("Script1BeanInfo", true);
    cl = spy(cl);

    assertThat(cl.loadClass("Script1BeanInfo", true), equalTo(null));
    verify(cl, never()).loadApplicationClass(anyString());
  }

  @Test
  public void loadsClassForApplicationClasses() throws ClassNotFoundException {
    ApplicationClass applicationClass = new ApplicationClass("com.my.User");
    applicationClass.javaClass = User.class;
    Play.classes.add(applicationClass);

    assertThat(cl.loadClass("com.my.User", true), equalTo(User.class));
  }

  @Test
  public void cachesLoadedClasses() throws ClassNotFoundException {
    ApplicationClass applicationClass = new ApplicationClass("com.my.User");
    applicationClass.javaClass = User.class;
    Play.classes.add(applicationClass);
    cl.loadClass("com.my.User", true);

    cl = spy(cl);
    assertThat(cl.loadClass("com.my.User", true), equalTo(User.class));
    verify(cl, never()).loadApplicationClass(anyString());
  }

  @Test
  public void doesNotDetectChanges() throws RestartNeededException {
    Play.classes = mock(ApplicationClasses.class);

    cl.detectChanges();

    verify(Play.classes, never()).all();
  }

  private static class User {}
}