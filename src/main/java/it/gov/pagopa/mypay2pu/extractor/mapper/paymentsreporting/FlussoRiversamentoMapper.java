package it.gov.pagopa.mypay2pu.extractor.mapper.paymentsreporting;

import it.gov.pagopa.mypay2pu.extractor.model.mpv4.PaymentReporting;
import it.gov.digitpa.schemas._2011.pagamenti.CtDatiSingoliPagamenti;
import it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivoco;
import it.gov.digitpa.schemas._2011.pagamenti.CtIdentificativoUnivocoPersonaG;
import it.gov.digitpa.schemas._2011.pagamenti.CtIstitutoMittente;
import it.gov.digitpa.schemas._2011.pagamenti.CtIstitutoRicevente;
import it.gov.digitpa.schemas._2011.pagamenti.FlussoRiversamento;
import it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivoco;
import it.gov.digitpa.schemas._2011.pagamenti.StTipoIdentificativoUnivocoPersG;
import it.gov.pagopa.mypay2pu.extractor.exception.InvalidValueException;
import it.gov.pagopa.mypay2pu.extractor.utils.Utilities;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class FlussoRiversamentoMapper {

  private static final String INVALID_PAYMENTS_REPORTING_FLOW = "INVALID_PAYMENTS_REPORTING_FLOW";

  public FlussoRiversamento map(List<PaymentReporting> rows) {
    if (CollectionUtils.isEmpty(rows)) {
      throw invalidValue("Cannot map an empty payments reporting flow");
    }

    PaymentReporting firstRow = rows.getFirst();
    validateMandatoryHeaderFields(firstRow);
    validateRows(rows, firstRow);
    List<CtDatiSingoliPagamenti> details = rows.stream()
      .map(this::mapDetail)
      .toList();
    BigDecimal totalAmount = details.stream()
      .map(CtDatiSingoliPagamenti::getSingoloImportoPagato)
      .reduce(BigDecimal.ZERO, BigDecimal::add)
      .setScale(2, RoundingMode.UNNECESSARY);

    FlussoRiversamento flow = new FlussoRiversamento();
    flow.setVersioneOggetto(firstRow.versioneOggetto());
    flow.setIdentificativoFlusso(firstRow.codIdentificativoFlusso());
    flow.setDataOraFlusso(Utilities.toXMLGregorianCalendar(firstRow.dtDataOraFlusso()));
    flow.setIdentificativoUnivocoRegolamento(firstRow.codIdentificativoUnivocoRegolamento());
    flow.setDataRegolamento(Utilities.toXMLGregorianCalendar(firstRow.dtDataRegolamento()));
    flow.setIstitutoMittente(mapIstitutoMittente(firstRow));
    flow.setCodiceBicBancaDiRiversamento(firstRow.codiceBicBancaDiRiversamento());
    flow.setIstitutoRicevente(mapIstitutoRicevente(firstRow));
    flow.setNumeroTotalePagamenti(BigDecimal.valueOf(details.size()));
    flow.setImportoTotalePagamenti(totalAmount);
    flow.getDatiSingoliPagamentis().addAll(details);
    return flow;
  }

  private CtDatiSingoliPagamenti mapDetail(PaymentReporting row) {
    CtDatiSingoliPagamenti detail = new CtDatiSingoliPagamenti();
    detail.setIdentificativoUnivocoVersamento(row.codDatiSingPagamIdentificativoUnivocoVersamento());
    detail.setIdentificativoUnivocoRiscossione(row.codDatiSingPagamIdentificativoUnivocoRiscossione());
    detail.setIndiceDatiSingoloPagamento(row.indiceDatiSingoloPagamento());
    detail.setSingoloImportoPagato(normalizeAmount(row));
    detail.setCodiceEsitoSingoloPagamento(row.codDatiSingPagamCodiceEsitoSingoloPagamento());
    detail.setDataEsitoSingoloPagamento(Utilities.toXMLGregorianCalendar(row.dtDatiSingPagamDataEsitoSingoloPagamento()));
    return detail;
  }

  private CtIstitutoMittente mapIstitutoMittente(PaymentReporting row) {
    CtIdentificativoUnivoco identificativo = new CtIdentificativoUnivoco();
    identificativo.setTipoIdentificativoUnivoco(mapMittenteIdentifierType(row));
    identificativo.setCodiceIdentificativoUnivoco(row.codIstMittIdUnivMittCodiceIdentificativoUnivoco());
    CtIstitutoMittente istituto = new CtIstitutoMittente();
    istituto.setIdentificativoUnivocoMittente(identificativo);
    istituto.setDenominazioneMittente(row.deIstMittDenominazioneMittente());
    return istituto;
  }

  private CtIstitutoRicevente mapIstitutoRicevente(PaymentReporting row) {
    CtIdentificativoUnivocoPersonaG identificativo = new CtIdentificativoUnivocoPersonaG();
    identificativo.setTipoIdentificativoUnivoco(mapRiceventeIdentifierType(row));
    identificativo.setCodiceIdentificativoUnivoco(row.codIstRicevIdUnivRicevCodiceIdentificativoUnivoco());
    CtIstitutoRicevente istituto = new CtIstitutoRicevente();
    istituto.setIdentificativoUnivocoRicevente(identificativo);
    istituto.setDenominazioneRicevente(row.deIstRicevDenominazioneRicevente());
    return istituto;
  }

  private void validateMandatoryHeaderFields(PaymentReporting row) {
    if (!StringUtils.hasText(row.versioneOggetto())
      || !StringUtils.hasText(row.codIdentificativoFlusso())
      || row.dtDataOraFlusso() == null
      || !StringUtils.hasText(row.codIdentificativoUnivocoRegolamento())
      || row.dtDataRegolamento() == null
      || !StringUtils.hasText(row.codIstMittIdUnivMittTipoIdentificativoUnivoco())
      || !StringUtils.hasText(row.codIstMittIdUnivMittCodiceIdentificativoUnivoco())
      || !StringUtils.hasText(row.codIstRicevIdUnivRicevTipoIdentificativoUnivoco())
      || !StringUtils.hasText(row.codIstRicevIdUnivRicevCodiceIdentificativoUnivoco())) {
      throw invalidValue(
        "Missing mandatory header value for payments reporting flow " + row.codIdentificativoFlusso()
      );
    }
  }

  private void validateRows(List<PaymentReporting> rows, PaymentReporting firstRow) {
    Set<String> detailKeys = new HashSet<>();
    for (PaymentReporting row : rows) {
      if (!sameHeader(firstRow, row)) {
        throw invalidValue(
          "Inconsistent header values for payments reporting flow " + firstRow.codIdentificativoFlusso()
        );
      }
      validateMandatoryDetailFields(row);
      String detailKey = "%s|%s|%s".formatted(
        row.codDatiSingPagamIdentificativoUnivocoVersamento(),
        row.codDatiSingPagamIdentificativoUnivocoRiscossione(),
        row.indiceDatiSingoloPagamento()
      );
      if (!detailKeys.add(detailKey)) {
        throw invalidValue(
          "Duplicate payment detail for payments reporting flow " + firstRow.codIdentificativoFlusso()
        );
      }
    }
  }

  private boolean sameHeader(PaymentReporting expected, PaymentReporting actual) {
    return java.util.Objects.equals(expected.codIdentificativoFlusso(), actual.codIdentificativoFlusso())
      && java.util.Objects.equals(expected.versioneOggetto(), actual.versioneOggetto())
      && java.util.Objects.equals(expected.dtDataOraFlusso(), actual.dtDataOraFlusso())
      && java.util.Objects.equals(expected.codIdentificativoUnivocoRegolamento(), actual.codIdentificativoUnivocoRegolamento())
      && java.util.Objects.equals(expected.dtDataRegolamento(), actual.dtDataRegolamento())
      && java.util.Objects.equals(
        expected.codIstMittIdUnivMittTipoIdentificativoUnivoco(), actual.codIstMittIdUnivMittTipoIdentificativoUnivoco()
      )
      && java.util.Objects.equals(
        expected.codIstMittIdUnivMittCodiceIdentificativoUnivoco(), actual.codIstMittIdUnivMittCodiceIdentificativoUnivoco()
      )
      && java.util.Objects.equals(expected.deIstMittDenominazioneMittente(), actual.deIstMittDenominazioneMittente())
      && java.util.Objects.equals(expected.codiceBicBancaDiRiversamento(), actual.codiceBicBancaDiRiversamento())
      && java.util.Objects.equals(
        expected.codIstRicevIdUnivRicevTipoIdentificativoUnivoco(), actual.codIstRicevIdUnivRicevTipoIdentificativoUnivoco()
      )
      && java.util.Objects.equals(
        expected.codIstRicevIdUnivRicevCodiceIdentificativoUnivoco(), actual.codIstRicevIdUnivRicevCodiceIdentificativoUnivoco()
      )
      && java.util.Objects.equals(expected.deIstRicevDenominazioneRicevente(), actual.deIstRicevDenominazioneRicevente());
  }

  private void validateMandatoryDetailFields(PaymentReporting row) {
    if (!StringUtils.hasText(row.codDatiSingPagamIdentificativoUnivocoVersamento())
      || !StringUtils.hasText(row.codDatiSingPagamIdentificativoUnivocoRiscossione())
      || row.indiceDatiSingoloPagamento() == null
      || row.numDatiSingPagamSingoloImportoPagato() == null
      || !StringUtils.hasText(row.codDatiSingPagamCodiceEsitoSingoloPagamento())
      || row.dtDatiSingPagamDataEsitoSingoloPagamento() == null) {
      throw invalidValue(
        "Missing mandatory payment detail value for payments reporting flow " + row.codIdentificativoFlusso()
      );
    }
  }

  private BigDecimal normalizeAmount(PaymentReporting row) {
    try {
      return row.numDatiSingPagamSingoloImportoPagato().setScale(2, RoundingMode.UNNECESSARY);
    } catch (ArithmeticException exception) {
      throw invalidValue(
        "Invalid payment amount for payments reporting flow " + row.codIdentificativoFlusso(),
        exception
      );
    }
  }

  private StTipoIdentificativoUnivoco mapMittenteIdentifierType(PaymentReporting row) {
    try {
      return StTipoIdentificativoUnivoco.valueOf(row.codIstMittIdUnivMittTipoIdentificativoUnivoco());
    } catch (IllegalArgumentException exception) {
      throw invalidValue(
        "Invalid sender identifier type for payments reporting flow " + row.codIdentificativoFlusso(),
        exception
      );
    }
  }

  private StTipoIdentificativoUnivocoPersG mapRiceventeIdentifierType(PaymentReporting row) {
    try {
      return StTipoIdentificativoUnivocoPersG.valueOf(row.codIstRicevIdUnivRicevTipoIdentificativoUnivoco());
    } catch (IllegalArgumentException exception) {
      throw invalidValue(
        "Invalid receiver identifier type for payments reporting flow " + row.codIdentificativoFlusso(),
        exception
      );
    }
  }

  private IllegalArgumentException invalidValue(String message) {
    return invalidValue(message, null);
  }

  private IllegalArgumentException invalidValue(String message, Throwable cause) {
    InvalidValueException invalidValueException = new InvalidValueException(INVALID_PAYMENTS_REPORTING_FLOW, message);
    if (cause != null) {
      invalidValueException.initCause(cause);
    }
    return new IllegalArgumentException(message, invalidValueException);
  }
}
