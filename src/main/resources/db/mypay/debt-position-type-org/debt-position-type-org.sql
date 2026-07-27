SELECT
    e.cod_ipa_ente AS ipa_code,
    etd.de_bilancio_default AS balance,
    etd.cod_tipo AS code,
    etd.de_tipo AS description,
    etd.iban_accredito_psp AS iban,
    etd.iban_accredito_pi AS postal_iban,
    etd.cod_conto_corrente_postale AS postal_account_code,
    etd.de_intestatario_cc_postale AS holder_postal_cc,
    etd.de_settore_ente AS org_sector,
    CAST(ROUND(etd.importo * 100) AS BIGINT) AS amount_cents,
    etd.de_url_pagamento_dovuto AS external_payment_url,
    etd.flg_cf_anonimo::text AS flag_anonymous_fiscal_code,
    etd.flg_scadenza_obbligatoria::text AS flag_mandatory_due_date,
    etd.spontaneo::text AS flag_spontaneous,
    etd.flg_notifica_io::text AS flag_notify_io,
    etd.flg_attivo::text AS flag_active,
    etd.flg_notifica_esito_push::text AS flag_notify_outcome_push,
    (
        SELECT es.nome_applicativo
        FROM mygov_ente_sil es
        WHERE es.mygov_ente_id = etd.mygov_ente_id
          AND etd.flg_notifica_esito_push = TRUE
        ORDER BY es.nome_applicativo ASC
        LIMIT 1
    ) AS notify_outcome_push_org_sil_service_code,
    CASE
        WHEN etd.url_notifica_pnd IS NOT NULL
         AND etd.user_pnd IS NOT NULL
         AND etd.psw_pnd IS NOT NULL
        THEN 'true'
        ELSE 'false'
    END AS flag_amount_actualization,
    null AS amount_actualization_org_sil_service_id,
    etd.codice_servizio AS spontaneous_form_code
FROM mygov_ente_tipo_dovuto etd
JOIN mygov_ente e
    ON e.mygov_ente_id = etd.mygov_ente_id
WHERE e.cod_ipa_ente = :ipaCode
  AND (:logicalKeysEmpty = TRUE OR etd.cod_tipo IN (:logicalKeys))
ORDER BY etd.cod_tipo
LIMIT :limit
OFFSET COALESCE(:offset, 0);
