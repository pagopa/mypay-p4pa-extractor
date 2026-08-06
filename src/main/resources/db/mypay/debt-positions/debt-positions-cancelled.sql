SELECT
    de.gpd_iupd AS iupd,
    NULL AS descrizione_posizione_debitoria,
    CURRENT_DATE AS data_validita,
    FALSE AS coobbligato,
    NULL AS data_notifica,
    1 AS indice_opzione_pagamento,
    'SINGLE_INSTALLMENT' AS tipo_opzione_pagamento,
    'Pagamento Singolo Avviso' AS descrizione_opzione_pagamento,
    de.cod_iud AS iud,
    de.cod_iuv AS cod_iuv,
    de.cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco AS tipo_identificativo_univoco,
    de.cod_rp_sogg_pag_id_univ_pag_codice_id_univoco AS codice_identificativo_univoco,
    de.de_rp_sogg_pag_anagrafica_pagatore AS anagrafica_pagatore,
    de.de_rp_sogg_pag_indirizzo_pagatore AS indirizzo_pagatore,
    de.de_rp_sogg_pag_civico_pagatore AS civico_pagatore,
    de.cod_rp_sogg_pag_cap_pagatore AS cap_pagatore,
    de.de_rp_sogg_pag_localita_pagatore AS localita_pagatore,
    de.de_rp_sogg_pag_provincia_pagatore AS provincia_pagatore,
    de.cod_rp_sogg_pag_nazione_pagatore AS nazione_pagatore,
    de.de_rp_sogg_pag_email_pagatore AS mail_pagatore,
    de.dt_rp_dati_vers_data_esecuzione_pagamento AS data_esecuzione_pagamento,
    de.num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento AS importo_dovuto,
    de.cod_tipo_dovuto AS tipo_dovuto,
    de.de_rp_dati_vers_dati_sing_vers_causale_versamento AS causale_versamento,
    de.de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione AS dati_specifici_riscossione,
    de.flg_genera_iuv AS flg_genera_iuv,
    de.bilancio AS bilancio,
    FALSE AS draft,
    FALSE AS flag_multi_beneficiario,
    NULL AS codice_fiscale_ente_1,
    NULL AS denominazione_ente_1,
    NULL AS iban_accredito_ente_1,
    NULL AS causale_versamento_ente_1,
    NULL AS importo_versamento_ente_1,
    NULL AS codice_tassonomia_ente_1,
    de.dt_creazione AS dt_creazione,
    NULL AS dt_ultima_modifica
FROM mygov_dovuto_elaborato de
JOIN mygov_flusso f
    ON de.mygov_flusso_id = f.mygov_flusso_id
JOIN mygov_ente e
    ON f.mygov_ente_id = e.mygov_ente_id
JOIN mygov_anagrafica_stato s
    ON s.mygov_anagrafica_stato_id = de.mygov_anagrafica_stato_id
WHERE e.cod_ipa_ente = :codIpaEnte
  AND s.cod_stato = 'ANNULLATO'
  AND (:gpdIupdsEmpty = TRUE OR de.gpd_iupd IN (:gpdIupds))
  AND (:iudsEmpty = TRUE OR de.cod_iud IN (:iuds))
  AND (:updatedFrom IS NULL OR de.dt_creazione >= :updatedFrom)
  AND (:updatedToExclusive IS NULL OR de.dt_creazione < :updatedToExclusive)
ORDER BY de.dt_creazione, de.mygov_dovuto_elaborato_id
LIMIT :limit
OFFSET COALESCE(:offset, 0)
