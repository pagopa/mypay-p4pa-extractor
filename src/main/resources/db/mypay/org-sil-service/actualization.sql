SELECT
    e.cod_ipa_ente AS ipa_code,
    'ACTUALIZATION' AS application_name,
    'ACTUALIZATION' AS service_type,
    NULL AS service_url,
    TRUE AS flag_legacy,
    NULL AS legacy_jwt_kid,
    NULL AS legacy_jwt_subject,
    NULL AS legacy_jwt_issuer,
    NULL AS legacy_jwt_algorithm,
    NULL AS legacy_jwt_signing_key,
    etd.url_notifica_pnd AS legacy_basic_auth_url,
    etd.user_pnd AS legacy_basic_user,
    etd.psw_pnd AS legacy_basic_psw
FROM mygov_ente_tipo_dovuto etd
JOIN mygov_ente e
    ON e.mygov_ente_id = etd.mygov_ente_id
WHERE etd.url_notifica_pnd IS NOT NULL
  AND etd.user_pnd IS NOT NULL
  AND etd.psw_pnd IS NOT NULL
  AND (:codIpaEnte IS NULL OR e.cod_ipa_ente = :codIpaEnte)
ORDER BY e.cod_ipa_ente, application_name
LIMIT :limit
OFFSET COALESCE(:offset, 0)
