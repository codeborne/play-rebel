package play.rebel;

import play.data.validation.Validation;
import play.exceptions.UnexpectedException;
import play.libs.MimeTypes;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Scope;
import play.mvc.results.Result;
import play.templates.Template;
import play.templates.TemplateLoader;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * 200 OK with a template rendering
 */
public class RenderView extends Result {

  private final String templateName;
  private final Map<String, Object> arguments = new HashMap<>();
  private String content;
  private long renderTime;

  public RenderView(String templateName) {
    this(templateName, emptyMap());
  }

  public RenderView(String templateName, Map<String, Object> arguments) {
    this.templateName = templateName;
    this.arguments.putAll(Scope.RenderArgs.current().data);
    this.arguments.putAll(arguments);
    generateAuthenticityToken();
  }

  private void generateAuthenticityToken() {
    // it's important to generate authenticityToken in controller, not in `apply` method
    Scope.Session.current().getAuthenticityToken();
  }

  @Override
  public void apply(Http.Request request, Http.Response response) {
    try {
      renderView(response);
    } catch (Exception e) {
      throw new UnexpectedException(e);
    }
  }

  private void renderView(Http.Response response) throws Exception {
    long start = System.currentTimeMillis();
    Template template = resolveTemplate();

    Map<String, Object> templateBinding = new HashMap<>();
    templateBinding.putAll(arguments);
    templateBinding.put("session", Scope.Session.current());
    templateBinding.put("request", Http.Request.current());
    templateBinding.put("flash", Scope.Flash.current());
    templateBinding.put("params", Scope.Params.current());
    templateBinding.put("errors", Validation.errors());

    this.content = template.render(templateBinding);
    this.renderTime = System.currentTimeMillis() - start;
    String contentType = MimeTypes.getContentType(template.name, "text/plain");
    response.out.write(content.getBytes(getEncoding()));
    setContentTypeIfNotSet(response, contentType);
  }

  private Template resolveTemplate() throws Exception {
    return TemplateLoader.load(template(templateName));
  }

  private String template(String templateName) throws Exception {
    Method method = Controller.class.getDeclaredMethod("template", String.class);
    method.setAccessible(true);
    return (String) method.invoke(null, templateName);
  }

  public String getName() {
    return templateName;
  }

  public String getContent() {
    return content;
  }

  public Map<String, Object> getArguments() {
    return arguments;
  }

  public long getRenderTime() {
    return renderTime;
  }

  public RenderView with(String name, Object value) {
    arguments.put(name, value);
    return this;
  }
}

