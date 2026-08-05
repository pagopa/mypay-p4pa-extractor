package it.gov.pagopa.mypay2pu.extractor.utils.mydictionary;

import com.fasterxml.jackson.databind.JsonNode;
import it.gov.pagopa.mypay2pu.extractor.config.json.JsonConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MyPayFormSerializationTest {

  @Test
  void serializeShouldExposeBooleanPropertiesInCamelCase() throws Exception {
    MyPayForm form = new MyDictionaryToMyPayMapper(new com.google.gson.Gson()).map("""
      [{
        "name":"field",
        "html_render":"TEXT",
        "ins_order":1,
        "is_indexable":true,
        "renderable_order":2,
        "ser_order":3,
        "lis_order":4,
        "is_insertable":true,
        "is_renderable":true,
        "is_searchable":true,
        "is_listable":true,
        "is_association":false,
        "is_detail_link":false,
        "min_occurences":0,
        "max_occurences":1
      }]
      """);
    JsonConfig jsonConfig = new JsonConfig();

    JsonNode fieldBean = jsonConfig
      .objectMapper()
      .readTree(jsonConfig.objectMapper().writeValueAsString(form))
      .path("fieldBeans")
      .get(0);

    assertEquals(true, fieldBean.path("indexable").asBoolean());
    assertEquals(true, fieldBean.path("insertable").asBoolean());
    assertEquals(true, fieldBean.path("renderable").asBoolean());
    assertFalse(fieldBean.has("isIndexable"));
    assertFalse(fieldBean.has("isInsertable"));
    assertFalse(fieldBean.has("isRenderable"));
  }
}
