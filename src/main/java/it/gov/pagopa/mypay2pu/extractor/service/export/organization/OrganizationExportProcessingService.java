package it.gov.pagopa.mypay2pu.extractor.service.export.organization;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.OrganizationDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuOrganizationDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.organization.OrganizationMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
import it.gov.pagopa.mypay2pu.extractor.service.FileArchiverService;
import it.gov.pagopa.mypay2pu.extractor.service.export.BaseExportProcessingService;
import it.gov.pagopa.mypay2pu.extractor.service.files.CsvService;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrganizationExportProcessingService extends BaseExportProcessingService<Organization, PuOrganizationDTO> {

  private final OrganizationDao organizationDao;
  private final OrganizationMapper organizationMapper;

  public OrganizationExportProcessingService(
    OrganizationDao organizationDao,
    OrganizationMapper organizationMapper,
    CsvService csvService,
    FileArchiverService fileArchiverService,
    Validator validator,
    ExtractorExportProperties exportProperties
  ) {
    super(csvService, fileArchiverService, validator, exportProperties);
    this.organizationDao = organizationDao;
    this.organizationMapper = organizationMapper;
  }

  @Override
  protected MigrationFileType getMigrationFileType() {
    return MigrationFileType.ORGANIZATIONS;
  }

  @Override
  protected Class<PuOrganizationDTO> getDtoClass() {
    return PuOrganizationDTO.class;
  }

  @Override
  protected String getZipVersion() {
    return PuOrganizationDTO.VERSION;
  }

  @Override
  protected PuOrganizationDTO toExportableEntity(Organization organization) {
    return organizationMapper.map(organization);
  }

  @Override
  protected List<Organization> retrieveData(ExtractionRequest request, int pageSize, int offset) {
    return organizationDao.findByFilters(request.getIpaCode(), request.getFilters(), pageSize, offset);
  }
}
