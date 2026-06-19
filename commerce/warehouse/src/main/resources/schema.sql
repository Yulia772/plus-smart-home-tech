CREATE SCHEMA IF NOT EXISTS warehouse;

CREATE TABLE IF NOT EXISTS warehouse.products (
    product_id UUID PRIMARY KEY,
    quantity BIGINT NOT NULL CHECK (quantity >= 0),
    fragile BOOLEAN NOT NULL,
    width DOUBLE PRECISION NOT NULL CHECK (width > 0),
    height DOUBLE PRECISION NOT NULL CHECK (height > 0),
    depth DOUBLE PRECISION NOT NULL CHECK (depth > 0),
    weight DOUBLE PRECISION NOT NULL CHECK (weight > 0)
);
