-- Initial schema for tenant service
-- This migration creates all necessary tables for tenant management

-- Create subscription_plans table
CREATE TABLE subscription_plans (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    max_users INTEGER DEFAULT 10,
    max_storage_gb INTEGER DEFAULT 1,
    price_monthly DECIMAL(10,2) DEFAULT 0.00,
    price_yearly DECIMAL(10,2) DEFAULT 0.00,
    features JSONB,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create tenants table
CREATE TABLE tenants (
    id VARCHAR(50) PRIMARY KEY,
    display_name VARCHAR(100) NOT NULL,
    realm_name VARCHAR(50) NOT NULL UNIQUE,
    domain VARCHAR(100),
    admin_email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    country VARCHAR(3) DEFAULT 'IN',
    state VARCHAR(100),
    pincode VARCHAR(10),
    locale VARCHAR(10) DEFAULT 'en-GB',
    currency VARCHAR(3) DEFAULT 'INR',
    admin_temp_password VARCHAR(255),
    subscription_plan_id BIGINT REFERENCES subscription_plans(id),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_date_effective TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_date_effective TIMESTAMP
);

-- Create tenant_configs table
CREATE TABLE tenant_configs (
    id VARCHAR(50) PRIMARY KEY REFERENCES tenants(id) ON DELETE CASCADE,
    server_url VARCHAR(255) NOT NULL,
    api_client_id VARCHAR(255) NOT NULL,
    ui_client_id VARCHAR(255) NOT NULL,
    api_client_secret VARCHAR(255),
    ui_client_secret VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create users table
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    country VARCHAR(3),
    state VARCHAR(100),
    pincode VARCHAR(10),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    tenant_id VARCHAR(50) NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    start_date_effective TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_date_effective TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create audit_logs table for audit trail
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL, -- CREATE, UPDATE, DELETE
    old_values JSONB,
    new_values JSONB,
    changed_by VARCHAR(255),
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address INET,
    user_agent TEXT
);

-- Create mobile_verifications table for OTP verification
CREATE TABLE mobile_verifications (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    tenant_id VARCHAR(50) REFERENCES tenants(id) ON DELETE CASCADE,
    user_id VARCHAR(50) REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, VERIFIED, EXPIRED, FAILED
    attempts INTEGER DEFAULT 0,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create email_notifications table for tracking sent emails
CREATE TABLE email_notifications (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) REFERENCES tenants(id) ON DELETE CASCADE,
    user_id VARCHAR(50) REFERENCES users(id) ON DELETE CASCADE,
    email_type VARCHAR(50) NOT NULL, -- SIGNUP_STARTED, SIGNUP_COMPLETED, WELCOME, etc.
    recipient_email VARCHAR(255) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    template_name VARCHAR(100),
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, SENT, FAILED, BOUNCED
    sent_at TIMESTAMP,
    error_message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create subscriptions table for subscription management
CREATE TABLE subscriptions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    subscription_plan_id BIGINT NOT NULL REFERENCES subscription_plans(id),
    status VARCHAR(20) NOT NULL, -- TRIAL, ACTIVE, PAST_DUE, CANCELLED, EXPIRED, SUSPENDED
    billing_cycle VARCHAR(20) NOT NULL, -- MONTHLY, QUARTERLY, YEARLY
    current_price DECIMAL(10,2),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    next_billing_date TIMESTAMP,
    trial_end_date TIMESTAMP,
    cancelled_at TIMESTAMP,
    cancellation_reason TEXT,
    auto_renew BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create usage_metrics table for tracking usage
CREATE TABLE usage_metrics (
    id BIGSERIAL PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    user_id VARCHAR(50) REFERENCES users(id) ON DELETE CASCADE,
    metric_date TIMESTAMP NOT NULL,
    metric_type VARCHAR(50) NOT NULL, -- USERS, STORAGE_GB, API_CALLS, etc.
    current_usage BIGINT NOT NULL,
    limit_value BIGINT,
    percentage_used DOUBLE PRECISION,
    is_over_limit BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create rate_limit_entries table for rate limiting
CREATE TABLE rate_limit_entries (
    id BIGSERIAL PRIMARY KEY,
    identifier VARCHAR(255) NOT NULL, -- IP address, user ID, or tenant ID
    endpoint VARCHAR(255) NOT NULL,
    request_count INTEGER DEFAULT 1,
    window_start TIMESTAMP NOT NULL,
    window_end TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(identifier, endpoint, window_start)
);

-- Create indexes for better performance
CREATE INDEX idx_tenants_admin_email ON tenants(admin_email);
CREATE INDEX idx_tenants_realm_name ON tenants(realm_name);
CREATE INDEX idx_tenants_status ON tenants(status);
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_changed_at ON audit_logs(changed_at);
CREATE INDEX idx_mobile_verifications_phone ON mobile_verifications(phone_number);
CREATE INDEX idx_mobile_verifications_tenant ON mobile_verifications(tenant_id);
CREATE INDEX idx_email_notifications_tenant ON email_notifications(tenant_id);
CREATE INDEX idx_email_notifications_status ON email_notifications(status);
CREATE INDEX idx_subscriptions_tenant ON subscriptions(tenant_id);
CREATE INDEX idx_subscriptions_status ON subscriptions(status);
CREATE INDEX idx_subscriptions_next_billing ON subscriptions(next_billing_date);
CREATE INDEX idx_usage_metrics_tenant ON usage_metrics(tenant_id);
CREATE INDEX idx_usage_metrics_type ON usage_metrics(metric_type);
CREATE INDEX idx_usage_metrics_date ON usage_metrics(metric_date);
CREATE INDEX idx_rate_limit_identifier ON rate_limit_entries(identifier, endpoint);

-- Insert default subscription plans
INSERT INTO subscription_plans (code, name, description, max_users, max_storage_gb, price_monthly, price_yearly, features) VALUES
('FREE', 'Free Plan', 'Basic plan with limited features', 5, 1, 0.00, 0.00, '{"features": ["basic_support", "standard_storage"]}'),
('BASIC', 'Basic Plan', 'Standard plan for small teams', 25, 10, 29.99, 299.99, '{"features": ["priority_support", "advanced_storage", "api_access"]}'),
('PROFESSIONAL', 'Professional Plan', 'Advanced plan for growing businesses', 100, 50, 99.99, 999.99, '{"features": ["24x7_support", "unlimited_storage", "api_access", "custom_integrations"]}'),
('ENTERPRISE', 'Enterprise Plan', 'Full-featured plan for large organizations', -1, -1, 299.99, 2999.99, '{"features": ["dedicated_support", "unlimited_storage", "api_access", "custom_integrations", "sso", "audit_logs"]}');

-- Create triggers for updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_tenants_updated_at BEFORE UPDATE ON tenants FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_tenant_configs_updated_at BEFORE UPDATE ON tenant_configs FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_subscriptions_updated_at BEFORE UPDATE ON subscriptions FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_rate_limit_entries_updated_at BEFORE UPDATE ON rate_limit_entries FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
