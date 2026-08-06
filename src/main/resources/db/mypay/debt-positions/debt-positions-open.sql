SELECT
    d.gpd_iupd AS iupd,
    NULL AS descrizione_posizione_debitoria,
    CURRENT_DATE AS data_validita,
    FALSE AS coobbligato,
    NULL AS data_notifica,
    1 AS indice_opzione_pagamento,
    'SINGLE_INSTALLMENT' AS tipo_opzione_pagamento,
    'Pagamento Singolo Avviso' AS descrizione_opzione_pagamento,
    d.cod_iud AS iud,
    d.cod_iuv AS cod_iuv,
    d.cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco AS tipo_identificativo_univoco,
    d.cod_rp_sogg_pag_id_univ_pag_codice_id_univoco AS codice_identificativo_univoco,
    d.de_rp_sogg_pag_anagrafica_pagatore AS anagrafica_pagatore,
    d.de_rp_sogg_pag_indirizzo_pagatore AS indirizzo_pagatore,
    d.de_rp_sogg_pag_civico_pagatore AS civico_pagatore,
    d.cod_rp_sogg_pag_cap_pagatore AS cap_pagatore,
    d.de_rp_sogg_pag_localita_pagatore AS localita_pagatore,
    d.de_rp_sogg_pag_provincia_pagatore AS provincia_pagatore,
    d.cod_rp_sogg_pag_nazione_pagatore AS nazione_pagatore,
    d.de_rp_sogg_pag_email_pagatore AS mail_pagatore,
    d.dt_rp_dati_vers_data_esecuzione_pagamento AS data_esecuzione_pagamento,
    d.num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento AS importo_dovuto,
    d.cod_tipo_dovuto AS tipo_dovuto,
    d.de_rp_dati_vers_dati_sing_vers_causale_versamento AS causale_versamento,
    d.de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione AS dati_specifici_riscossione,
    d.flg_genera_iuv AS flg_genera_iuv,
    d.bilancio AS bilancio,
    FALSE AS draft,
    EXISTS (
        SELECT 1
        FROM mygov_dovuto_multibeneficiario mbx
        WHERE mbx.mygov_dovuto_id = d.mygov_dovuto_id
    ) AS flag_multi_beneficiario,
    mb.codice_fiscale_ente AS codice_fiscale_ente1,
    mb.de_rp_ente_benef_denominazione_beneficiario AS denominazione_ente1,
    mb.cod_rp_dati_vers_dati_sing_vers_iban_accredito AS iban_accredito_ente1,
    mb.de_rp_dati_vers_dati_sing_vers_causale_versamento AS causale_versamento_ente1,
    mb.num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento AS importo_versamento_ente1,
    mb.de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione AS codice_tassonomia_ente1,
    d.dt_creazione AS dt_creazione,
    d.dt_ultima_modifica AS dt_ultima_modifica
FROM mygov_dovuto d
JOIN mygov_flusso f
    ON d.mygov_flusso_id = f.mygov_flusso_id
JOIN mygov_ente e
    ON f.mygov_ente_id = e.mygov_ente_id
LEFT JOIN mygov_dovuto_multibeneficiario mb
    ON mb.mygov_dovuto_id = d.mygov_dovuto_id
WHERE e.cod_ipa_ente = :codIpaEnte
  AND (:skipGpdIupdFilter = TRUE OR d.gpd_iupd = :gpdIupd)
  AND (:skipCodIudFilter = TRUE OR d.cod_iud = :codIud)
  AND (:skipUpdatedFromFilter = TRUE OR d.dt_ultima_modifica >= :updatedFrom)
  AND (:skipUpdatedToExclusiveFilter = TRUE OR d.dt_ultima_modifica < :updatedToExclusive)
  AND (:debtPositionTypeOrgCodesEmpty = TRUE OR d.cod_tipo_dovuto IN (:debtPositionTypeOrgCodes))
  AND (d.flg_iuv_volatile IS NULL OR d.flg_iuv_volatile = FALSE)
ORDER BY d.dt_ultima_modifica, d.mygov_dovuto_id
LIMIT :limit
OFFSET COALESCE(:offset, 0)
