package play.mvc;

import org.apache.commons.collections.map.LinkedMap;
import play.Play;
import play.data.binding.Unbinder;
import play.mvc.results.Redirect;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang.StringUtils.isBlank;

@Singleton
public class Redirector {

  private String dateFormat = Play.configuration.getProperty("date.format", "dd.MM.yyyy");

  public void to(String action, Parameter... parameters) {
    to(action, asList(parameters));
  }
  
  public void to(String action, List<Parameter> parameters) {
    to(action, toMap(parameters));
  }
  
  public void to(String action, Map<String, Object> parameters) {
    if ((action.startsWith("/") || action.startsWith("http://") || action.startsWith("https://")) && parameters.isEmpty()) {
      toUrl(action);
    }
    else if (action.startsWith("@")) {
      action = action.substring(1);
      if (!action.contains(".")) action = Http.Request.current().controller + "." + action;
    }
    Router.ActionDefinition actionDefinition = parameters.isEmpty() ? Router.reverse(action) : Router.reverse(action, parameters);
    throw new Redirect(actionDefinition.toString(), false);
  }

  public void toUrl(String url) {
    throw new Redirect(url, false);
  }

  public void toUrl(String url, Map<String, Object> parameters) {
    if (parameters.entrySet().stream().anyMatch(e -> isBlank(e.getKey()))) {
      throw new IllegalArgumentException("Paramater name can not me Blank");
    }
    String parametersPart = parameters.entrySet().stream()
        .filter(e -> e.getValue() != null)
        .map(e -> encode(e.getKey()) + "=" + encode(paramToString(e.getValue())))
        .collect(joining("&"));
    String separator = parametersPart.isEmpty() ? "" : url.contains("?") ? "&" : "?";
    toUrl(url + separator + parametersPart);
  }

  public void toUrl(String url, String paramName, @Nullable Object paramValue) {
    toUrlByVarArgs(url, paramName, paramValue);
  }

  public void toUrl(String url,
                    String param1Name, @Nullable Object param1value,
                    String param2name, @Nullable Object param2value) {
    toUrlByVarArgs(url, param1Name, param1value, param2name, param2value);
  }

  public void toUrl(String url,
                    String param1Name, @Nullable Object param1value,
                    String param2name, @Nullable Object param2value,
                    String param3name, @Nullable Object param3value) {
    toUrlByVarArgs(url, param1Name, param1value, param2name, param2value, param3name, param3value);
  }

  public void toUrl(String url,
                    String param1Name, @Nullable Object param1value,
                    String param2name, @Nullable Object param2value,
                    String param3name, @Nullable Object param3value,
                    String param4name, @Nullable Object param4value) {
    toUrlByVarArgs(url, param1Name, param1value, param2name, param2value, param3name, param3value, param4name, param4value);
  }

  public void toUrl(String url,
                    String param1Name, @Nullable Object param1value,
                    String param2name, @Nullable Object param2value,
                    String param3name, @Nullable Object param3value,
                    String param4name, @Nullable Object param4value,
                    String param5name, @Nullable Object param5value) {
    toUrlByVarArgs(url, param1Name, param1value, param2name, param2value, param3name, param3value,
        param4name, param4value, param5name, param5value);
  }

  private void toUrlByVarArgs(String url, Object... args) {
    Map<String, Object> parameters = new LinkedMap(args.length / 2);
    for (int i = 0; i < args.length - 1; i += 2) {
      parameters.put((String) args[i], args[i + 1]);
    }
    toUrl(url, parameters);
  }

  private String paramToString(Object value) {
    if (value instanceof Date) {
      return new SimpleDateFormat(dateFormat).format((Date) value);
    }
    return String.valueOf(value);
  }

  private static String encode(String parameter) {
    try {
      return parameter == null ? "" : URLEncoder.encode(parameter, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException(e);
    }
  }
  
  public Parameters with(String name, Object value) {
    return new Parameters(name, value);
  }

  public static Parameter param(String name, Object value) {
    return new Parameter(name, value);
  }

  private static final Annotation[] NO_ANNOTATIONS = new Annotation[0];
  
  private static Map<String, Object> toMap(List<Parameter> parameters) {
    Map<String, Object> newArgs = new HashMap<>(parameters.size());
    for (Parameter parameter : parameters) {
      Unbinder.unBind(newArgs, parameter.value, parameter.name, NO_ANNOTATIONS);
    }
    return newArgs;
  }

  public static final class Parameter {
    public final String name;
    public final Object value;

    public Parameter(String name, Object value) {
      this.name = name;
      this.value = value;
    }

    @Override public boolean equals(Object other) {
      if (this == other) return true;
      if (other == null || getClass() != other.getClass()) return false;
      Parameter parameter = (Parameter) other;
      return Objects.equals(name, parameter.name) && Objects.equals(value, parameter.value);
    }

    @Override public int hashCode() {
      return Objects.hash(name, value);
    }

    @Override public String toString() {
      return name + '=' + value;
    }
  }

  public class Parameters {
    private final Map<String, Object> parameters = new HashMap<>();

    public Parameters(String name, Object value) {
      with(name, value);
    }

    public final Parameters with(String name, Object value) {
      Unbinder.unBind(parameters, value, name, NO_ANNOTATIONS);
      return this;
    }

    public void to(String action) {
      Redirector.this.to(action, parameters);
    }
  }
}
