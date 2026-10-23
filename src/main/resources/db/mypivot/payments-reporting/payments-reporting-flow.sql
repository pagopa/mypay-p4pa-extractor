SELECT DISTINCT fr.cod_identificativo_flusso
FROM public.mygov_flusso_rendicontazione fr
JOIN public.mygov_ente e
  ON e.mygov_ente_id = fr.mygov_ente_id
WHERE e.cod_ipa_ente = :ipaCode
  AND (:skipCreatedFromFilter = TRUE OR fr.dt_creazione >= :createdFrom)
  AND (:skipCreatedToFilter = TRUE OR fr.dt_creazione <= :createdTo)
  AND (
    :skipFlowIdentifiersFilter = TRUE
    OR fr.cod_identificativo_flusso IN (:flowIdentifiers)
  )
ORDER BY fr.cod_identificativo_flusso
LIMIT :limit
OFFSET COALESCE(:offset, 0)
