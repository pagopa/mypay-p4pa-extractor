-- Organization export query.
-- Feature flags are pre-aggregated once for the requested organization instead of
-- repeating two correlated EXISTS clauses in the main SELECT.
WITH organization_feature_flags AS (
    SELECT
        ef.cod_ipa_ente,
        BOOL_OR(ef.cod_funzionalita = 'NOTIFICA_AVVISI_IO') AS flag_notify_io,
        BOOL_OR(ef.cod_funzionalita = 'INOLTRO_ESITO_PAGAMENTO_PUSH') AS flag_notify_outcome_push
    FROM mygov_ente_funzionalita ef
    WHERE ef.cod_ipa_ente = :ipaCode
      AND ef.flg_attivo = TRUE
      AND ef.cod_funzionalita IN (
          'NOTIFICA_AVVISI_IO',
          'INOLTRO_ESITO_PAGAMENTO_PUSH'
      )
    GROUP BY ef.cod_ipa_ente
)
SELECT
    e.cod_ipa_ente AS ipa_code,
    e.codice_fiscale_ente AS org_fiscal_code,
    e.de_nome_ente AS org_name,
    e.cod_tipo_ente AS org_type_code,
    e.email_amministratore AS org_email,
    e.cod_rp_dati_vers_dati_sing_vers_iban_accredito AS iban,
    e.cod_rp_dati_vers_dati_sing_vers_iban_appoggio AS postal_iban,
    e.application_code AS segregation_code,
    e.cod_codice_interbancario_cbill AS cbill_inter_bank_code,
    e.de_logo_ente AS org_logo,
    s.cod_stato AS status,
    e.lingua_aggiuntiva AS additional_language,
    e.dt_avvio AS start_date,
    -- The standard MyPay4 codebase does not handle IO communication; therefore, it is not possible to extract its API keys. As a result, this tool will disable this feature for all exported organizations.
    -- However, to preserve the original configuration, an additional column will be created(flag_notify_io_bkp), which will be ignored during the Piattaforma Unitaria import.
    -- If your custom MyPay4 version allows exporting the API key via SQL, replace the NULL value with it here, and update the value of the flag_notify_io column using the expression provided for flag_notify_io_bkp.
    NULL AS io_api_key,
    FALSE AS flag_notify_io,
    COALESCE(organization_feature_flags.flag_notify_io, FALSE) AS flag_notify_io_bkp,
    COALESCE(organization_feature_flags.flag_notify_outcome_push, FALSE) AS flag_notify_outcome_push
FROM mygov_ente e
LEFT JOIN mygov_anagrafica_stato s
    ON s.mygov_anagrafica_stato_id = e.cd_stato_ente
LEFT JOIN organization_feature_flags
    ON organization_feature_flags.cod_ipa_ente = e.cod_ipa_ente
WHERE e.cod_ipa_ente = :ipaCode
  AND (:modifiedFrom IS NULL OR e.dt_ultima_modifica >= :modifiedFrom)
  AND (:modifiedToExclusive IS NULL OR e.dt_ultima_modifica < :modifiedToExclusive)
ORDER BY e.dt_ultima_modifica, e.mygov_ente_id
LIMIT :limit
OFFSET COALESCE(:offset, 0);
