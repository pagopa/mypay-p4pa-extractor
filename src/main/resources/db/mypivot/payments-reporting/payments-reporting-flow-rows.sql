SELECT
  fr.cod_identificativo_flusso,
  fr.versione_oggetto,
  fr.dt_data_ora_flusso,
  fr.cod_identificativo_univoco_regolamento,
  fr.dt_data_regolamento,
  fr.cod_ist_mitt_id_univ_mitt_tipo_identificativo_univoco,
  fr.cod_ist_mitt_id_univ_mitt_codice_identificativo_univoco,
  fr.de_ist_mitt_denominazione_mittente,
  fr.codice_bic_banca_di_riversamento,
  fr.cod_ist_ricev_id_univ_ricev_tipo_identificativo_univoco,
  fr.cod_ist_ricev_id_univ_ricev_codice_identificativo_univoco,
  fr.de_ist_ricev_denominazione_ricevente,
  fr.cod_dati_sing_pagam_identificativo_univoco_versamento,
  fr.cod_dati_sing_pagam_identificativo_univoco_riscossione,
  fr.indice_dati_singolo_pagamento,
  fr.num_dati_sing_pagam_singolo_importo_pagato,
  fr.cod_dati_sing_pagam_codice_esito_singolo_pagamento,
  fr.dt_dati_sing_pagam_data_esito_singolo_pagamento
FROM public.mygov_flusso_rendicontazione fr
JOIN public.mygov_ente e
  ON e.mygov_ente_id = fr.mygov_ente_id
WHERE e.cod_ipa_ente = :ipaCode
  AND fr.cod_identificativo_flusso IN (:flowIdentifiers)
  AND (:skipCreatedFromFilter = TRUE OR fr.dt_creazione >= :createdFrom)
  AND (:skipCreatedToFilter = TRUE OR fr.dt_creazione <= :createdTo)
ORDER BY
  fr.indice_dati_singolo_pagamento,
  fr.cod_dati_sing_pagam_identificativo_univoco_versamento,
  fr.cod_dati_sing_pagam_identificativo_univoco_riscossione
