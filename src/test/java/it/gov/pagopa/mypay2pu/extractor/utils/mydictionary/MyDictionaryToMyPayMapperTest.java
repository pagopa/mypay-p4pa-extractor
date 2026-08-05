package it.gov.pagopa.mypay2pu.extractor.utils.mydictionary;

import com.google.gson.Gson;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MyDictionaryToMyPayMapperTest {

  private final MyDictionaryToMyPayMapper mapper = new MyDictionaryToMyPayMapper(new Gson());

  @Test
  void mapShouldPreserveSupportedFieldsAndNormalizeBlankHtmlClassRecursively() {
    MyPayForm result = mapper.map("""
      [{
        "name":"root",
        "required":true,
        "regex":".*",
        "html_render":"DYNAMIC_SELECT",
        "html_class":null,
        "html_label":"Root label",
        "html_placeholder":"Root placeholder",
        "bind_cms":"rootCms",
        "default_value":"rootDefault",
        "ins_order":1,
        "is_indexable":true,
        "renderable_order":2,
        "ser_order":3,
        "lis_order":4,
        "is_insertable":true,
        "is_renderable":true,
        "is_searchable":true,
        "is_listable":true,
        "is_association":true,
        "is_detail_link":true,
        "association_field":"parent",
        "min_occurences":1,
        "max_occurences":2,
        "group_by":"group",
        "extra_map":{"configuration":"{'source':'mydictionary'}"},
        "enumeration_list":["first","second"],
        "subfields":[{
          "name":"nested",
          "html_render":"TEXT",
          "html_class":"   ",
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
          "max_occurences":1,
          "subfields":[{
            "name":"deep",
            "html_render":"TEXT",
            "html_class":"custom",
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
        }],
        "valid_depends_on":"valid",
        "valid_depends_on_uids":"validUids",
        "value_depends_on":"value",
        "value_depends_on_uids":"valueUids",
        "hidden_depends_on":"hidden",
        "hidden_depends_on_uids":"hiddenUids",
        "mandatory_depends_on":"mandatory",
        "mandatory_depends_on_uids":"mandatoryUids",
        "enabled_depends_on":"enabled",
        "enabled_depends_on_uids":"enabledUids",
        "error_message":"Error",
        "help_message":"Help"
      }]
      """);

    FieldBean root = result.getFieldBeans().getFirst();
    TestUtils.checkNotNullFields(result);
    TestUtils.checkNotNullFields(root);
    assertEquals(RenderType.DYNAMIC_SELECT, root.getHtmlRender());
    assertEquals("center", root.getHtmlClass());
    assertEquals("{'source':'mydictionary'}", root.getExtraAttr().get("configuration"));
    assertEquals("center", root.getSubfields().getFirst().getHtmlClass());
    assertEquals("custom", root.getSubfields().getFirst().getSubfields().getFirst().getHtmlClass());
  }

  @Test
  void mapShouldRejectInvalidResponseBodies() {
    assertThrows(IllegalArgumentException.class, () -> mapper.map(null));
    assertThrows(IllegalArgumentException.class, () -> mapper.map("{}"));
    assertThrows(IllegalArgumentException.class, () -> mapper.map("{"));
    assertThrows(IllegalArgumentException.class, () -> mapper.map("[\"not-a-field\"]"));
    assertThrows(IllegalArgumentException.class, () -> mapper.map("[{\"name\":\"field\"}]"));
  }

  @Test
  void mapShouldAcceptCamelCaseBooleanPropertiesFromMyDictionary() {
    MyPayForm result = mapper.map("""
      [{
        "name":"sys_send_mysearch",
        "required":false,
        "html_render":"NONE",
        "default_value":"true",
        "ins_order":0,
        "isIndexable":false,
        "renderable_order":0,
        "ser_order":0,
        "lis_order":0,
        "isInsertable":false,
        "isRenderable":false,
        "isSearchable":false,
        "isListable":false,
        "isAssociation":false,
        "isDetailLink":false,
        "min_occurences":0,
        "max_occurences":0
      }]
      """);

    FieldBean fieldBean = result.getFieldBeans().getFirst();

    assertEquals(false, fieldBean.isIndexable());
    assertEquals(false, fieldBean.isInsertable());
    assertEquals(false, fieldBean.isRenderable());
    assertEquals("center", fieldBean.getHtmlClass());
  }
}
