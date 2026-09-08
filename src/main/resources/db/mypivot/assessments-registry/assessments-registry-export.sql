SELECT uac.cod_tipo_dovuto AS debtPositionTypeOrgCode,
       uac.cod_capitolo AS sectionCode,
       uac.de_capitolo AS sectionDescription,
       uac.cod_ufficio AS officeCode,
       uac.de_ufficio AS officeDescription,
       uac.cod_accertamento AS assessmentCode,
       uac.de_accertamento AS assessmentDescription,
       uac.de_anno_esercizio AS operatingYear,
       uac.flg_attivo AS flagActive,
       e.cod_ipa_ente AS organizationIpaCode
FROM mygov_anagrafica_uff_cap_acc uac
JOIN mygov_ente e ON uac.mygov_ente_id = e.mygov_ente_id
WHERE e.cod_ipa_ente = :ipaCode
  AND (:skipDebtPositionTypeOrgCodesFilter = TRUE OR uac.cod_tipo_dovuto IN (:debtPositionTypeOrgCodes))
  AND (:skipDateFromFilter = TRUE OR uac.dt_ultima_modifica >= :dateFrom)
  AND (
    :skipDateToFilter = TRUE
    OR uac.dt_ultima_modifica <= :dateTo
  )
ORDER BY uac.cod_tipo_dovuto, uac.cod_capitolo, uac.cod_ufficio
LIMIT :limit
OFFSET COALESCE(:offset, 0);
