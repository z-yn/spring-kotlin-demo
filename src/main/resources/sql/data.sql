INSERT INTO users (username, email) VALUES ('test', 'user@test.com') ON CONFLICT DO NOTHING;
