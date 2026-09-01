package it.gov.pagopa.mypay2pu.extractor.utils.mydictionary;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MyPayFormTest {

  @Test
  void shouldExposeFieldBeansProvidedByConstructor() {
    List<FieldBean> fieldBeans = List.of(FieldBean.builder().name("constructor-field").build());

    MyPayForm form = new MyPayForm(fieldBeans);

    assertEquals(fieldBeans, form.getFieldBeans());
  }

  @Test
  void shouldUpdateFieldBeansProvidedBySetter() {
    List<FieldBean> fieldBeans = List.of(FieldBean.builder().name("setter-field").build());
    MyPayForm form = new MyPayForm();

    form.setFieldBeans(fieldBeans);

    assertEquals(fieldBeans, form.getFieldBeans());
  }
}
