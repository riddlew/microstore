CREATE TABLE inventory_item
(
	id             UUID         NOT NULL,
	sku            VARCHAR(12)  NOT NULL,
	name           VARCHAR(255) NOT NULL,
	description    VARCHAR(255),
	quantity       INTEGER      NOT NULL,
	price_in_cents INTEGER      NOT NULL,
	created_at     TIMESTAMP WITHOUT TIME ZONE,
	updated_at     TIMESTAMP WITHOUT TIME ZONE,
	CONSTRAINT pk_inventory_item PRIMARY KEY (id)
);

ALTER TABLE inventory_item
	ADD CONSTRAINT uc_inventory_item_sku UNIQUE (sku);

CREATE INDEX idx_sku ON inventory_item (sku);