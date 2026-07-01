CREATE SCHEMA IF NOT EXISTS delivery;

CREATE TABLE IF NOT EXISTS delivery.deliveries (
    delivery_id UUID PRIMARY KEY,
    delivery_state VARCHAR(50) NOT NULL,
    order_id UUID NOT NULL UNIQUE,
    from_country VARCHAR(255) NOT NULL,
    from_city VARCHAR(255) NOT NULL,
    from_street VARCHAR(255) NOT NULL,
    from_house VARCHAR(255) NOT NULL,
    from_flat VARCHAR(255),
    to_country VARCHAR(255) NOT NULL,
    to_city VARCHAR(255) NOT NULL,
    to_street VARCHAR(255) NOT NULL,
    to_house VARCHAR(255) NOT NULL,
    to_flat VARCHAR(255),
    delivery_weight DOUBLE PRECISION,
    delivery_volume DOUBLE PRECISION,
    fragile BOOLEAN
);