SELECT EXISTS (
    SELECT 1
    FROM mygov_ente
    WHERE cod_ipa_ente = :codIpaEnte
      AND flg_tesoreria = TRUE
);
