```
erDiagram
  CORE_USERS {
    UUID id PK
    CITEXT email
    TEXT phone_e164
    TEXT role
    TEXT status
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }

  PROGRAM_GUEST_PROFILES {
    UUID user_id PK, FK -> CORE_USERS.id
    TEXT full_name
    TEXT locale
    INT trust_score
    JSONB segments
    INT past_stays_count
    JSONB flags
    TEXT kyc_status
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }

  HOTEL_HOTELS {
    UUID id PK
    TEXT name
    TEXT address
    TEXT city
    TEXT country
    NUMERIC star_rating
    TEXT supplier_id
    TEXT status
    BOOLEAN needs_check
    INT check_priority
    TIMESTAMPTZ last_secret_check_at
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }

  HOTEL_QUALITY_SIGNALS {
    UUID id PK
    UUID hotel_id FK -> HOTEL_HOTELS.id
    TEXT source
    NUMERIC score
    JSONB payload
    TIMESTAMPTZ created_at
  }

  HOTEL_PUBLIC_METRICS {
    UUID hotel_id PK, FK -> HOTEL_HOTELS.id
    NUMERIC freshness_score
    NUMERIC overall_score
    JSONB section_scores
    TIMESTAMPTZ last_update_at
    JSONB snapshot
  }

  PROGRAM_ASSIGNMENTS {
    UUID id PK
    UUID guest_id FK -> CORE_USERS.id
    UUID hotel_id FK -> HOTEL_HOTELS.id
    TEXT status
    TIMESTAMPTZ offer_expires_at
    DATE checkin_date
    DATE checkout_date
    TEXT incentive_type
    NUMERIC incentive_value
    TEXT currency
    TIMESTAMPTZ created_at
    TIMESTAMPTZ updated_at
  }

  PROGRAM_STAYS {
    UUID id PK
    UUID assignment_id UNIQUE, FK -> PROGRAM_ASSIGNMENTS.id
    TIMESTAMPTZ actual_checkin_at
    TIMESTAMPTZ actual_checkout_at
    JSONB metadata
    TIMESTAMPTZ created_at
  }

  REVIEW_REPORTS {
    UUID id PK
    UUID assignment_id UNIQUE, FK -> PROGRAM_ASSIGNMENTS.id
    TIMESTAMPTZ submitted_at
    TEXT status
    NUMERIC overall_score
    JSONB sections
    TEXT comments
    NUMERIC fraud_score
    TIMESTAMPTZ created_at
  }

  REVIEW_REPORT_MEDIA {
    UUID id PK
    UUID report_id FK -> REVIEW_REPORTS.id
    TEXT type
    TEXT url
    TEXT sha256
    TIMESTAMPTZ taken_at
    JSONB exif
    TEXT provenance
    TEXT moderation
    TIMESTAMPTZ created_at
  }

  MODERATION_ACTIONS {
    UUID id PK
    UUID report_id FK -> REVIEW_REPORTS.id
    UUID moderator_id FK -> CORE_USERS.id
    TEXT action
    TEXT reason
    TIMESTAMPTZ created_at
  }

  BILLING_COUPONS {
    UUID id PK
    UUID user_id FK -> CORE_USERS.id
    TEXT code UNIQUE
    NUMERIC amount
    TEXT currency
    TIMESTAMPTZ expires_at
    TEXT status
    TIMESTAMPTZ created_at
  }

  OPS_AUDIT_LOG {
    UUID id PK
    UUID actor_id FK -> CORE_USERS.id
    TEXT action
    TEXT entity_type
    UUID entity_id
    JSONB payload
    TIMESTAMPTZ created_at
  }

  %% -------- Relationships (cardinalities) ----------
  CORE_USERS ||--|| PROGRAM_GUEST_PROFILES : "has profile"
  CORE_USERS ||--o{ PROGRAM_ASSIGNMENTS : "creates (as guest)"
  HOTEL_HOTELS ||--o{ PROGRAM_ASSIGNMENTS : "is assigned"
  PROGRAM_ASSIGNMENTS ||--|| PROGRAM_STAYS : "has stay"
  PROGRAM_ASSIGNMENTS ||--|| REVIEW_REPORTS : "has report"
  REVIEW_REPORTS ||--o{ REVIEW_REPORT_MEDIA : "has media"
  REVIEW_REPORTS ||--o{ MODERATION_ACTIONS : "moderated by"
  CORE_USERS ||--o{ MODERATION_ACTIONS : "acts (moderator)"
  CORE_USERS ||--o{ BILLING_COUPONS : "receives"
  HOTEL_HOTELS ||--o{ HOTEL_QUALITY_SIGNALS : "emits signal"
  HOTEL_HOTELS ||--|| HOTEL_PUBLIC_METRICS : "has metrics"
  CORE_USERS ||--o{ OPS_AUDIT_LOG : "actor"
```
