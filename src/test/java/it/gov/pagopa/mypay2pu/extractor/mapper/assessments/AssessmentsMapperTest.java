package it.gov.pagopa.mypay2pu.extractor.mapper.assessments;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuAssessmentsDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Assessments;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssessmentsMapperTest {

  private final AssessmentsMapper mapper = new AssessmentsMapper();

  @Test
  void whenMapThenReturnPuAssessmentsDTO() {
    Assessments assessments = TestUtils.getPodamFactory().manufacturePojo(Assessments.class);

    PuAssessmentsDTO result = mapper.map(assessments);

    assertEquals(assessments.assessmentName(), result.getAssessmentName());
    assertEquals(assessments.organizationIpaCode(), result.getOrganizationIpaCode());
    assertEquals(assessments.debtPositionTypeOrgCode(), result.getDebtPositionTypeOrgCode());
    assertEquals(assessments.iuv(), result.getIuv());
    assertEquals(assessments.iud(), result.getIud());
    assertEquals(assessments.officeCode(), result.getOfficeCode());
    assertEquals(assessments.officeDescription(), result.getOfficeDescription());
    assertEquals(assessments.sectionCode(), result.getSectionCode());
    assertEquals(assessments.sectionDescription(), result.getSectionDescription());
    assertEquals(assessments.assessmentCode(), result.getAssessmentCode());
    assertEquals(assessments.assessmentDescription(), result.getAssessmentDescription());
    assertEquals(assessments.amountCents(), result.getAmountCents());
    assertEquals(assessments.amountSubmitted(), result.getAmountSubmitted());

    TestUtils.reflectionEqualsByName(result, assessments);
    TestUtils.checkNotNullFields(result);

  }
}
