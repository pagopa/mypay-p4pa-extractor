package it.gov.pagopa.mypay2pu.extractor.utils.mydictionary;

import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class GsonInclusionStrategyTest {

  @Test
  void shouldSerializeOnlyFieldsAnnotatedWithConfiguredAnnotation() {
    AnnotatedPayload payload = new AnnotatedPayload();
    String json = new GsonBuilder()
      .setExclusionStrategies(new GsonInclusionStrategy(Exported.class))
      .create()
      .toJson(payload);

    assertEquals("{\"included\":\"value\"}", json);
    assertEquals("hidden", payload.getExcluded());
  }

  @Test
  void shouldNotSkipClasses() {
    GsonInclusionStrategy strategy = new GsonInclusionStrategy(Exported.class);

    assertFalse(strategy.shouldSkipClass(AnnotatedPayload.class));
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.FIELD)
  private @interface Exported {
  }

  private static class AnnotatedPayload {

    @Exported
    private final String included = "value";
    private final String excluded = "hidden";

    private String getExcluded() {
      return excluded;
    }
  }
}
