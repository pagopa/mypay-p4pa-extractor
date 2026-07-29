package it.gov.pagopa.mypay2pu.extractor.mapper.debtpositiontypeorg;

import it.gov.pagopa.mypay2pu.extractor.config.MyPayProperties;
import it.gov.pagopa.mypay2pu.extractor.dao.DebtPositionTypeOrgDao;
import it.gov.pagopa.mypay2pu.extractor.dto.export.PuDebtPositionTypeOrgDTO;
import it.gov.pagopa.mypay2pu.extractor.model.mp4.DebtPositionTypeOrg;
import it.gov.pagopa.mypay2pu.extractor.connector.mydictionary.MyDictionaryClient;
import org.springframework.stereotype.Component;

@Component
public class DebtPositionTypeOrgMapper {

  private final DebtPositionTypeOrgDao debtPositionTypeOrgDao;
  private final MyPayProperties myPayProperties;
  private final MyDictionaryClient myDictionaryClient;

  public DebtPositionTypeOrgMapper(DebtPositionTypeOrgDao debtPositionTypeOrgDao,
                                   MyPayProperties myPayProperties,
                                   MyDictionaryClient myDictionaryClient) {
    this.debtPositionTypeOrgDao = debtPositionTypeOrgDao;
    this.myPayProperties = myPayProperties;
    this.myDictionaryClient = myDictionaryClient;
  }

  public PuDebtPositionTypeOrgDTO map(DebtPositionTypeOrg debtPositionTypeOrg) {
    String strutturaPagamentoSpontaneo = myDictionaryClient.getSpontaneousFormStructure(debtPositionTypeOrg.spontaneousFormCode());
    return PuDebtPositionTypeOrgDTO.builder()
      .ipaCode(debtPositionTypeOrg.ipaCode())
      .balance(debtPositionTypeOrg.balance())
      .code(debtPositionTypeOrg.code())
      .description(debtPositionTypeOrg.description())
      .iban(debtPositionTypeOrg.iban())
      .postalIban(debtPositionTypeOrg.postalIban())
      .postalAccountCode(debtPositionTypeOrg.postalAccountCode())
      .holderPostalCc(debtPositionTypeOrg.holderPostalCc())
      .orgSector(debtPositionTypeOrg.orgSector())
      .spontaneousFormCode(debtPositionTypeOrg.spontaneousFormCode())
      .spontaneousFormStructure(strutturaPagamentoSpontaneo)
      .amountCents(debtPositionTypeOrg.amountCents())
      .externalPaymentUrl(debtPositionTypeOrg.externalPaymentUrl())
      .flagAnonymousFiscalCode(debtPositionTypeOrg.flagAnonymousFiscalCode())
      .flagMandatoryDueDate(debtPositionTypeOrg.flagMandatoryDueDate())
      .flagSpontaneous(debtPositionTypeOrg.flagSpontaneous())
      .flagNotifyIo(debtPositionTypeOrg.flagNotifyIo())
      .flagNotifyIoBkp(debtPositionTypeOrg.flagNotifyIoBkp())
      .ioTemplateMessage(transcodeTemplateTags(myPayProperties.ioTemplateMessage()))
      .flagActive(debtPositionTypeOrg.flagActive())
      .flagNotifyOutcomePush(debtPositionTypeOrg.flagNotifyOutcomePush())
      .notifyOutcomePushOrgSilServiceCode(debtPositionTypeOrg.notifyOutcomePushOrgSilServiceCode())
      .flagAmountActualization(debtPositionTypeOrg.flagAmountActualization())
      .amountActualizationOrgSilServiceCode(debtPositionTypeOrg.amountActualizationOrgSilServiceCode())
      .flagExternal(debtPositionTypeOrgDao.isExternal(debtPositionTypeOrg.ipaCode(), debtPositionTypeOrg.code()))
      .serviceCode(debtPositionTypeOrg.serviceCode())
      .ioTemplateSubject(transcodeTemplateTags(myPayProperties.ioTemplateSubject()))
      .build();
  }

  private String transcodeTemplateTags(String template) {
    if (template == null) {
      return null;
    }
    return template
      .replace("{desc_pagamento}", "%posizioneDebitoria_descrizione%")
      .replace("{anag_pagatore}", "%debitore_nomeCompleto%")
      .replace("{importo}", "%importoTotale%")
      .replace("{data_scadenza}", "%dataScadenza%");
  }
}
