SELECT
    e.cod_ipa_ente AS ipa_code,
    es.nome_applicativo AS application_name,
    'PAID_NOTIFICATION_OUTCOME' AS service_type,
    es.de_url_inoltro_esito_pagamento_push AS service_url,
    TRUE AS flag_legacy,
    es.cod_service_account_jwt_uscita_secret_key_id AS legacy_jwt_kid,
    es.de_service_account_jwt_uscita_client_mail AS legacy_jwt_subject,
    es.cod_service_account_jwt_uscita_client_id AS legacy_jwt_issuer,
    'HS512' AS legacy_jwt_algorithm,
    es.cod_service_account_jwt_uscita_secret_key AS legacy_jwt_signing_key,
    NULL AS legacy_basic_auth_url,
    NULL AS legacy_basic_user,
    NULL AS legacy_basic_psw
FROM mygov_ente_sil es
JOIN mygov_ente e
    ON e.mygov_ente_id = es.mygov_ente_id
WHERE es.nome_applicativo IS NOT NULL
  AND (:codIpaEnte IS NULL OR e.cod_ipa_ente = :codIpaEnte)
ORDER BY e.cod_ipa_ente, application_name
LIMIT :limit
OFFSET COALESCE(:offset, 0)
