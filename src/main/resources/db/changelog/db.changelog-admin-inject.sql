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
        'LeAuXQVPc5zY7+vYHvi2uQ==',
        'Czechia',
        'admin@mpba.com',
        '$2a$12$15j07jN5lfXbRrewQGwW/OAW3yjUXDK.ubMrAm8y8Gowc9WPbiI0.', -- decrypt: admin
        'https://www.shareicon.net/data/2015/09/18/103157_man_512x512.png',
        'v75ySFMAY5Cjz4msGwFKVl046szL9Kk3wIypTmJOorA='); -- decrypt: +420111222333