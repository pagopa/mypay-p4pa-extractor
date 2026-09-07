SELECT
    ft.de_anno_bolletta,
    ft.cod_bolletta,
    NULL AS cod_ente_bt,
    NULL AS cod_istat_ente,
    e.cod_ipa_ente,
    ft.cod_id_univoco_flusso,
    ft.cod_id_univoco_versamento,
    ft.cod_conto,
    ft.cod_id_dominio,
    ft.cod_tipo_movimento,
    ft.cod_causale,
    ft.de_causale,
    ft.num_ip_bolletta,
    ft.dt_bolletta,
    ft.dt_ricezione,
    ft.de_anno_documento,
    ft.cod_documento,
    ft.cod_bollo,
    ft.de_cognome,
    ft.de_nome,
    ft.de_via,
    ft.de_cap,
    ft.de_citta,
    ft.cod_codice_fiscale,
    ft.cod_partita_iva,
    ft.cod_abi,
    ft.cod_cab,
    ft.cod_iban,
    ft.cod_conto_anagrafica,
    ft.de_ae_provvisorio,
    ft.cod_provvisorio,
    ft.cod_tipo_conto,
    ft.cod_processo,
    ft.cod_pg_esecuzione,
    ft.cod_pg_trasferimento,
    ft.num_pg_processo,
    ft.dt_data_valuta_regione,
    ft.flg_regolarizzata,
    ft.dt_effettiva_sospeso,
    ft.codice_gestionale_provvisorio,
    ft.end_to_end_id
FROM mygov_flusso_tesoreria ft
JOIN mygov_ente e
    ON ft.mygov_ente_id = e.mygov_ente_id
WHERE e.cod_ipa_ente = :ipaCode
  AND (:annoBolletta IS NULL OR ft.de_anno_bolletta = :annoBolletta)
  AND (:codBolletta IS NULL OR ft.cod_bolletta = :codBolletta)
  AND (:updatedFrom IS NULL OR ft.dt_ultima_modifica >= :updatedFrom)
  AND (:updatedTo IS NULL OR ft.dt_ultima_modifica <= :updatedTo)
ORDER BY ft.de_anno_bolletta, ft.cod_bolletta
LIMIT :limit
OFFSET COALESCE(:offset, 0);
