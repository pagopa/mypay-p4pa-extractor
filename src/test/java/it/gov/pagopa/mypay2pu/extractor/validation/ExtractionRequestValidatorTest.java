package it.gov.pagopa.mypay2pu.extractor.validation;

import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionFilters;
import it.gov.pagopa.mypay2pu.extractor.dto.generated.ExtractionRequest;
import it.gov.pagopa.mypay2pu.extractor.exception.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExtractionRequestValidatorTest {

  @Mock
  private ExtractionRequest requestMock;

  @Mock
  private ExtractionFilters filtersMock;

  @InjectMocks
  private ExtractionRequestValidator validator;

  @AfterEach
  void tearDown() {
    verifyNoMoreInteractions(requestMock, filtersMock);
  }

  @Test
  void givenNullRequestWhenValidateThenNoExceptionThrown() {
    assertDoesNotThrow(() -> validator.validate(null));
  }

  @ParameterizedTest
  @MethodSource("provideValidateShouldNotThrowCases")
  void givenValidFilterWhenValidateThenNoExceptionThrown(OffsetDateTime dateFrom, OffsetDateTime dateTo) {
    if (dateFrom == null && dateTo == null) {
      when(requestMock.getFilters()).thenReturn(null);
    } else {
      when(requestMock.getFilters()).thenReturn(filtersMock);
      when(filtersMock.getDateFrom()).thenReturn(dateFrom);
      when(filtersMock.getDateTo()).thenReturn(dateTo);
    }

    assertDoesNotThrow(() -> validator.validate(requestMock));
  }

  private static Stream<Arguments> provideValidateShouldNotThrowCases() {
    OffsetDateTime date1 = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, OffsetDateTime.now().getOffset());
    OffsetDateTime date2 = OffsetDateTime.of(2026, 1, 2, 0, 0, 0, 0, OffsetDateTime.now().getOffset());

    return Stream.of(
      Arguments.of(null, null),
      Arguments.of(null, date1),
      Arguments.of(date1, null),
      Arguments.of(date1, date1),
      Arguments.of(date1, date2)
    );
  }

  @Test
  void givenRequestWithDateFromAfterDateToWhenValidateThenThrowBadRequestException() {
    OffsetDateTime dateFrom = OffsetDateTime.of(2026, 1, 2, 0, 0, 0, 0, OffsetDateTime.now().getOffset());
    OffsetDateTime dateTo = OffsetDateTime.of(2026, 1, 1, 0, 0, 0, 0, OffsetDateTime.now().getOffset());
    when(requestMock.getFilters()).thenReturn(filtersMock);
    when(filtersMock.getDateFrom()).thenReturn(dateFrom);
    when(filtersMock.getDateTo()).thenReturn(dateTo);

    BadRequestException exception = assertThrows(BadRequestException.class, () -> validator.validate(requestMock));

    assertEquals("INVALID_EXTRACTION_FILTERS", exception.getCode());
    assertEquals("filters.dateFrom must be before or equal to filters.dateTo", exception.getMessage());
  }

  @ParameterizedTest
  @NullAndEmptySource
  @ValueSource(strings = {"not-a-valid-uuid"})
  void givenInvalidExtractionIdWhenValidateExtractionIdThenThrowBadRequestException(String extractionId) {
    BadRequestException exception = assertThrows(BadRequestException.class, () -> validator.validateExtractionId(extractionId));

    assertEquals("INVALID_EXTRACTION_ID", exception.getCode());
    assertEquals("extractionId must be a valid UUID", exception.getMessage());
  }

  @ParameterizedTest
  @CsvSource({
    "550e8400-e29b-41d4-a716-446655440000",
    "550e8400-E29B-41d4-A716-446655440000"
  })
  void givenValidExtractionIdWhenValidateExtractionIdThenNoExceptionThrown(String extractionId) {
    assertDoesNotThrow(() -> validator.validateExtractionId(extractionId));
  }
}
