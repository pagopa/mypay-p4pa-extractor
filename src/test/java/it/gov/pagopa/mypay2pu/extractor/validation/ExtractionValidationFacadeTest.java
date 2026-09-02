package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.MigrationFileType;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import it.gov.pagopa.mypay2pu.extractor.exception.ExportFileTypeNotSupportedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtractionValidationFacadeTest {

  @Mock
  private ExtractionRequestValidator extractionRequestValidatorMock;
  @Mock
  private ValueLogicalKeyValidator valueLogicalKeyValidatorMock;
  @Mock
  private PairedLogicalKeyValidator pairedLogicalKeyValidatorMock;

  private ExtractionValidationFacade validationFacade;

  @BeforeEach
  void setUp() {
    validationFacade = new ExtractionValidationFacade(
      extractionRequestValidatorMock,
      valueLogicalKeyValidatorMock,
      pairedLogicalKeyValidatorMock
    );
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(
      extractionRequestValidatorMock,
      valueLogicalKeyValidatorMock,
      pairedLogicalKeyValidatorMock
    );
  }

  @ParameterizedTest
  @EnumSource(MigrationFileType.class)
  void whenValidateThenRouteByFileType(MigrationFileType fileType) {
    ExtractionRequest request = request(fileType);
    // using verify to testing routing
    switch (fileType) {
      case ORGANIZATIONS, ORG_SIL_SERVICES -> {
        validationFacade.validate(request);
        verify(extractionRequestValidatorMock).validate(request);
      }
      case DEBT_POSITIONS_TYPE, DEBT_POSITIONS_TYPE_ORG, DEBT_POSITIONS -> {
        validationFacade.validate(request);
        verify(valueLogicalKeyValidatorMock).validate(request);
      }
      case DEBT_POSITIONS_TYPE_ORG_OPERATORS, PAYMENT_NOTIFICATION -> {
        validationFacade.validate(request);
        verify(pairedLogicalKeyValidatorMock).validate(request);
      }
      default -> assertThrows(ExportFileTypeNotSupportedException.class,
        () -> validationFacade.validate(request));
    }
  }

  @Test
  void givenNullRequestWhenValidateThenThrowException() {
    assertThrows(BadRequestException.class, () -> validationFacade.validate(null));
  }

  private ExtractionRequest request(MigrationFileType fileType) {
    return new ExtractionRequest(List.of("IPA_CODE"), fileType);
  }
}
