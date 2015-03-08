CREATE TABLE "user" (
  id    SERIAL PRIMARY KEY                     NOT NULL,
  login VARCHAR(64)                            NOT NULL UNIQUE
);

CREATE TABLE account (
  id                SERIAL PRIMARY KEY                     NOT NULL,
  parent_account_id BIGINT REFERENCES account              NULL,
  account_owner     BIGINT REFERENCES "user"               NOT NULL
);

CREATE TABLE asset (
  id          SERIAL PRIMARY KEY             NOT NULL,
  address     VARCHAR(255)                   NOT NULL,
  location    POINT                          NOT NULL,
  account_id  BIGINT REFERENCES account      NOT NULL
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
  id       SERIAL PRIMARY KEY                                   NOT NULL,
  order_id BIGINT REFERENCES "order"                            NOT NULL,
  amount   MONEY DEFAULT 0                                      NOT NULL,
  quantity BIGINT REFERENCES product                            NOT NULL
);

CREATE TABLE "group" (
  id         SERIAL PRIMARY KEY        NOT NULL,
  account_id BIGINT REFERENCES account NOT NULL
);

CREATE TABLE group_membership (
  id       SERIAL PRIMARY KEY               NOT NULL,
  group_id BIGINT REFERENCES "group"        NOT NULL,
  user_id  BIGINT REFERENCES "user"         NOT NULL
);

CREATE TABLE "role" (
  id   SERIAL PRIMARY KEY                           NOT NULL,
  name VARCHAR(128)                                 NOT NULL
);

CREATE TABLE group_roles (
  id       SERIAL PRIMARY KEY                           NOT NULL,
  group_id BIGINT REFERENCES "group"                    NOT NULL,
  role_id  BIGINT REFERENCES "role"                     NOT NULL
);


ALTER TABLE "user" ADD COLUMN account_id INT UNIQUE NULL;
