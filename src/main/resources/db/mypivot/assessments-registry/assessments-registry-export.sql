SELECT uac.cod_tipo_dovuto,
       uac.cod_capitolo,
       uac.de_capitolo,
       uac.cod_ufficio,
       uac.de_ufficio,
       uac.cod_accertamento,
       uac.de_accertamento,
       uac.de_anno_esercizio,
       uac.flg_attivo,
       e.cod_ipa_ente
FROM mygov_anagrafica_uff_cap_acc uac
JOIN mygov_ente e ON uac.mygov_ente_id = e.mygov_ente_id
WHERE e.cod_ipa_ente = :ipaCode
  AND (:skipDebtPositionTypeOrgCodesFilter = TRUE OR uac.cod_tipo_dovuto IN (:debtPositionTypeOrgCodes))
  AND (
    (
      :skipDateFromFilter = FALSE
      AND uac.dt_ultima_modifica >= :dateFrom
    )
    OR (
      :skipDateFromFilter = TRUE
      AND (
        :skipLastExtractionDateFilter = TRUE
        OR uac.dt_ultima_modifica > :lastExtractionDate
      )
    )
  )
  AND (
    :skipDateToFilter = TRUE
    OR uac.dt_ultima_modifica <= :dateTo
  )
ORDER BY uac.cod_tipo_dovuto, uac.cod_capitolo, uac.cod_ufficio
LIMIT :limit
OFFSET COALESCE(:offset, 0);
