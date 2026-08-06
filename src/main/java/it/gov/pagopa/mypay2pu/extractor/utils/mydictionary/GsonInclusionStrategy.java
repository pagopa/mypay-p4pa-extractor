package it.gov.pagopa.mypay2pu.extractor.utils.mydictionary;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.lang.annotation.Annotation;

public class GsonInclusionStrategy implements ExclusionStrategy {

  private final Class<? extends Annotation> annotation;

  public GsonInclusionStrategy(Class<? extends Annotation> annotation) {
    this.annotation = annotation;
  }

  @Override
  public boolean shouldSkipField(FieldAttributes fieldAttributes) {
    return fieldAttributes.getAnnotation(annotation) == null;
  }

  @Override
  public boolean shouldSkipClass(Class<?> clazz) {
    return false;
  }
}
