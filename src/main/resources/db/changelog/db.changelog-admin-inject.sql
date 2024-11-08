-- changeset Unknown:10
-- description: Add admin user to user_profile table
INSERT INTO user_profile (id,
                          role,
                          status,
                          name,
                          surname,
                          date_of_birth,
                          country_of_origin,
                          email,
                          password,
                          avatar,
                          phone_number)
VALUES ('00000000-0000-0000-0000-000000000001',
        'ROLE_ADMIN',
        'STATUS_DEFAULT',
        'Admin',
        'Admin',
        'UDW9hFYgvJQ8JrPtm9KWaQ==', -- decrypt: 2001-01-01
        'Czechia',
        'admin@mpba.com',
        '$2a$12$6tP0EfemWAZfwkGeLPc68eXL9bXfSBC8zpyNbMbkG5yYAxs3afiCO', -- decrypt: admin (BCrypt)
        'https://www.shareicon.net/data/2015/09/18/103157_man_512x512.png',
        '5IUSHqA/9Ba/X9RWQJVSCQ=='); -- decrypt: +420111222333