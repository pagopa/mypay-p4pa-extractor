package it.gov.pagopa.mypay2pu.extractor.mapper.assessmentsregistry;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuAssessmentsRegistryDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.AssessmentsRegistry;
import it.gov.pagopa.mypay2pu.extractor.utils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AssessmentsRegistryMapperTest {
  private final AssessmentsRegistryMapper mapper = new AssessmentsRegistryMapper();

  @Test
  void whenMapThenReturnAssessmentsRegistryDTO() {
    AssessmentsRegistry model = TestUtils.getPodamFactory().manufacturePojo(AssessmentsRegistry.class);

    PuAssessmentsRegistryDTO dto = mapper.map(model);

    assertEquals(model.organizationIpaCode(), dto.getOrganizationIpaCode());
    assertEquals(model.debtPositionTypeOrgCode(), dto.getDebtPositionTypeOrgCode());
    assertEquals(model.sectionCode(), dto.getSectionCode());
    assertEquals(model.sectionDescription(), dto.getSectionDescription());
    assertEquals(model.officeCode(), dto.getOfficeCode());
    assertEquals(model.officeDescription(), dto.getOfficeDescription());
    assertEquals(model.assessmentCode(), dto.getAssessmentCode());
    assertEquals(model.assessmentDescription(), dto.getAssessmentDescription());
    assertEquals(model.operatingYear(), dto.getOperatingYear());
    assertEquals(model.status(), dto.getStatus());

    TestUtils.reflectionEqualsByName(model, dto);
    TestUtils.checkNotNullFields(dto);
  }
}
