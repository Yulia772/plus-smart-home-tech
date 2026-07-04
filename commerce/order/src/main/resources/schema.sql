CREATE SCHEMA IF NOT EXISTS orders;

CREATE TABLE IF NOT EXISTS orders.addresses (
    address_id UUID PRIMARY KEY,
    country VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    street VARCHAR(255) NOT NULL,
    house VARCHAR(255) NOT NULL,
    flat VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS orders.orders (
    order_id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    shopping_cart_id UUID,
    payment_id UUID,
    delivery_id UUID,
    state VARCHAR(50) NOT NULL,
    delivery_weight DOUBLE PRECISION,
    delivery_volume DOUBLE PRECISION,
    fragile BOOLEAN,
    total_price NUMERIC,
    delivery_price NUMERIC,
    product_price NUMERIC,
    to_address_id UUID NOT NULL,
    from_address_id UUID,
    CONSTRAINT fk_orders_to_address
        FOREIGN KEY (to_address_id)
        REFERENCES orders.addresses(address_id),
    CONSTRAINT fk_orders_from_address
        FOREIGN KEY (from_address_id)
        REFERENCES orders.addresses(address_id)
);

CREATE TABLE IF NOT EXISTS orders.order_products (
    order_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity BIGINT NOT NULL CHECK (quantity > 0),
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_order_products_to_orders
        FOREIGN KEY (order_id)
        REFERENCES orders.orders(order_id)
        ON DELETE CASCADE
);

