SELECT rs.de_nome_file_scaricato
FROM mygov_flusso_rend_spc rs
WHERE rs.cod_ipa_ente = :ipaCode
  AND rs.cod_stato = 'OK'
  AND (:skipDateFromFilter = TRUE OR rs.dt_ultima_modifica >= :dateFrom)
  AND (
    :skipDateToFilter = TRUE
    OR rs.dt_ultima_modifica <= :dateTo
  )
  AND (
    :skipLogicalKeyFilter = TRUE
    OR rs.cod_identificativo_flusso = :logicalKey
  )
ORDER BY rs.dt_creazione
LIMIT :limit
OFFSET COALESCE(:offset, 0)
