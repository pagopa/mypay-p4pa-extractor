package it.gov.pagopa.mypay2pu.extractor.service.export.orgsil;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.OrgSilServiceDao;
import it.gov.pagopa.mypay2pu.extractor.dao.OrganizationDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuOrgSilServiceDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.organization.OrganizationMapper;
import it.gov.pagopa.mypay2pu.extractor.mapper.orgsil.OrgSilServiceMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.OrgSilService;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.BaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

@Service
public class OrgSilServiceExportProcessingService extends BaseExportProcessingService<OrgSilService, PuOrgSilServiceDTO> {

  private final OrgSilServiceDao orgSilServiceDao;
  private final OrgSilServiceMapper orgSilServiceMapper;

  public OrgSilServiceExportProcessingService(
    OrgSilServiceDao orgSilServiceDao,
    OrgSilServiceMapper orgSilServiceMapper,
    CsvService csvService,
    FileArchiverService fileArchiverService,
    Validator validator,
    ExtractorExportProperties exportProperties
  ) {
    super(csvService, fileArchiverService, validator, exportProperties);
    this.orgSilServiceDao = orgSilServiceDao;
    this.orgSilServiceMapper = orgSilServiceMapper;
  }

  @Override
  protected MigrationFileType getMigrationFileType() {
    return MigrationFileType.ORG_SIL_SERVICES;
  }

  @Override
  protected Class<PuOrgSilServiceDTO> getDtoClass() {
    return PuOrgSilServiceDTO.class;
  }

  @Override
  protected String getZipVersion() {
    return PuOrgSilServiceDTO.VERSION;
  }

  @Override
  protected PuOrgSilServiceDTO toExportableEntity(OrgSilService orgSilService) {
    return orgSilServiceMapper.map(orgSilService);
  }

  @Override
  protected List<OrgSilService> retrieveData(ExtractionRequest request, int pageSize, int offset) {
    return List.of();
  }
}
