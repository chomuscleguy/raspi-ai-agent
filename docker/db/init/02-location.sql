CREATE TABLE IF NOT EXISTS location_settings
(
    id
    BIGSERIAL
    PRIMARY
    KEY,
    latitude
    DOUBLE
    PRECISION
    NOT
    NULL,
    longitude
    DOUBLE
    PRECISION
    NOT
    NULL,
    city_label
    VARCHAR
(
    255
),
    source VARCHAR
(
    20
) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now
(
)
    );