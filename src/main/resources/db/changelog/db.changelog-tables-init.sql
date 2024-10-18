-- Changeset: 1
-- This changeset creates the user_profile table to store user information.
-- Fields include ID, role, status, name, surname, date of birth, country of origin,
-- email (unique), password, avatar URL, and phone number (unique).
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

-- Changeset: 2
-- This changeset creates the bank_identity table to store bank information related to users.
-- It includes fields for bank name, bank number (unique), SWIFT code (unique), and a foreign key to user_profile.
CREATE TABLE bank_identity
(
    id          UUID PRIMARY KEY,
    bank_name   VARCHAR(255) NOT NULL,
    bank_number VARCHAR(255) NOT NULL UNIQUE,
    swift       VARCHAR(255) NOT NULL UNIQUE,
    user_id     UUID         NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_profile (id)
);

-- Changeset: 3
-- This changeset creates the bank_account table to manage user bank accounts.
-- It includes fields for account currency, balance, account number (unique), IBAN (unique),
-- and a foreign key to bank_identity.
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

-- Changeset: 4
-- This changeset creates the card table to store information about user cards.
-- It includes fields for card category, type, status, card number (unique),
-- CVV, PIN, start date, expiration date, and a foreign key to bank_account.
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

-- Changeset: 5
-- This changeset creates the payment table to record payment transactions.
-- It includes fields for status, type, currency, amount, sender and recipient names,
-- description, date/time, and foreign keys to sender and recipient accounts and sender card.
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

-- Changeset: 6
-- This changeset creates the message table to manage user messages.
-- It includes fields for message content, timestamp, and foreign keys to sender and receiver user profiles.
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

-- Changeset: 7
-- This changeset creates the currency_data table to store currency information.
-- It includes fields for currency name (unique) and its exchange rate.
CREATE TABLE currency_data
(
    id       UUID PRIMARY KEY,
    currency VARCHAR(255)   NOT NULL UNIQUE,
    rate     DECIMAL(19, 2) NOT NULL
);

-- Changeset: 8
-- This changeset creates the access_token table to store generated JWT tokens.
-- It includes fields for token value (unique), expiration date, and a foreign key to user_profile.
CREATE TABLE access_token
(
    id              UUID PRIMARY KEY,
    token           VARCHAR(1024) NOT NULL UNIQUE,
    expiration_date TIMESTAMP     NOT NULL,
    user_id         UUID          NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_profile (id)
);

-- Changeset: 9
-- This changeset creates the user_profile_currency_data table to establish a many-to-many relationship
-- between user profiles and currencies, linking user profiles to their preferred currencies.
CREATE TABLE user_profile_currency_data
(
    user_profiles_id UUID,
    currency_data_id UUID,
    PRIMARY KEY (user_profiles_id, currency_data_id),
    FOREIGN KEY (user_profiles_id) REFERENCES user_profile (id),
    FOREIGN KEY (currency_data_id) REFERENCES currency_data (id)
);