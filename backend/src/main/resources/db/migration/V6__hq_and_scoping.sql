CREATE TABLE hqs (
    id   BIGSERIAL    PRIMARY KEY,
    code VARCHAR(20)  NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

ALTER TABLE centres
    ADD COLUMN hq_id BIGINT REFERENCES hqs(id);

ALTER TABLE users
    ADD COLUMN hq_id     BIGINT REFERENCES hqs(id),
    ADD COLUMN centre_id BIGINT REFERENCES centres(id);

CREATE TABLE officer_hq_assignments (
    officer_id BIGINT NOT NULL REFERENCES users(id),
    hq_id      BIGINT NOT NULL REFERENCES hqs(id),
    PRIMARY KEY (officer_id, hq_id)
);
