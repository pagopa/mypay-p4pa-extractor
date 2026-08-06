package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileTypeNotSupportedException;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtractionValidationFacadeTest {

  @Mock
  private ExtractionRequestValidator extractionRequestValidatorMock;
  @Mock
  private CsvLogicalKeyValidator csvLogicalKeyValidatorMock;
  @Mock
  private PairedLogicalKeyValidator pairedLogicalKeyValidatorMock;

  @InjectMocks
  private ExtractionValidationFacade validationFacade;

  @ParameterizedTest
  @EnumSource(MigrationFileType.class)
  void whenValidateThenRouteByFileType(MigrationFileType fileType) {
    ExtractionRequest request = request(fileType);

    switch (fileType) {
      case ORGANIZATIONS, ORG_SIL_SERVICES -> {
        doNothing().when(extractionRequestValidatorMock).validate(request);
        assertDoesNotThrow(() -> validationFacade.validate(request));
        verifyNoMoreInteractions(extractionRequestValidatorMock);
      }
      case DEBT_POSITIONS_TYPE, DEBT_POSITIONS_TYPE_ORG, DEBT_POSITIONS -> {
        doNothing().when(csvLogicalKeyValidatorMock).validate(request);
        assertDoesNotThrow(() -> validationFacade.validate(request));
        verifyNoMoreInteractions(csvLogicalKeyValidatorMock);
      }
      case DEBT_POSITIONS_TYPE_ORG_OPERATORS -> {
        doNothing().when(pairedLogicalKeyValidatorMock).validate(request);
        assertDoesNotThrow(() -> validationFacade.validate(request));
        verifyNoMoreInteractions(pairedLogicalKeyValidatorMock);
      }
      default -> assertThrows(ExportFileTypeNotSupportedException.class,
        () -> validationFacade.validate(request));
    }
  }

  private ExtractionRequest request(MigrationFileType fileType) {
    return new ExtractionRequest(List.of("IPA_CODE"), fileType);
  }
}
