-- changeset Unknown:1
-- description: Create user profile table
CREATE TABLE user_profile
(
    id                UUID PRIMARY KEY,
    role              VARCHAR(255),
    status            VARCHAR(255),
    name              VARCHAR(255)        NOT NULL,
    surname           VARCHAR(255)        NOT NULL,
    date_of_birth     VARCHAR(255)        NOT NULL,
    country_of_origin VARCHAR(255)        NOT NULL,
    email             VARCHAR(255) UNIQUE NOT NULL,
    password          VARCHAR(255)        NOT NULL,
    avatar            VARCHAR(255),
    phone_number      VARCHAR(255) UNIQUE NOT NULL
);

-- changeset Unknown:2
-- description: Create bank identity table
CREATE TABLE bank_identity
(
    id          UUID PRIMARY KEY,
    bank_name   VARCHAR(255) NOT NULL,
    bank_number VARCHAR(255) NOT NULL UNIQUE,
    swift       VARCHAR(255) NOT NULL UNIQUE,
    user_id     UUID         NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_profile (id)
);

-- changeset Unknown:3
-- description: Create bank account table
CREATE TABLE bank_account
(
    id             UUID PRIMARY KEY,
    currency       VARCHAR(255),
    balance        DECIMAL(19, 2) NOT NULL,
    account_number VARCHAR(255)   NOT NULL UNIQUE,
    iban           VARCHAR(255)   NOT NULL UNIQUE,
    bank_id        UUID           NOT NULL,
    FOREIGN KEY (bank_id) REFERENCES bank_identity (id)
);

-- changeset Unknown:4
-- description: Create card table
CREATE TABLE card
(
    id              UUID PRIMARY KEY,
    category        VARCHAR(255),
    type            VARCHAR(255),
    status          VARCHAR(255),
    card_number     VARCHAR(255) NOT NULL UNIQUE,
    cvv             VARCHAR(255) NOT NULL,
    pin             VARCHAR(255) NOT NULL,
    start_date      DATE         NOT NULL,
    expiration_date DATE         NOT NULL,
    account_id      UUID         NOT NULL,
    FOREIGN KEY (account_id) REFERENCES bank_account (id)
);

-- changeset Unknown:5
-- description: Create payment table
CREATE TABLE payment
(
    id                   UUID PRIMARY KEY,
    status               VARCHAR(255),
    type                 VARCHAR(255),
    currency             VARCHAR(255),
    amount               DECIMAL(19, 2) NOT NULL,
    sender_name          VARCHAR(255)   NOT NULL,
    recipient_name       VARCHAR(255)   NOT NULL,
    description          VARCHAR(255),
    date_time            DATE           NOT NULL,
    sender_account_id    UUID,
    recipient_account_id UUID,
    sender_card_id       UUID,
    FOREIGN KEY (sender_account_id) REFERENCES bank_account (id),
    FOREIGN KEY (recipient_account_id) REFERENCES bank_account (id),
    FOREIGN KEY (sender_card_id) REFERENCES card (id)
);

-- changeset Unknown:6
-- description: Create message table
CREATE TABLE message
(
    id          UUID PRIMARY KEY,
    content     VARCHAR(255) NOT NULL,
    timestamp   TIMESTAMP    NOT NULL,
    sender_id   UUID,
    receiver_id UUID,
    FOREIGN KEY (sender_id) REFERENCES user_profile (id),
    FOREIGN KEY (receiver_id) REFERENCES user_profile (id)
);

-- changeset Unknown:7
-- description: Create currency data table
CREATE TABLE currency_data
(
    id       UUID PRIMARY KEY,
    currency VARCHAR(255)   NOT NULL UNIQUE,
    rate     DECIMAL(19, 2) NOT NULL
);

-- changeset Unknown:8
-- description: Create access token table
CREATE TABLE access_token
(
    id              UUID PRIMARY KEY,
    token           VARCHAR(1024) NOT NULL UNIQUE,
    expiration_date TIMESTAMP     NOT NULL,
    user_id         UUID          NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_profile (id)
);

-- changeset Unknown:9
-- description: Create user_profile_currency_data table
CREATE TABLE user_profile_currency_data
(
    user_profiles_id UUID,
    currency_data_id UUID,
    PRIMARY KEY (user_profiles_id, currency_data_id),
    FOREIGN KEY (user_profiles_id) REFERENCES user_profile (id),
    FOREIGN KEY (currency_data_id) REFERENCES currency_data (id)
);