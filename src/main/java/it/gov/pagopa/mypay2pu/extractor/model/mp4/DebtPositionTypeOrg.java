package it.gov.pagopa.mypay2pu.extractor.model.mp4;

import it.gov.pagopa.mypay2pu.extractor.model.ExportModel;

public record DebtPositionTypeOrg(
  String enteIpaCode,
  String bilancioDefault,
  String codice,
  String descrizione,
  String codIban,
  String ibanPostale,
  String codiceContoPostale,
  String intestatarioContoPostale,
  String settoreOrganizzazione,
  Long importoCentesimi,
  String urlPagamentoEsterno,
  String codiceFiscaleAnonimo,
  String scadenzaObbligatoria,
  String pagamentoSpontaneo,
  String notificaIo,
  String attivo,
  String notificaEsitoPush,
  String codServNotificaEsitoPush,
  String attualizzazioneImporto,
  String codiceServizio,
  String codXsdCausale,
  String urlNotificaPnd,
  String userPnd,
  String pswPnd
) implements ExportModel {
}
