package it.gov.pagopa.mypay2pu.extractor.model.mp4;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;

public record OrgSilService(
  String ipaCode,
  String applicationName,
  String serviceType,
  String serviceUrl,
  Boolean flagLegacy,
  String legacyJwtKid,
  String legacyJwtSubject,
  String legacyJwtIssuer,
  String legacyJwtAlgorithm,
  String legacyJwtSigningKey,
  String legacyBasicAuthUrl,
  String legacyBasicUser,
  String legacyBasicPsw
) implements ExportModel {}
