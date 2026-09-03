SELECT
    a.de_nome_accertamento AS assessment_name,
    ad.cod_ipa_ente AS organization_ipa_code,
    ad.cod_tipo_dovuto AS debt_position_type_org_code,
    ad.cod_iuv AS iuv,
    ad.cod_iud AS iud,
    ad.cod_ufficio AS office_code,
    NULL AS office_description,
    ad.cod_capitolo AS section_code,
    NULL AS section_description,
    ad.cod_accertamento AS assessment_code,
    NULL AS assessment_description,
    CAST(ad.num_importo * 100 AS BIGINT) AS amount_cents,
    ad.flg_importo_inserito AS amount_submitted
FROM mygov_accertamento a
JOIN mygov_accertamento_dettaglio ad ON ad.mygov_accertamento_id = a.mygov_accertamento_id
WHERE ad.cod_ipa_ente = :codIpaEnte
  AND a.dt_ultima_modifica > :dataUltimaMigrazione
ORDER BY a.de_nome_accertamento, ad.cod_iuv;
