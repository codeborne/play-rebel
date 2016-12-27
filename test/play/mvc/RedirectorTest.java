package play.mvc;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import play.Play;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class RedirectorTest {
  Redirector redirector;

  @Before
  public void setUp() {
    Play.configuration.clear();
    redirector = spy(new Redirector());
    doNothing().when(redirector).toUrl(anyString());
  }

  @Test
  public void toUrlWithParams_empty() {
    redirector.toUrl("/foo/bar", emptyMap());
    verify(redirector).toUrl("/foo/bar");
  }

  @Test
  public void toUrlWithParams_simple() {
    redirector.toUrl("/foo/bar", ImmutableMap.of("a", "1"));
    verify(redirector).toUrl("/foo/bar?a=1");
  }

  @Test
  public void toUrlWithParams_multiple() {
    redirector.toUrl("/data", ImmutableMap.of("a", "1", "b", "7"));
    verify(redirector).toUrl("/data?a=1&b=7");
  }

  @Test
  public void toUrlWithParams_escaping() {
    redirector.toUrl("/data", ImmutableMap.of("k=ey2&", "valu ?e2"));
    verify(redirector).toUrl("/data?k%3Dey2%26=valu+%3Fe2");
  }

  @Test
  public void toUrlWithParams_nullValue() {
    redirector.toUrl("/foo/bar", singletonMap("b", null));
    verify(redirector).toUrl("/foo/bar");
  }

  @Test
  public void toUrlWithParams_nullNameThrows() {
    try {
      redirector.toUrl("url", singletonMap(null, "value"));
      fail();
    }
    catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).contains("Blank");
    }
  }

  @Test
  public void toUrl_withNullNameThrows() {
    try {
      redirector.toUrl("url", null, "value");
      fail();
    }
    catch (IllegalArgumentException e) {
      assertThat(e.getMessage()).contains("Blank");
    }
  }

  @Test
  public void toUrl_withNullValueIgnoresIt() {
    redirector.toUrl("/url", "name", null);
    verify(redirector).toUrl("/url");
  }

  @Test
  public void toUrlWithParams_urlWithParams() {
    redirector.toUrl("/foo/bar?a=5", singletonMap("b", "6"));
    verify(redirector).toUrl("/foo/bar?a=5&b=6");
  }

  @Test
  public void toUrlWithParams_urlWithDateParam() {
    Date date = Date.from(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    redirector.toUrl("/foo/bar", singletonMap("date", date));
    verify(redirector).toUrl("/foo/bar?date=01.01.2000");
  }

  @Test
  public void toUrlWith1Param() {
    redirector.toUrl("/url", "name", "value");
    verify(redirector).toUrl("/url?name=value");
  }

  @Test
  public void toUrlWith2Params() {
    redirector.toUrl("/url", "name", "value", "name2", 1);
    verify(redirector).toUrl("/url?name=value&name2=1");
  }

  @Test
  public void toUrlWith3Params() {
    redirector.toUrl("/url", "name", "value", "name2", 1, "name3", true);
    verify(redirector).toUrl("/url?name=value&name2=1&name3=true");
  }

  @Test
  public void toUrlWith4Params() {
    redirector.toUrl("/url", "name", "value", "name2", 1, "name3", true, "name4", 4L);
    verify(redirector).toUrl("/url?name=value&name2=1&name3=true&name4=4");
  }

  @Test
  public void toUrlWith5Params() {
    Date date = Date.from(LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    redirector.toUrl("/url", "name", "value", "name2", 1, "name3", true, "name4", 4L, "date", date);
    verify(redirector).toUrl("/url?name=value&name2=1&name3=true&name4=4&date=01.01.2000");
  }
}