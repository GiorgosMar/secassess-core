-- 1. Organization
CREATE TABLE organization (
    id UUID PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    slug VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 2. Project
CREATE TABLE project (
    id BIGSERIAL PRIMARY KEY, -- BigInt Auto Increment [cite: 150]
    organization_id UUID NOT NULL REFERENCES organization(id),
    name VARCHAR(128) NOT NULL,
    code VARCHAR(12) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT uk_project_org_code UNIQUE (organization_id, code) -- Unique per org [cite: 153]
);

-- 3. Assessment Template
CREATE TABLE assessment_template (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL REFERENCES organization(id),
    title VARCHAR(160) NOT NULL,
    version VARCHAR(32),
    status VARCHAR(16) NOT NULL, -- DRAFT, PUBLISHED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- Index for sorting/filtering templates
CREATE INDEX idx_template_status_updated ON assessment_template(status, updated_at);

-- 4. Template Criterion
CREATE TABLE template_criterion (
    id UUID PRIMARY KEY,
    template_id UUID NOT NULL REFERENCES assessment_template(id),
    section VARCHAR(80) NOT NULL,
    text TEXT NOT NULL,
    severity VARCHAR(8) NOT NULL, -- LOW, MEDIUM, HIGH
    weight DECIMAL(3,2) NOT NULL CHECK (weight >= 0 AND weight <= 1)
);

-- 5. Assessment
CREATE TABLE assessment (
    id UUID PRIMARY KEY,
    project_id BIGINT NOT NULL REFERENCES project(id),
    title VARCHAR(160) NOT NULL,
    status VARCHAR(16) NOT NULL, -- OPEN, IN_PROGRESS, COMPLETED
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

-- 6. Assessment Item
CREATE TABLE assessment_item (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES assessment(id),
    criterion_ref UUID,
    section VARCHAR(80) NOT NULL,
    text TEXT NOT NULL,
    severity VARCHAR(8) NOT NULL,
    weight DECIMAL(3,2) NOT NULL,
    score INTEGER CHECK (score >= 0 AND score <= 100),
    notes VARCHAR(4000)
);

-- SEED DATA
-- Organization: Org-A
INSERT INTO organization (id, name, slug)
VALUES ('11111111-1111-1111-1111-111111111111', 'SecAssess Corp', 'secassess-corp');

-- Template: Published Security Standard
INSERT INTO assessment_template (id, organization_id, title, version, status)
VALUES ('22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 'ISO 27001 Basic', '1.0', 'PUBLISHED');

-- Criteria
INSERT INTO template_criterion (id, template_id, section, text, severity, weight) VALUES
('33333333-3333-3333-3333-333333333301', '22222222-2222-2222-2222-222222222222', 'Access Control', 'Are strong passwords enforced?', 'HIGH', 1.00),
('33333333-3333-3333-3333-333333333302', '22222222-2222-2222-2222-222222222222', 'Access Control', 'Is MFA enabled for all admins?', 'HIGH', 1.00),
('33333333-3333-3333-3333-333333333303', '22222222-2222-2222-2222-222222222222', 'Network Security', 'Are firewalls configured properly?', 'MEDIUM', 0.80),
('33333333-3333-3333-3333-333333333304', '22222222-2222-2222-2222-222222222222', 'Logging', 'Are logs retained for 90 days?', 'LOW', 0.50),
('33333333-3333-3333-3333-333333333305', '22222222-2222-2222-2222-222222222222', 'Encryption', 'Is data at rest encrypted?', 'HIGH', 1.00);