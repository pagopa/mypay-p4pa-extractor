SELECT
    e.cod_ipa_ente AS ente_ipa_code,
    etd.de_bilancio_default AS bilancio_default,
    etd.cod_tipo AS codice,
    etd.de_tipo AS descrizione,
    etd.iban_accredito_psp AS cod_iban,
    etd.iban_accredito_pi AS iban_postale,
    etd.cod_conto_corrente_postale AS codice_conto_postale,
    etd.de_intestatario_cc_postale AS intestatario_conto_postale,
    etd.de_settore_ente AS settore_organizzazione,
    CAST(ROUND(etd.importo * 100) AS BIGINT) AS importo_centesimi,
    etd.de_url_pagamento_dovuto AS url_pagamento_esterno,
    etd.flg_cf_anonimo::text AS codice_fiscale_anonimo,
    etd.flg_scadenza_obbligatoria::text AS scadenza_obbligatoria,
    etd.spontaneo::text AS pagamento_spontaneo,
    etd.flg_notifica_io::text AS notifica_io,
    etd.flg_attivo::text AS attivo,
    etd.flg_notifica_esito_push::text AS notifica_esito_push,
    (
        SELECT es.nome_applicativo
        FROM mygov_ente_sil es
        WHERE es.mygov_ente_id = etd.mygov_ente_id
          AND etd.flg_notifica_esito_push = TRUE
        ORDER BY es.nome_applicativo ASC
        LIMIT 1
    ) AS cod_serv_notifica_esito_push,
    CASE
        WHEN etd.url_notifica_pnd IS NOT NULL
         AND etd.user_pnd IS NOT NULL
         AND etd.psw_pnd IS NOT NULL
        THEN 'true'
        ELSE 'false'
    END AS attualizzazione_importo,
    etd.codice_servizio AS codice_servizio,
    etd.cod_xsd_causale AS cod_xsd_causale,
    etd.url_notifica_pnd AS url_notifica_pnd,
    etd.user_pnd AS user_pnd,
    etd.psw_pnd AS psw_pnd
FROM mygov_ente_tipo_dovuto etd
JOIN mygov_ente e
    ON e.mygov_ente_id = etd.mygov_ente_id
WHERE e.cod_ipa_ente = :ipaCode
  AND (:logicalKeysEmpty = TRUE OR etd.cod_tipo IN (:logicalKeys))
ORDER BY etd.cod_tipo
LIMIT :limit
OFFSET COALESCE(:offset, 0);
