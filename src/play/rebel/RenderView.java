package play.rebel;

import play.exceptions.UnexpectedException;
import play.libs.MimeTypes;
import play.mvc.Controller;
import play.mvc.Http;
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
    this.arguments.putAll(arguments);
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
    this.content = template.render(arguments);
    this.renderTime = System.currentTimeMillis() - start;
    String contentType = MimeTypes.getContentType(templateName, "text/plain");
    response.out.write(content.getBytes(getEncoding()));
    setContentTypeIfNotSet(response, contentType);
  }

  private Template resolveTemplate() throws Exception {
    return TemplateLoader.load(template(templateName));
  }

  private String template(String templateName) throws Exception {
    Method method = Controller.class.getMethod("template", String.class);
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

