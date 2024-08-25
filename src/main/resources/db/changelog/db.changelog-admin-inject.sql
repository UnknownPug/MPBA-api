-- If an admin user does not exist, create one
DO
$$
    BEGIN
        IF NOT EXISTS (SELECT 1
                       FROM user_profile
                       WHERE email = 'admin@mpba.com') THEN
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
                    '2000-01-01',
                    'Czechia',
                    'admin@mpba.com',
                    '$2a$12$15j07jN5lfXbRrewQGwW/OAW3yjUXDK.ubMrAm8y8Gowc9WPbiI0.', -- decrypt: admin
                    'https://www.shareicon.net/data/2015/09/18/103157_man_512x512.png',
                    '+420111222333');
        END IF;
    END
$$;

DO
$$
    BEGIN
        INSERT INTO access_token (id, user_id, bank_id, token, expiration_date)
        VALUES ('00000000-0000-0000-0000-000000000001',
                '00000000-0000-0000-0000-000000000001',
                NULL, -- Set bank_id to NULL if not applicable
                'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjAwMDAwMDAwLT' ||
                'AwMDAtMDAwMC0wMDAwLTAwMDAwMDAwMDAwMSIsInJvbGUiOiJST0xFX0FETUlOIiwic3RhdH' ||
                'VzIjoiU1RBVFVTX0RFRkFVTFQiLCJuYW1lIjoiQWRtaW4iLCJzdXJuYW1lIjoiQWRtaW4iLC' ||
                'JkYXRlX29mX2JpcnRoIjoiMjAwMC0wMS0wMSIsImNvdW50cnlfb2Zfb3JpZ2luIjoiQ3plY2' ||
                'hpYSIsImVtYWlsIjoiYWRtaW5AbXBiYS5jb20iLCJwYXNzd29yZCI6IiQyYSQxMiQxNWowN2' ||
                'pONWxmWGJScmV3UUd3Vy9PQVczeWpVWERLLnViTXJBbTh5OEdvd2M5V1BiaUkwLiIsImF2YX' ||
                'RhciI6Imh0dHBzOi8vd3d3LnNoYXJlaWNvbi5uZXQvZGF0YS8yMDE1LzA5LzE4LzEwMzE1N1' ||
                '9tYW5fNTEyeDUxMi5wbmciLCJwaG9uZV9udW1iZXIiOiIrNDIwMTExMjIyMzMzIn0.g9AJRx' ||
                '8GAT_Jqp1VFt3rT7fB2kqJTGLQVG4L_q0YAVY',
                NULL); -- Set expiration_date to NULL if not applicable
    END;
$$;