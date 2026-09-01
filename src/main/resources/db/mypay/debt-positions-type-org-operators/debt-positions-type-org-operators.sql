SELECT
    e.cod_ipa_ente                  AS organizationIpaCode,
    o.cod_fed_user_id               AS operatorFiscalCode,
    etd.cod_tipo                    AS debtPositionsTypeOrgCode
FROM mygov_operatore_ente_tipo_dovuto oetd
JOIN mygov_operatore o
    ON oetd.mygov_operatore_id = o.mygov_operatore_id
JOIN mygov_ente_tipo_dovuto etd
    ON oetd.mygov_ente_tipo_dovuto_id = etd.mygov_ente_tipo_dovuto_id
JOIN mygov_ente e
    ON etd.mygov_ente_id = e.mygov_ente_id
WHERE e.cod_ipa_ente = :ipaCode
AND (:debtPositionTypeOrgCodesEmpty = TRUE OR etd.cod_tipo IN (:debtPositionTypeOrgCodes))
AND (:operatorFiscalCodesEmpty = TRUE OR o.cod_fed_user_id IN (:operatorFiscalCodes))
ORDER BY etd.cod_tipo, o.cod_fed_user_id
LIMIT :limit
OFFSET COALESCE(:offset, 0);
