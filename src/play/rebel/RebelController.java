package play.rebel;

import com.thoughtworks.xstream.XStream;
import org.w3c.dom.Document;
import play.Logger;
import play.data.validation.Validation;
import play.exceptions.UnexpectedException;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.PlayController;
import play.mvc.Scope;
import play.mvc.results.*;
import play.mvc.results.Error;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * A superclass for all controllers that are intended to work without play enhancers.
 */
public class RebelController implements PlayController {

  protected Http.Request request = request();

  protected static Http.Request request() {
    return Http.Request.current();
  }
  
  protected Http.Response response = response();
  
  protected static Http.Response response() {
    return Http.Response.current();
  }

  protected Scope.Session session = session();
  
  protected static Scope.Session session() {
    return Scope.Session.current();
  }

  protected Scope.Flash flash = flash();
  
  protected static Scope.Flash flash() {
    return Scope.Flash.current();
  }

  protected static void flash(String key, Object value) {
    Scope.Flash.current().put(key, value);
  }

  protected Scope.Params params = params();
  
  protected static Scope.Params params() {
    return Scope.Params.current();
  }
  
  protected Scope.RenderArgs renderArgs = renderArgs();
  
  protected static Scope.RenderArgs renderArgs() {
    return Scope.RenderArgs.current();
  }

  protected Scope.RouteArgs routeArgs = Scope.RouteArgs.current();
  
  protected Validation validation = Validation.current();

  protected static void checkAuthenticity() {
    if (Scope.Params.current().get("authenticityToken") == null
        || !Scope.Params.current().get("authenticityToken").equals(Scope.Session.current().getAuthenticityToken())) {
      throw new Forbidden("Bad authenticity token");
    }
  }

  protected void render() {
    renderTemplate(template(), emptyMap());
  }
  
  protected static void render(String templateName) {
    renderTemplate(templateName, emptyMap());
  }
  
  protected void renderTemplate(Map<String, Object> args) {
    renderTemplate(template(), args);
  }
  
  protected static void renderTemplate(String templateName, Map<String, Object> args) {
    try {
      Method method = Controller.class.getDeclaredMethod("renderTemplate", String.class, Map.class);
      method.setAccessible(true);
      method.invoke(null, templateName, args);
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (InvocationTargetException e) {
      if (e.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException) e.getTargetException();
      }
      else {
        throw new UnexpectedException(e.getTargetException());
      }
    }
    catch (Exception e) {
      throw new UnexpectedException(e);
    }
  }
  
  protected String template() {
    return Bridge.template();
  }
  
  protected String template(String templateName) {
    return Bridge.template(templateName);
  }
  
  protected static void redirect(String url) {
    throw new Redirect(url);
  }
  
  protected static void redirect(String url, boolean permanent) {
    throw new Redirect(url, permanent);
  }
  
  protected static void renderJSON(String jsonString) {
    throw new RenderJson(jsonString);
  }

  protected static void renderJSON(Object o) {
    throw new RenderJson(o);
  }

  protected static void renderText(CharSequence pattern, Object... args) {
    throw new RenderText(pattern == null ? "" : String.format(pattern.toString(), args));
  }

  protected static void renderText(Object text) {
    throw new RenderText(text == null ? "" : text.toString());
  }

  protected static void renderHtml(Object html) {
    throw new RenderHtml(html == null ? "" : html.toString());
  }

  protected static void renderXml(String xml) {
    throw new RenderXml(xml);
  }

  protected static void renderXml(Document xml) {
    throw new RenderXml(xml);
  }

  protected static void renderXml(Object o) {
    throw new RenderXml(o);
  }

  protected static void renderXml(Object o, XStream xstream) {
    throw new RenderXml(o, xstream);
  }

  protected static void renderBinary(InputStream is) {
    throw new RenderBinary(is, null, true);
  }

  protected static void renderBinary(InputStream is, long length) {
    throw new RenderBinary(is, null, length, true);
  }

  protected static void renderBinary(InputStream is, String name) {
    throw new RenderBinary(is, name, false);
  }
  
  protected static void renderBinary(InputStream is, String name, long length) {
    throw new RenderBinary(is, name, length, false);
  }

  protected static void renderBinary(InputStream is, String name, boolean inline) {
    throw new RenderBinary(is, name, inline);
  }

  protected static void renderBinary(InputStream is, String name, long length, boolean inline) {
    throw new RenderBinary(is, name, length, inline);
  }

  protected static void renderBinary(InputStream is, String name, String contentType, boolean inline) {
    throw new RenderBinary(is, name, contentType, inline);
  }

  protected static void renderBinary(InputStream is, String name, long length, String contentType, boolean inline) {
    throw new RenderBinary(is, name, length, contentType, inline);
  }

  protected static void renderBinary(File file) {
    throw new RenderBinary(file);
  }
  
  protected static void renderBinary(File file, String name) {
    throw new RenderBinary(file, name);
  }

  protected static void forbidden() {
    throw new Forbidden("Access denied");
  }

  protected static void forbidden(String reason) {
    throw new Forbidden(reason);
  }

  protected static void notFound() {
    notFound("");
  }

  protected static void notFound(String what) {
    throw new NotFound(what);
  }

  protected static void notFoundIfNull(Object o) {
    if (o == null) {
      notFound();
    }
  }

  protected static void error() {
    error("Internal Error");
  }

  protected static void error(Exception reason) {
    Logger.error(reason, "error()");
    throw new Error(reason.toString());
  }

  protected static void error(String reason) {
    throw new play.mvc.results.Error(reason);
  }

  protected static void error(int status, String reason) {
    throw new Error(status, reason);
  }

  protected static void badRequest(String msg) {
    throw new BadRequest(msg);
  }

  protected static void badRequest() {
    throw new BadRequest("Bad request");
  }

  protected static <T extends Annotation> T getActionAnnotation(Class<T> annotationClass) {
    return request().invokedMethod.getAnnotation(annotationClass);
  }

  protected static <T extends Annotation> T getControllerInheritedAnnotation(Class<T> annotationClass) {
    return Bridge.getControllerInheritedAnnotation(annotationClass);
  }
}
