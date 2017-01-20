package play.rebel;

import play.Play;
import play.PlayPlugin;
import play.classloading.enhancers.ControllersEnhancer;
import play.data.validation.Validation;
import play.mvc.Http;
import play.mvc.Scope;

import java.lang.reflect.Method;

import static play.rebel.Bridge.setControllerField;

/**
 * NB! It's important to include this plugin to `play.plugins` AFTER guice plugin (that has priority 1000):
 * 
 * ```
 * 1001:play.rebel.PlayRebelPlugin
 * ```
 */
public class PlayRebelPlugin extends PlayPlugin {
  @Override public void beforeActionInvocation(Method actionMethod) {
    setRebelControllerFields(Scope.Params.current(), Http.Request.current(), Http.Response.current(),
        Scope.Session.current(), Scope.Flash.current(), Scope.RenderArgs.current(),
        Scope.RouteArgs.current(), Validation.current());
    
    // we will uncomment this later, when RebelController will not extend play.mvc.Controller
    // resetStaticControllerFields();
  }

  @Override public void afterActionInvocation() {
    setRebelControllerFields(null, null, null, null, null, null, null, null);
  }

  private void setRebelControllerFields(Scope.Params params, Http.Request request, Http.Response response,
                                        Scope.Session session, Scope.Flash flash, Scope.RenderArgs renderArgs,
                                        Scope.RouteArgs routeArgs, Validation validation) {
    ControllersEnhancer.ControllerSupport c = Http.Request.current().controllerInstance;
    if (c instanceof RebelController) {
      RebelController controller = (RebelController) c;
      controller.params = params;
      controller.request = request;
      controller.response = response;
      controller.session = session;
      controller.flash = flash;
      controller.renderArgs = renderArgs;
      controller.routeArgs = routeArgs;
      controller.validation = validation;
    }
  }

  private void resetStaticControllerFields() {
    if (Play.mode == Play.Mode.DEV) {
      try {
        setControllerField("params", null);
        setControllerField("request", null);
        setControllerField("response", null);
        setControllerField("session", null);
        setControllerField("flash", null);
        setControllerField("renderArgs", null);
        setControllerField("routeArgs", null);
        setControllerField("validation", null);
      }
      catch (RuntimeException e) {
        throw e;
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
}
