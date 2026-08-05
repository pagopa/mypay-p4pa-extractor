package it.gov.pagopa.mypay2pu.extractor.utils.mydictionary;

import java.util.List;

public class MyPayForm {

  private List<FieldBean> fieldBeans;

  public MyPayForm() {
  }

  public MyPayForm(List<FieldBean> fieldBeans) {
    this.fieldBeans = fieldBeans;
  }

  public List<FieldBean> getFieldBeans() {
    return fieldBeans;
  }

  public void setFieldBeans(List<FieldBean> fieldBeans) {
    this.fieldBeans = fieldBeans;
  }
}
