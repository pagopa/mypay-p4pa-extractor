package it.gov.pagopa.mypay2pu.extractor.utils.mydictionary;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class FieldBeanTest {

  @Test
  void toJsonShouldSerializeAllFieldsAndFilterByAnnotation() {
    FieldBean fieldBean = FieldBean.builder()
      .name("field")
      .htmlRender(RenderType.TEXT)
      .htmlClass("center")
      .isInsertable(true)
      .build();

    JsonObject json = JsonParser.parseString(fieldBean.toJson()).getAsJsonObject();
    JsonObject annotatedJson = JsonParser.parseString(fieldBean.toJson(SerializedName.class)).getAsJsonObject();

    assertEquals("field", json.get("name").getAsString());
    assertEquals(true, json.get("isInsertable").getAsBoolean());
    assertEquals("field", annotatedJson.get("name").getAsString());
    assertEquals("TEXT", annotatedJson.get("html_render").getAsString());
    assertFalse(annotatedJson.has("isInsertable"));
  }

  @Test
  void getMapOfExtraAttrByKeyShouldDeserializeConfiguredValueOrReturnEmptyMap() {
    FieldBean fieldBean = FieldBean.builder()
      .extraAttr(Map.of("configuration", "{'key':'value'}"))
      .build();

    Map<String, String> result = fieldBean.getMapOfExtraAttrByKey("configuration");

    assertEquals(Map.of("key", "value"), result);
    assertEquals(Map.of(), fieldBean.getMapOfExtraAttrByKey("missing"));
  }
}
