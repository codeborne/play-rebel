package play.mvc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import play.data.validation.Validation;
import play.templates.BaseTemplate;
import play.templates.TemplateLoader;

import javax.inject.Singleton;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static play.mvc.Controller.*;

@Singleton
public class Renderer {
  private static final Gson gsonWithTimeFormat = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
  
  public void template() {
    renderTemplate(getTemplateName(), emptyMap());
  }
  
  public void template(String templateName) {
    renderTemplate(templateName, emptyMap());
  }

  public void template(String templateName, Map<String, Object> args) {
    renderTemplate(templateName, args);
  }

  public Parameters with(String name, Object value) {
    return new Parameters(name, value);
  }

  public void text(String text) {
    renderText(text);
  }

  public void html(String html) {
    renderHtml(html);
  }

  public void xml(Object arg) {
    renderXml(arg);
  }

  public void json(Object arg) {
    if (arg instanceof String)
      renderJSON((String) arg);
    else
      renderJSON(arg, gsonWithTimeFormat);
  }

  public void file(File file, String fileName) {
    renderBinary(file, fileName);
  }

  public String templateAsString(String templateName) {
    Scope.RenderArgs templateBinding = Scope.RenderArgs.current();
    templateBinding.data.putAll(renderArgs.data);
    templateBinding.put("session", Scope.Session.current());
    templateBinding.put("request", Http.Request.current());
    templateBinding.put("flash", Scope.Flash.current());
    templateBinding.put("params", Scope.Params.current());
    templateBinding.put("errors", Validation.errors());
    BaseTemplate t = (BaseTemplate) TemplateLoader.load(templateName);
    return t.render(templateBinding.data);
  }

  public class Parameters {
    private final Map<String, Object> parameters = new HashMap<>();

    private Parameters(String name, Object value) {
      with(name, value);
    }

    public final Parameters with(String name, Object value) {
      parameters.put(name, value);
      return this;
    }

    public void template() {
      renderTemplate(getTemplateName(), parameters);
    }

    public void template(String templateName) {
      renderTemplate(templateName, parameters);
    }
    
    public void json() {
      renderJSON(gsonWithTimeFormat.toJson(parameters));
    }

    public void json(Gson customGson) {
      renderJSON(customGson.toJson(parameters));
    }
  }

  public void renderTemplate(String templateName, Map<String, Object> args) {
    try {
      Method method = Controller.class.getDeclaredMethod("renderTemplate", String.class, Map.class);
      method.setAccessible(true);
      method.invoke(null, templateName, args);
    }
    catch (NoSuchMethodException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    catch (InvocationTargetException e) {
      throw e.getTargetException() instanceof RuntimeException ? (RuntimeException) e.getTargetException() :
          new RuntimeException(e);
    }
  }

  private String getTemplateName() {
    try {
      Method method = Controller.class.getDeclaredMethod("template");
      method.setAccessible(true);
      return (String) method.invoke(null);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
