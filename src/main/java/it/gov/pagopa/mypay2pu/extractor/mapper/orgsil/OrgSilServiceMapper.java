package it.gov.pagopa.mypay2pu.extractor.mapper.orgsil;

import it.gov.pagopa.mypay2pu.extractor.dto.export.PuOrgSilServiceDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.OrgSilService;
import org.springframework.stereotype.Component;

@Component
public class OrgSilServiceMapper {

  public PuOrgSilServiceDTO map(OrgSilService orgSilService) {
    return PuOrgSilServiceDTO.builder()
      .ipaCode(orgSilService.ipaCode())
      .applicationName(orgSilService.applicationName())
      .serviceType(orgSilService.serviceType())
      .serviceUrl(orgSilService.serviceUrl())
      .flagLegacy(Boolean.toString(orgSilService.flagLegacy()))
      .legacyJwtKid(orgSilService.legacyJwtKid())
      .legacyJwtSubject(orgSilService.legacyJwtSubject())
      .legacyJwtIssuer(orgSilService.legacyJwtIssuer())
      .legacyJwtAlgorithm(orgSilService.legacyJwtAlgorithm())
      .legacyJwtSigningKey(orgSilService.legacyJwtSigningKey())
      .legacyBasicAuthUrl(orgSilService.legacyBasicAuthUrl())
      .legacyBasicUser(orgSilService.legacyBasicUser())
      .legacyBasicPsw(orgSilService.legacyBasicPsw())
      .build();
  }
}
