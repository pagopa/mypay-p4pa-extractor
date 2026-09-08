package it.gov.pagopa.mypay2pu.extractor.mapper.assessmentsregistry;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuAssessmentsRegistryDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mpv4.AssessmentsRegistry;
import org.springframework.stereotype.Component;

@Component
public class AssessmentsRegistryMapper {

  public PuAssessmentsRegistryDTO map(AssessmentsRegistry model) {
    return PuAssessmentsRegistryDTO.builder()
        .organizationIpaCode(model.organizationIpaCode())
        .debtPositionTypeOrgCode(model.debtPositionTypeOrgCode())
        .sectionCode(model.sectionCode())
        .sectionDescription(model.sectionDescription())
        .officeCode(model.officeCode())
        .officeDescription(model.officeDescription())
        .assessmentCode(model.assessmentCode())
        .assessmentDescription(model.assessmentDescription())
        .operatingYear(model.operatingYear())
        .status(Boolean.parseBoolean(model.status())? "ACTIVE" : "INACTIVE")
        .build();
  }
}
