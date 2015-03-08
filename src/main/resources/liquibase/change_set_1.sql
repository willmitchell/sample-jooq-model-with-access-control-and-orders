-- NOTE: The purpose of this model is to teach some ideas about how to deal with relations across many
-- tables when using JOOQ, not to explore what goes into a "user" table.  Throughout the model, the
-- overriding goal is to provide a minimal structure for each table; just enough to support self-descriptive
-- queries.

CREATE TABLE "user" (
  id    SERIAL PRIMARY KEY                     NOT NULL,
  login VARCHAR(64)                            NOT NULL UNIQUE

  -- no password?  correct.  see above paragraph.
);

CREATE TABLE account (
  id                SERIAL PRIMARY KEY                     NOT NULL,
  name              VARCHAR(128)                           NOT NULL,
  parent_account_id BIGINT REFERENCES account              NULL, -- feature not fully explored in tests yet
  owner_id          BIGINT REFERENCES "user"               NOT NULL
);

CREATE TABLE asset (
  id         SERIAL PRIMARY KEY             NOT NULL,
  account_id BIGINT REFERENCES account      NOT NULL,
  address    VARCHAR(255)                   NOT NULL
);

CREATE TABLE product (
  id    SERIAL PRIMARY KEY                           NOT NULL,
  name  VARCHAR(128)                                 NOT NULL,
  price MONEY                                        NOT NULL
);

CREATE TABLE "order" (
  id      SERIAL PRIMARY KEY                     NOT NULL,
  user_id BIGINT REFERENCES "user"               NOT NULL,
  total   MONEY DEFAULT 0                        NOT NULL
);

CREATE TABLE order_item (
  id         SERIAL PRIMARY KEY                                   NOT NULL,
  order_id   BIGINT REFERENCES "order"                            NOT NULL,
  product_id BIGINT REFERENCES product                            NOT NULL,
  quantity   INT                                                  NOT NULL,
  amount     MONEY DEFAULT 0                                      NOT NULL
);

CREATE TABLE "group" (
  id         SERIAL PRIMARY KEY        NOT NULL,
  name       VARCHAR(128)              NOT NULL,
  account_id BIGINT REFERENCES account NOT NULL
);

CREATE TABLE group_user (
  id       SERIAL PRIMARY KEY               NOT NULL,
  group_id BIGINT REFERENCES "group"        NOT NULL,
  user_id  BIGINT REFERENCES "user"         NOT NULL
);

CREATE TABLE "role" (
  id   SERIAL PRIMARY KEY                           NOT NULL,
  name VARCHAR(128)                                 NOT NULL
);

CREATE TABLE group_role (
  id       SERIAL PRIMARY KEY                           NOT NULL,
  group_id BIGINT REFERENCES "group"                    NOT NULL,
  role_id  BIGINT REFERENCES "role"                     NOT NULL
);
