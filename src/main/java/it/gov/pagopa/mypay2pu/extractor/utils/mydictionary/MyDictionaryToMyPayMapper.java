package it.gov.pagopa.mypay2pu.extractor.utils.mydictionary;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

@Component
public class MyDictionaryToMyPayMapper {

  private static final Type FIELD_BEANS_TYPE = new TypeToken<List<FieldBean>>() {}.getType();
  private static final Set<String> REQUIRED_BOOLEAN_FIELDS = Set.of(
    "is_indexable",
    "is_insertable",
    "is_renderable",
    "is_searchable",
    "is_listable",
    "is_association",
    "is_detail_link"
  );
  private static final Set<String> REQUIRED_NUMBER_FIELDS = Set.of(
    "ins_order",
    "renderable_order",
    "ser_order",
    "lis_order",
    "min_occurences",
    "max_occurences"
  );

  private final Gson gson;

  public MyDictionaryToMyPayMapper(Gson gson) {
    this.gson = gson;
  }

  public MyPayForm map(String myDictionaryJson) {
    if (myDictionaryJson == null || myDictionaryJson.isBlank()) {
      throw new IllegalArgumentException("MyDictionary response body must be a JSON array");
    }

    JsonElement responseBody = parseResponseBody(myDictionaryJson);
    if (!responseBody.isJsonArray()) {
      throw new IllegalArgumentException("MyDictionary response body must be a JSON array");
    }
    validateFieldBeans(responseBody.getAsJsonArray());
    normalizeFieldBeanProperties(responseBody.getAsJsonArray());

    List<FieldBean> fieldBeans;
    try {
      fieldBeans = gson.fromJson(responseBody, FIELD_BEANS_TYPE);
    } catch (JsonParseException e) {
      throw new IllegalArgumentException("MyDictionary response body contains incompatible field beans", e);
    }
    if (fieldBeans == null) {
      throw new IllegalArgumentException("MyDictionary response body must contain field beans");
    }

    addDefaultCssClass(fieldBeans);
    return new MyPayForm(fieldBeans);
  }

  private JsonElement parseResponseBody(String myDictionaryJson) {
    try {
      return JsonParser.parseString(myDictionaryJson);
    } catch (JsonParseException e) {
      throw new IllegalArgumentException("MyDictionary response body is not valid JSON", e);
    }
  }

  private void addDefaultCssClass(List<FieldBean> fieldBeans) {
    for (FieldBean fieldBean : fieldBeans) {
      validateFieldBean(fieldBean);

      if (fieldBean.getHtmlClass() == null || fieldBean.getHtmlClass().isBlank()) {
        fieldBean.setHtmlClass("center");
      }

      if (fieldBean.getSubfields() != null) {
        addDefaultCssClass(fieldBean.getSubfields());
      }
    }
  }

  private void validateFieldBeans(JsonArray fieldBeans) {
    for (JsonElement fieldBean : fieldBeans) {
      if (!fieldBean.isJsonObject()) {
        throw new IllegalArgumentException("MyDictionary response body contains an invalid field bean");
      }

      JsonObject jsonFieldBean = fieldBean.getAsJsonObject();
      requireString(jsonFieldBean, "name");
      requireString(jsonFieldBean, "html_render");
      REQUIRED_BOOLEAN_FIELDS.forEach(fieldName -> requireBoolean(jsonFieldBean, fieldName));
      REQUIRED_NUMBER_FIELDS.forEach(fieldName -> requireNumber(jsonFieldBean, fieldName));

      JsonElement subfields = jsonFieldBean.get("subfields");
      if (subfields != null && !subfields.isJsonNull()) {
        if (!subfields.isJsonArray()) {
          throw new IllegalArgumentException("MyDictionary response body contains invalid subfields");
        }
        validateFieldBeans(subfields.getAsJsonArray());
      }
    }
  }

  private void normalizeFieldBeanProperties(JsonArray fieldBeans) {
    for (JsonElement fieldBean : fieldBeans) {
      JsonObject jsonFieldBean = fieldBean.getAsJsonObject();
      copyProperty(jsonFieldBean, "is_indexable", "isIndexable");
      copyProperty(jsonFieldBean, "is_insertable", "isInsertable");
      copyProperty(jsonFieldBean, "is_renderable", "isRenderable");
      copyProperty(jsonFieldBean, "is_searchable", "isSearchable");
      copyProperty(jsonFieldBean, "is_listable", "isListable");
      copyProperty(jsonFieldBean, "is_association", "isAssociation");
      copyProperty(jsonFieldBean, "is_detail_link", "isDetailLink");
      copyProperty(jsonFieldBean, "association_field", "associationField");

      JsonElement subfields = jsonFieldBean.get("subfields");
      if (subfields != null && !subfields.isJsonNull()) {
        normalizeFieldBeanProperties(subfields.getAsJsonArray());
      }
    }
  }

  private void copyProperty(JsonObject fieldBean, String sourceName, String targetName) {
    JsonElement property = fieldBean.get(sourceName);
    if (property != null) {
      fieldBean.add(targetName, property);
    }
  }

  private void requireString(JsonObject fieldBean, String fieldName) {
    JsonElement fieldValue = fieldBean.get(fieldName);
    if (fieldValue == null || fieldValue.isJsonNull() || !fieldValue.isJsonPrimitive()
      || !fieldValue.getAsJsonPrimitive().isString() || fieldValue.getAsString().isBlank()) {
      throw new IllegalArgumentException("MyDictionary response body contains an invalid " + fieldName);
    }
  }

  private void requireBoolean(JsonObject fieldBean, String fieldName) {
    JsonElement fieldValue = fieldBean.get(fieldName);
    if (fieldValue == null || fieldValue.isJsonNull() || !fieldValue.isJsonPrimitive()
      || !fieldValue.getAsJsonPrimitive().isBoolean()) {
      throw new IllegalArgumentException("MyDictionary response body contains an invalid " + fieldName);
    }
  }

  private void requireNumber(JsonObject fieldBean, String fieldName) {
    JsonElement fieldValue = fieldBean.get(fieldName);
    if (fieldValue == null || fieldValue.isJsonNull() || !fieldValue.isJsonPrimitive()
      || !fieldValue.getAsJsonPrimitive().isNumber()) {
      throw new IllegalArgumentException("MyDictionary response body contains an invalid " + fieldName);
    }
  }

  private void validateFieldBean(FieldBean fieldBean) {
    if (fieldBean == null || fieldBean.getHtmlRender() == null) {
      throw new IllegalArgumentException("MyDictionary response body contains an invalid field bean");
    }
  }
}
