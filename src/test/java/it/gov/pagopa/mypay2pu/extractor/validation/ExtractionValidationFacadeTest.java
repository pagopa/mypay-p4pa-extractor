package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileTypeNotSupportedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class ExtractionValidationFacadeTest {

  @Mock
  private ExtractionRequestValidator extractionRequestValidatorMock;
  @Mock
  private CsvLogicalKeyValidator csvLogicalKeyValidatorMock;
  @Mock
  private PairedLogicalKeyValidator pairedLogicalKeyValidatorMock;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(
      extractionRequestValidatorMock,
      csvLogicalKeyValidatorMock,
      pairedLogicalKeyValidatorMock
    );
  }

  @Test
  void givenBaseFileTypeWhenValidateThenInvokeBaseValidator() {
    ExtractionRequest request = request(MigrationFileType.ORGANIZATIONS);
    ExtractionValidationFacade facade = facade();

    facade.validate(request);

    verify(extractionRequestValidatorMock).validate(request);
  }

  @Test
  void givenCsvFileTypeWhenValidateThenInvokeCsvValidator() {
    ExtractionRequest request = request(MigrationFileType.DEBT_POSITIONS_TYPE);
    ExtractionValidationFacade facade = facade();

    facade.validate(request);

    verify(csvLogicalKeyValidatorMock).validate(request);
  }

  @Test
  void givenPairedFileTypeWhenValidateThenInvokePairedValidator() {
    ExtractionRequest request = request(MigrationFileType.DEBT_POSITIONS);
    ExtractionValidationFacade facade = facade();

    facade.validate(request);

    verify(pairedLogicalKeyValidatorMock).validate(request);
  }

  @Test
  void givenUnsupportedFileTypeWhenValidateThenThrowException() {
    ExtractionRequest request = request(MigrationFileType.DEBT_POSITIONS_PAID);
    ExtractionValidationFacade facade = facade();

    assertThrows(ExportFileTypeNotSupportedException.class, () -> facade.validate(request));
  }

  private ExtractionValidationFacade facade() {
    return new ExtractionValidationFacade(
      extractionRequestValidatorMock,
      csvLogicalKeyValidatorMock,
      pairedLogicalKeyValidatorMock
    );
  }

  private ExtractionRequest request(MigrationFileType fileType) {
    return new ExtractionRequest(List.of("IPA_CODE"), fileType);
  }
}
