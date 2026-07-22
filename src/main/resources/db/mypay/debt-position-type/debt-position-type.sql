WITH ranked AS (
    SELECT
        etd.cod_tipo,
        etd.de_tipo,
        etd.macro_area,
        etd.tipo_servizio,
        etd.motivo_riscossione,
        etd.cod_tassonomico,
        e.cod_tipo_ente,
        ROW_NUMBER() OVER (
            PARTITION BY etd.cod_tipo
            ORDER BY etd.mygov_ente_tipo_dovuto_id ASC
        ) AS rn
    FROM mygov_ente_tipo_dovuto etd
    JOIN mygov_ente e
        ON e.mygov_ente_id = etd.mygov_ente_id
)
SELECT
    :brokerCf AS broker_cf,
    cod_tipo AS debt_position_type_code,
    de_tipo AS description,
    cod_tipo_ente AS org_type,
    macro_area AS macro_area,
    tipo_servizio AS service_type,
    motivo_riscossione AS collecting_reason,
    cod_tassonomico AS taxonomy_code,
    FALSE AS flag_anonymous_fiscal_code,
    FALSE AS flag_mandatory_due_date,
    FALSE AS flag_notify_io,
    :ioTemplateMessage AS io_template_message,
    :ioTemplateSubject AS io_template_subject
FROM ranked
WHERE rn = 1
ORDER BY cod_tipo;
