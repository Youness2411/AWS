Add /certs/cert.crt in backend and frontend
Add .env

Database first scheme
users (id, username, email, role, password_hash, created_at)
theories (id, title, content, updated_at, created_at, author_id -> User.id)
comments (id, content, created_at, author_id -> User.id, theory_id -> Theory.id)
votes (id, type {UP,DOWN}, created_at, user_id -> User.id, theory_id -> Theory.id)