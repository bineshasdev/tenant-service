# Tenant Service Implementation Guide

## Overview

This document outlines the comprehensive implementation of the tenant service with the following key features:

1. ✅ **Email Notifications with Templates**
2. ✅ **Mobile Number Validation and Verification**
3. ✅ **Proper Database Migration Schema**
4. ✅ **Audit Trail Functionality**
5. ✅ **Rate Limiting and Security Enhancements**

## 🚀 New Features Implemented

### 1. Email Notifications with Templates

#### Features:
- **Thymeleaf Email Templates**: Professional HTML email templates
- **Multiple Email Types**: Signup started, signup completed, welcome, mobile verification
- **Email Tracking**: Database tracking of all sent emails
- **Retry Mechanism**: Failed email retry functionality
- **Template Engine**: Configurable email templates

#### Templates Created:
- `signup-started.html` - Welcome email when signup begins
- `signup-completed.html` - Account ready notification with credentials
- `welcome.html` - Welcome email for new users
- `mobile-verification.html` - OTP verification email

#### API Endpoints:
```http
POST /api/account/signup
GET /api/admin/emails/tenant/{tenantId}
GET /api/admin/emails/failed
POST /api/admin/emails/{emailId}/retry
```

### 2. Mobile Number Validation and Verification

#### Features:
- **International Phone Validation**: Using Google's libphonenumber library
- **OTP Generation**: 6-digit OTP with configurable expiry
- **Rate Limiting**: Prevents OTP spam (5 requests per hour)
- **Verification Tracking**: Database tracking of verification attempts
- **Security**: Maximum 3 verification attempts per OTP

#### API Endpoints:
```http
POST /api/account/verify-mobile
POST /api/account/resend-otp
```

#### Configuration:
```yaml
mobile-verification:
  otp-length: 6
  otp-expiry-minutes: 10
  max-attempts: 3
  max-requests-per-hour: 5
```

### 3. Database Migration Schema

#### New Tables Created:
- `subscription_plans` - Subscription plan definitions
- `tenants` - Enhanced tenant information
- `tenant_configs` - Tenant-specific configurations
- `users` - User management
- `audit_logs` - Comprehensive audit trail
- `mobile_verifications` - OTP verification tracking
- `email_notifications` - Email delivery tracking
- `rate_limit_entries` - Rate limiting data

#### Key Features:
- **Proper Indexing**: Performance-optimized indexes
- **Foreign Key Constraints**: Data integrity
- **Triggers**: Automatic timestamp updates
- **JSONB Support**: Flexible data storage
- **Audit Trail**: Complete change tracking

### 4. Audit Trail Functionality

#### Features:
- **Comprehensive Logging**: All entity changes tracked
- **JSON Storage**: Old and new values stored as JSON
- **User Context**: IP address, user agent, user ID tracking
- **Flexible Queries**: Search by entity, user, date range
- **Performance Optimized**: Indexed for fast queries

#### API Endpoints:
```http
GET /api/admin/audit/{entityType}/{entityId}
GET /api/admin/audit/{entityType}/{entityId}/page
GET /api/admin/audit/user/{userId}
GET /api/admin/audit/date-range
```

### 5. Rate Limiting and Security Enhancements

#### Features:
- **AOP-Based Rate Limiting**: Annotation-driven rate limiting
- **Endpoint-Specific Limits**: Different limits for different endpoints
- **Multiple Identifiers**: IP, user ID, tenant ID based limiting
- **Security Headers**: HSTS, CSP, frame options
- **CORS Configuration**: Proper cross-origin setup

#### Rate Limits:
- **Signup**: 5 requests per 15 minutes
- **Login**: 10 requests per 15 minutes
- **OTP**: 3 requests per 15 minutes
- **API**: 1000 requests per 15 minutes

#### Security Features:
- **Password Encryption**: BCrypt with strength 12
- **Input Validation**: Comprehensive request validation
- **SQL Injection Protection**: JPA parameterized queries
- **XSS Protection**: Input sanitization

## 🔧 Configuration

### Environment Variables

```bash
# Email Configuration
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_FROM=noreply@offisync360.com
MAIL_FROM_NAME=OffiSync360

# Application Configuration
APP_BASE_URL=https://your-domain.com

# Database Configuration
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tenant_service
DB_USERNAME=postgres
DB_PASSWORD=password
```

### Application Properties

```yaml
# Email Configuration
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${MAIL_USERNAME:}
    password: ${MAIL_PASSWORD:}

# Rate Limiting
rate-limit:
  default:
    window-size-minutes: 15
    max-requests: 100
  signup:
    max-requests: 5

# Mobile Verification
mobile-verification:
  otp-length: 6
  otp-expiry-minutes: 10
  max-attempts: 3
```

## 📊 Database Schema

### Key Tables Overview:

```sql
-- Tenants table with enhanced fields
CREATE TABLE tenants (
    id VARCHAR(50) PRIMARY KEY,
    display_name VARCHAR(100) NOT NULL,
    admin_email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    country VARCHAR(3) DEFAULT 'IN',
    -- ... other fields
);

-- Audit logs for complete tracking
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NOT NULL,
    action VARCHAR(20) NOT NULL,
    old_values JSONB,
    new_values JSONB,
    -- ... other fields
);

-- Mobile verification tracking
CREATE TABLE mobile_verifications (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,
    otp_code VARCHAR(10) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    -- ... other fields
);
```

## 🚀 API Usage Examples

### 1. Tenant Signup

```bash
curl -X POST http://localhost:8080/api/account/signup \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Acme Corp",
    "displayName": "Acme Corporation",
    "adminEmail": "admin@acme.com",
    "adminFirstName": "John",
    "adminLastName": "Doe",
    "phone": "+1234567890",
    "country": "US",
    "acceptTerms": true
  }'
```

### 2. Mobile Verification

```bash
# Send OTP
curl -X POST http://localhost:8080/api/account/resend-otp \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1234567890",
    "tenantId": "acme-corp"
  }'

# Verify OTP
curl -X POST http://localhost:8080/api/account/verify-mobile \
  -H "Content-Type: application/json" \
  -d '{
    "phoneNumber": "+1234567890",
    "otpCode": "123456"
  }'
```

### 3. Audit Logs

```bash
# Get audit history for a tenant
curl -X GET http://localhost:8080/api/admin/audit/TENANT/acme-corp

# Get paginated audit logs
curl -X GET "http://localhost:8080/api/admin/audit/TENANT/acme-corp/page?page=0&size=10"
```

## 🔒 Security Considerations

### 1. Rate Limiting
- All endpoints are rate limited
- Different limits for different operations
- IP-based and user-based limiting

### 2. Input Validation
- Comprehensive request validation
- Phone number format validation
- Email format validation
- SQL injection protection

### 3. Audit Trail
- All changes are logged
- User context is captured
- IP addresses are tracked
- Failed operations are logged

### 4. Email Security
- SMTP authentication required
- Secure email templates
- Email delivery tracking
- Failed email retry mechanism

## 📈 Monitoring and Observability

### 1. Audit Logs
- Complete audit trail of all operations
- Searchable by entity, user, date range
- JSON storage for flexible querying

### 2. Email Tracking
- Delivery status tracking
- Failed email monitoring
- Retry mechanism for failed emails

### 3. Rate Limiting Monitoring
- Rate limit violations are logged
- Configurable rate limits per endpoint
- Automatic cleanup of expired entries

## 🛠️ Development Setup

### 1. Prerequisites
- Java 17+
- PostgreSQL 13+
- Maven 3.8+
- Keycloak 25+

### 2. Database Setup
```bash
# Create database
createdb tenant_service

# Run migrations (automatic on startup)
mvn spring-boot:run
```

### 3. Email Setup
```bash
# For Gmail
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

### 4. Running the Service
```bash
mvn spring-boot:run
```

## 📝 Testing

### 1. Unit Tests
- Service layer tests
- Repository tests
- Validation tests

### 2. Integration Tests
- API endpoint tests
- Database integration tests
- Email service tests

### 3. Load Testing
- Rate limiting tests
- Concurrent signup tests
- Database performance tests

## 🔄 Future Enhancements

### 1. Async Processing
- Background job processing
- Queue-based email sending
- Retry mechanisms

### 2. Advanced Security
- JWT token validation
- OAuth2 integration
- Multi-factor authentication

### 3. Monitoring
- Prometheus metrics
- Health checks
- Performance monitoring

### 4. Scalability
- Database sharding
- Caching layer
- Load balancing

## 📞 Support

For questions or issues with this implementation:

1. Check the logs for detailed error messages
2. Verify configuration settings
3. Test with the provided API examples
4. Review the audit logs for troubleshooting

## 🎯 Key Benefits

1. **Complete Audit Trail**: Every action is tracked and searchable
2. **Professional Email System**: Beautiful, responsive email templates
3. **Mobile Verification**: Secure OTP-based phone verification
4. **Rate Limiting**: Protection against abuse and spam
5. **Security**: Comprehensive security measures and validations
6. **Scalability**: Database schema designed for growth
7. **Monitoring**: Built-in observability and tracking

This implementation provides a robust, secure, and scalable foundation for your SaaS tenant management system.