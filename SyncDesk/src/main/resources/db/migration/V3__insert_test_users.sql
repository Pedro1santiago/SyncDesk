-- =============================================================================
-- V3: Test users (one per role) — NOT FOR PRODUCTION
-- Password for all users: syncdesk123
-- =============================================================================

INSERT INTO users (id, department_id, username, email, password, role, created_at, updated_at) VALUES
(
    gen_random_uuid(),
    NULL,
    'super.admin',
    'super.admin@syncdesk.dev',
    '$2b$10$OCG9TEvEkt02dnGrCLfsnef51n3dxok6cVp.hu5mububVysth7XQq',
    'SUPER_ADMIN',
    NOW(), NOW()
),
(
    gen_random_uuid(),
    (SELECT id FROM departments WHERE name = 'TI'),
    'admin.ti',
    'admin.ti@syncdesk.dev',
    '$2b$10$LJ44.6AgM.ERdt8jIoLro.Oyyoqc5hYS9/TenXfe2PtXnp/VUVByG',
    'ADMIN',
    NOW(), NOW()
),
(
    gen_random_uuid(),
    (SELECT id FROM departments WHERE name = 'TI'),
    'agent.ti',
    'agent.ti@syncdesk.dev',
    '$2b$10$ngrEiJkqF6OGLFdd7mb9ZeO6eKFa9tGbZardSnj9ZDxd/G.Ty..Ya',
    'AGENT',
    NOW(), NOW()
),
(
    gen_random_uuid(),
    (SELECT id FROM departments WHERE name = 'TI'),
    'user.ti',
    'user.ti@syncdesk.dev',
    '$2b$10$BkZOy/rYuDwFg1R1SAO9FuNhI7.tEUi8JAN4dy2ikUVjAPznkBTYy',
    'USER',
    NOW(), NOW()
);
