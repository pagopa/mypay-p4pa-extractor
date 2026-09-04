package it.gov.pagopa.mypay2pu.extractor.mapper.assessments;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuAssessmentsDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Assessments;
import org.springframework.stereotype.Component;

@Component
public class AssessmentsMapper {

  public PuAssessmentsDTO map(Assessments assessments) {
    return PuAssessmentsDTO.builder()
      .assessmentName(assessments.assessmentName())
      .organizationIpaCode(assessments.organizationIpaCode())
      .debtPositionTypeOrgCode(assessments.debtPositionTypeOrgCode())
      .iuv(assessments.iuv())
      .iud(assessments.iud())
      .officeCode(assessments.officeCode())
      .officeDescription(assessments.officeDescription())
      .sectionCode(assessments.sectionCode())
      .sectionDescription(assessments.sectionDescription())
      .assessmentCode(assessments.assessmentCode())
      .assessmentDescription(assessments.assessmentDescription())
      .amountCents(assessments.amountCents())
      .amountSubmitted(assessments.amountSubmitted())
      .build();
  }
}
