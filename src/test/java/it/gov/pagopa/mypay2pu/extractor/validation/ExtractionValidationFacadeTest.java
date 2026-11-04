package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.config.MyPivotProperties;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
  @Mock
  private MyPaySharedFolderValidator myPaySharedFolderValidatorMock;

  private ExtractionValidationFacade validationFacade;

  @BeforeEach
  void setUp() {
    validationFacade = new ExtractionValidationFacade(
      extractionRequestValidatorMock,
      valueLogicalKeyValidatorMock,
      pairedLogicalKeyValidatorMock,
      new MyPivotProperties(true),
      myPaySharedFolderValidatorMock
    );
  }

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(
      extractionRequestValidatorMock,
      valueLogicalKeyValidatorMock,
      pairedLogicalKeyValidatorMock,
      myPaySharedFolderValidatorMock
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
      case DEBT_POSITIONS_TYPE, DEBT_POSITIONS_TYPE_ORG, DEBT_POSITIONS, DEBT_POSITIONS_PAID, PAYMENT_NOTIFICATION,
        ASSESSMENTS, ASSESSMENTS_REGISTRY -> {
        validationFacade.validate(request);
        verify(valueLogicalKeyValidatorMock).validate(request);
      }
      case DEBT_POSITIONS_TYPE_ORG_OPERATORS, TREASURY_CSV_COMPLETE -> {
        validationFacade.validate(request);
        verify(pairedLogicalKeyValidatorMock).validate(request);
      }
      case PAYMENTS_REPORTING_MYPAY -> {
        validationFacade.validate(request);
        verify(myPaySharedFolderValidatorMock).validateMyPaySharedFolder();
        verify(extractionRequestValidatorMock).validate(request);
      }
      case PAYMENTS_REPORTING_MYPIVOT -> {
        validationFacade.validate(request);
        verify(extractionRequestValidatorMock).validate(request);
      }
      default -> assertThrows(ExportFileTypeNotSupportedException.class,
        () -> validationFacade.validate(request));
    }
  }

  @Test
  void givenNullRequestWhenValidateThenThrowException() {
    assertThrows(BadRequestException.class, () -> validationFacade.validate(null));
  }

  @ParameterizedTest
  @EnumSource(
    value = MigrationFileType.class,
    names = {
      "PAYMENT_NOTIFICATION",
      "PAYMENTS_REPORTING_MYPIVOT",
      "TREASURY_CSV_COMPLETE",
      "ASSESSMENTS",
      "ASSESSMENTS_REGISTRY"
    },
    mode = EnumSource.Mode.INCLUDE
  )
  void givenMyPivotDisabledWhenValidateThenThrowBadRequest(MigrationFileType fileType) {
    ExtractionValidationFacade disabledMyPivotValidationFacade = new ExtractionValidationFacade(
      extractionRequestValidatorMock,
      valueLogicalKeyValidatorMock,
      pairedLogicalKeyValidatorMock,
      new MyPivotProperties(false),
      myPaySharedFolderValidatorMock
    );
    ExtractionRequest request = request(fileType);

    BadRequestException exception = assertThrows(
      BadRequestException.class,
      () -> disabledMyPivotValidationFacade.validate(request)
    );

    assertEquals("MYPIVOT_DISABLED", exception.getCode());
    assertEquals("MyPivot extraction is disabled", exception.getMessage());
  }

  @Test
  void givenMyPaySharedFolderUnavailableWhenValidateThenThrowBadRequest() {
    BadRequestException expectedException = new BadRequestException(
      "MYPAY_SHARED_FOLDER_UNAVAILABLE",
      "MyPay shared folder is unavailable"
    );
    doThrow(expectedException).when(myPaySharedFolderValidatorMock).validateMyPaySharedFolder();
    ExtractionRequest request = request(MigrationFileType.PAYMENTS_REPORTING_MYPAY);

    BadRequestException exception = assertThrows(
      BadRequestException.class,
      () -> validationFacade.validate(request)
    );

    assertEquals("MYPAY_SHARED_FOLDER_UNAVAILABLE", exception.getCode());
    assertEquals(
      "MyPay shared folder is unavailable",
      exception.getMessage()
    );
    verify(myPaySharedFolderValidatorMock).validateMyPaySharedFolder();
  }

  private ExtractionRequest request(MigrationFileType fileType) {
    return new ExtractionRequest(List.of("IPA_CODE"), fileType);
  }
}
