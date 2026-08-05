SELECT
    fi.cod_iud AS iud,
    fi.cod_rp_silinviarp_id_univoco_versamento AS iuv,
    fi.cod_rp_sogg_pag_id_univ_pag_tipo_id_univoco AS tipo_identificativo_univoco,
    fi.cod_rp_sogg_pag_id_univ_pag_codice_id_univoco AS codice_identificativo_univoco,
    fi.de_rp_sogg_pag_anagrafica_pagatore AS anagrafica_pagatore,
    fi.de_rp_sogg_pag_indirizzo_pagatore AS indirizzo_pagatore,
    fi.de_rp_sogg_pag_civico_pagatore AS civico_pagatore,
    fi.cod_rp_sogg_pag_cap_pagatore AS cap_pagatore,
    fi.de_rp_sogg_pag_localita_pagatore AS localita_pagatore,
    fi.de_rp_sogg_pag_provincia_pagatore AS provincia_pagatore,
    fi.cod_rp_sogg_pag_nazione_pagatore AS nazione_pagatore,
    fi.de_rp_sogg_pag_email_pagatore AS email_pagatore,
    fi.dt_rp_dati_vers_data_esecuzione_pagamento AS data_esecuzione_pagamento,
    fi.num_rp_dati_vers_dati_sing_vers_importo_singolo_versamento AS importo_dovuto_pagato,
    fi.num_rp_dati_vers_dati_sing_vers_commissione_carico_pa AS commissione_carico_pa,
    fi.cod_tipo_dovuto AS tipo_dovuto,
    fi.cod_rp_dati_vers_tipo_versamento AS tipo_versamento,
    fi.de_rp_dati_vers_dati_sing_vers_causale_versamento AS causale_versamento,
    fi.de_rp_dati_vers_dati_sing_vers_dati_specifici_riscossione AS dati_specifici_riscossione,
    fi.bilancio AS bilancio,
    e.cod_ipa_ente AS ipa_code,
    fi.dt_creazione AS dt_creazione,
    fi.dt_ultima_modifica AS dt_ultima_modifica
FROM mygov_flusso_import fi
JOIN mygov_ente e
    ON fi.mygov_ente_id = e.mygov_ente_id
WHERE e.cod_ipa_ente = :ipaCode
  AND (:iud IS NULL OR fi.cod_iud = :iud)
  AND (:iuv IS NULL OR fi.cod_rp_silinviarp_id_univoco_versamento = :iuv)
  AND (:skipCreatedFromFilter = TRUE OR fi.dt_creazione >= :createdFrom)
  AND (:skipCreatedToFilter = TRUE OR fi.dt_creazione <= :createdTo)
ORDER BY fi.dt_creazione
LIMIT :limit
OFFSET COALESCE(:offset, 0);
