SELECT EXISTS (
    SELECT 1
    FROM mygov_ente_tipo_dovuto etd_pv
    JOIN mygov_ente e_pv
        ON e_pv.mygov_ente_id = etd_pv.mygov_ente_id
    WHERE etd_pv.esterno
      AND e_pv.cod_ipa_ente = :ipaCode
      AND etd_pv.cod_tipo = :codTipo
) AS esterno;
