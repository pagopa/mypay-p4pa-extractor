package it.gov.pagopa.mypay2pu.extractor.service.export.organization;

import it.gov.pagopa.mypay2pu.extractor.config.ExtractorExportProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.ExportFileStatusDao;
import it.gov.pagopa.mypay2pu.extractor.dao.OrganizationDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuOrganizationDTO;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.mapper.organization.OrganizationMapper;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.Organization;
import it.gov.pagopa.mypay2pu.extractor.service.export.ExportBatchCoordinator;
import it.gov.pagopa.mypay2pu.extractor.service.export.ExportExecutionContext;
import it.gov.pagopa.mypay2pu.extractor.service.export.ExportFilePartWriter;
import it.gov.pagopa.mypay2pu.extractor.service.export.ExportProcessingService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Supplier;

@Component
public class OrganizationProcessingService extends ExportProcessingService<Organization, PuOrganizationDTO> {
  private final ExtractorExportProperties extractorExportProperties;
  private final OrganizationDao organizationDao;
  private final OrganizationMapper organizationMapper;

  public OrganizationProcessingService(
    OrganizationDao organizationDao,
    OrganizationMapper organizationMapper,
    ExtractorExportProperties extractorExportProperties,
    ExportFileStatusDao exportFileStatusDao,
    ExportBatchCoordinator exportBatchCoordinator,
    ExportFilePartWriter exportFilePartWriter
  ) {
    super(
      extractorExportProperties,
      exportBatchCoordinator,
      exportFilePartWriter,
      exportFileStatusDao
    );
    this.extractorExportProperties = extractorExportProperties;
    this.organizationDao = organizationDao;
    this.organizationMapper = organizationMapper;
  }

  @Override
  protected List<Organization> retrieveData(ExtractionRequest request) {
    return organizationDao.findByFilters(request.getIpaCode(), request.getFilters());
  }

  @Override
  protected Supplier<List<Organization>> retrieveDataSupplier(
    ExtractionRequest request,
    ExportExecutionContext executionContext
  ) {
    int pageSize = executionContext.pageSize();
    return createPagedSupplier(
      pageSize,
      offset -> organizationDao.findByFilters(request.getIpaCode(), request.getFilters(), pageSize, offset)
    );
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
  protected long getAvgRowSize() {
    return extractorExportProperties.avgRowSizeOrganizations();
  }

  @Override
  protected PuOrganizationDTO toExportableEntity(Organization organization) {
    return organizationMapper.map(organization);
  }
}
