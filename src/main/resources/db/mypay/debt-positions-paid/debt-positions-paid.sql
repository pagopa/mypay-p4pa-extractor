SELECT
    de.*,
    f.iuf,
    e.cod_ipa_ente,
    r.fiscal_code AS cod_fiscale_pa1,
    r.company_name AS de_nome_pa1,
    r.transfer_category_1 AS cod_tassonomico_dovuto_pa1
FROM mygov_dovuto_elaborato de
INNER JOIN mygov_flusso f
    ON de.mygov_flusso_id = f.mygov_flusso_id
INNER JOIN mygov_ente e
    ON f.mygov_ente_id = e.mygov_ente_id
INNER JOIN mygov_anagrafica_stato de_status
    ON de.mygov_anagrafica_stato_id = de_status.mygov_anagrafica_stato_id
INNER JOIN mygov_anagrafica_stato flow_status
    ON f.mygov_anagrafica_stato_id = flow_status.mygov_anagrafica_stato_id
LEFT JOIN mygov_receipt r
    ON de.mygov_dovuto_elaborato_id = r.mygov_dovuto_elaborato_id
WHERE e.cod_ipa_ente = :codIpaEnte
  AND de_status.cod_stato = 'COMPLETATO'
  AND de_status.de_tipo_stato = 'dovuto'
  AND flow_status.cod_stato = 'CARICATO'
  AND flow_status.de_tipo_stato = 'flusso'
  AND f.flg_attivo = TRUE
  AND de.flg_dovuto_attuale = TRUE
  AND de.num_e_dati_pag_dati_sing_pag_singolo_importo_pagato > 0
  AND de.cod_tipo_dovuto <> 'MARCA_BOLLO_DIGITALE'
  AND (:iudsEmpty = TRUE OR de.cod_iud IN (:iuds))
  AND (:iuvsEmpty = TRUE OR de.cod_rp_silinviarp_id_univoco_versamento IN (:iuvs))
  AND (:skipCreatedFromFilter = TRUE OR de.dt_creazione >= :createdFrom)
  AND (:skipCreatedToFilter = TRUE OR de.dt_creazione <= :createdTo)
ORDER BY de.dt_creazione, de.mygov_dovuto_elaborato_id
LIMIT :limit
OFFSET COALESCE(:offset, 0)
