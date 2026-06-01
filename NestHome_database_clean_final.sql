/* =========================================================
   NestHome / DormAssist - CLEAN DATABASE SCHEMA
   Target DBMS : Microsoft SQL Server
   App         : Java Swing Desktop Dormitory Management
   Purpose     : Clean schema, relationships, constraints and seed data
   Note        : This script DOES NOT drop or delete an existing database.
                 Run it on a new/empty [dormassist] database.
   ========================================================= */

IF DB_ID(N'dormassist') IS NULL
BEGIN
    CREATE DATABASE dormassist;
END
GO

USE dormassist;
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

/* =========================================================
   1. USERS / AUTHENTICATION
   Quan hệ:
   users 1 - 1 user_avatars
   users 1 - n tokens.created_by
   users 1 - n tokens.used_by
   users 1 - n password_reset_codes
   users 1 - n activity_logs
   users 1 - 0/1 students
   ========================================================= */

CREATE TABLE dbo.users (
    id             INT IDENTITY(1,1) NOT NULL,
    username       VARCHAR(50) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    role           VARCHAR(30) NOT NULL,
    full_name      NVARCHAR(150) NOT NULL,
    email          VARCHAR(150) NULL,
    phone          VARCHAR(30) NULL,
    token_used     VARCHAR(32) NULL,
    is_active      BIT NOT NULL CONSTRAINT DF_users_is_active DEFAULT (1),
    last_login     DATETIME2(0) NULL,
    created_at     DATETIME2(0) NOT NULL CONSTRAINT DF_users_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_users PRIMARY KEY (id),

    CONSTRAINT CK_users_role CHECK (
        role IN ('ADMIN_SUPER', 'ADMIN_BASE', 'BASE1', 'BASE2', 'BASE3')
    ),

    CONSTRAINT CK_users_username_not_blank CHECK (
        LEN(LTRIM(RTRIM(username))) > 0
    ),

    CONSTRAINT CK_users_full_name_not_blank CHECK (
        LEN(LTRIM(RTRIM(full_name))) > 0
    )
);
GO

CREATE UNIQUE INDEX UX_users_username
ON dbo.users(username);
GO

CREATE UNIQUE INDEX UX_users_email_not_blank
ON dbo.users(email)
WHERE email IS NOT NULL AND email <> '';
GO

CREATE INDEX IX_users_role_active
ON dbo.users(role, is_active);
GO


CREATE TABLE dbo.user_avatars (
    user_id       INT NOT NULL,
    avatar_data   VARBINARY(MAX) NOT NULL,
    content_type  VARCHAR(50) NOT NULL CONSTRAINT DF_user_avatars_content_type DEFAULT ('image/png'),
    updated_at    DATETIME2(0) NOT NULL CONSTRAINT DF_user_avatars_updated_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_user_avatars PRIMARY KEY (user_id),

    CONSTRAINT FK_user_avatars_users
        FOREIGN KEY (user_id) REFERENCES dbo.users(id)
        ON DELETE CASCADE,

    CONSTRAINT CK_user_avatars_content_type CHECK (
        content_type IN ('image/png', 'image/jpeg', 'image/jpg')
    ),

    CONSTRAINT CK_user_avatars_size CHECK (
        DATALENGTH(avatar_data) <= 2097152
    )
);
GO


CREATE TABLE dbo.activity_logs (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    user_id     INT NULL,
    action      NVARCHAR(80) NOT NULL,
    module      NVARCHAR(80) NULL,
    details     NVARCHAR(MAX) NULL,
    created_at  DATETIME2(0) NOT NULL CONSTRAINT DF_activity_logs_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_activity_logs PRIMARY KEY (id),

    CONSTRAINT FK_activity_logs_users
        FOREIGN KEY (user_id) REFERENCES dbo.users(id)
        ON DELETE SET NULL,

    CONSTRAINT CK_activity_logs_action_not_blank CHECK (
        LEN(LTRIM(RTRIM(action))) > 0
    )
);
GO

CREATE INDEX IX_activity_logs_user_created
ON dbo.activity_logs(user_id, created_at DESC);
GO


/* =========================================================
   2. TOKENS
   Quan hệ:
   users 1 - n tokens.created_by
   users 1 - n tokens.used_by
   ========================================================= */

CREATE TABLE dbo.tokens (
    id          INT IDENTITY(1,1) NOT NULL,
    token_code  VARCHAR(32) NOT NULL,
    role        VARCHAR(30) NOT NULL,
    is_used     BIT NOT NULL CONSTRAINT DF_tokens_is_used DEFAULT (0),
    used_by     INT NULL,
    created_by  INT NULL,
    created_at  DATETIME2(0) NOT NULL CONSTRAINT DF_tokens_created_at DEFAULT SYSDATETIME(),
    used_at     DATETIME2(0) NULL,

    CONSTRAINT PK_tokens PRIMARY KEY (id),

    CONSTRAINT FK_tokens_used_by_users
        FOREIGN KEY (used_by) REFERENCES dbo.users(id),

    CONSTRAINT FK_tokens_created_by_users
        FOREIGN KEY (created_by) REFERENCES dbo.users(id),

    CONSTRAINT CK_tokens_role CHECK (
        role IN ('ADMIN_SUPER', 'ADMIN_BASE', 'BASE1', 'BASE2', 'BASE3')
    ),

    CONSTRAINT CK_tokens_code_not_blank CHECK (
        LEN(LTRIM(RTRIM(token_code))) > 0
    ),

    CONSTRAINT CK_tokens_used_logic CHECK (
        (is_used = 0 AND used_by IS NULL AND used_at IS NULL)
        OR
        (is_used = 1)
    )
);
GO

CREATE UNIQUE INDEX UX_tokens_token_code
ON dbo.tokens(token_code);
GO

CREATE INDEX IX_tokens_role_used
ON dbo.tokens(role, is_used);
GO


/* =========================================================
   3. PASSWORD RESET
   Quan hệ:
   users 1 - n password_reset_codes
   ========================================================= */

CREATE TABLE dbo.password_reset_codes (
    id          BIGINT IDENTITY(1,1) NOT NULL,
    user_id     INT NOT NULL,
    email       VARCHAR(150) NOT NULL,
    code_hash   VARCHAR(255) NOT NULL,
    expires_at  DATETIME2(0) NOT NULL,
    used        BIT NOT NULL CONSTRAINT DF_password_reset_codes_used DEFAULT (0),
    created_at  DATETIME2(0) NOT NULL CONSTRAINT DF_password_reset_codes_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_password_reset_codes PRIMARY KEY (id),

    CONSTRAINT FK_password_reset_codes_users
        FOREIGN KEY (user_id) REFERENCES dbo.users(id)
        ON DELETE CASCADE,

    CONSTRAINT CK_password_reset_email_not_blank CHECK (
        LEN(LTRIM(RTRIM(email))) > 0
    )
);
GO

CREATE INDEX IX_password_reset_user_valid
ON dbo.password_reset_codes(user_id, used, expires_at DESC, created_at DESC);
GO


/* =========================================================
   4. BUILDINGS / ROOMS
   Quan hệ:
   buildings 1 - n rooms
   rooms 1 - n students
   rooms 1 - n assets
   rooms 1 - n bills
   rooms 1 - n incidents
   rooms 1 - n readings
   rooms 1 - n room_funds
   ========================================================= */

CREATE TABLE dbo.buildings (
    id            INT IDENTITY(1,1) NOT NULL,
    name          NVARCHAR(100) NOT NULL,
    total_floors  INT NOT NULL CONSTRAINT DF_buildings_total_floors DEFAULT (5),
    description   NVARCHAR(MAX) NULL,
    created_at    DATETIME2(0) NOT NULL CONSTRAINT DF_buildings_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_buildings PRIMARY KEY (id),

    CONSTRAINT CK_buildings_name_not_blank CHECK (
        LEN(LTRIM(RTRIM(name))) > 0
    ),

    CONSTRAINT CK_buildings_total_floors CHECK (
        total_floors > 0
    )
);
GO

CREATE UNIQUE INDEX UX_buildings_name
ON dbo.buildings(name);
GO


CREATE TABLE dbo.rooms (
    id                 INT IDENTITY(1,1) NOT NULL,
    room_number        VARCHAR(20) NOT NULL,
    floor              INT NOT NULL,
    building_id        INT NULL,
    capacity           INT NOT NULL CONSTRAINT DF_rooms_capacity DEFAULT (8),
    current_occupants  INT NOT NULL CONSTRAINT DF_rooms_current_occupants DEFAULT (0),
    status             VARCHAR(20) NOT NULL CONSTRAINT DF_rooms_status DEFAULT ('AVAILABLE'),
    rent_price         DECIMAL(18,2) NOT NULL CONSTRAINT DF_rooms_rent_price DEFAULT (0),
    room_type          VARCHAR(30) NOT NULL CONSTRAINT DF_rooms_room_type DEFAULT ('STANDARD'),
    notes              NVARCHAR(MAX) NULL,
    created_at         DATETIME2(0) NOT NULL CONSTRAINT DF_rooms_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_rooms PRIMARY KEY (id),

    CONSTRAINT FK_rooms_buildings
        FOREIGN KEY (building_id) REFERENCES dbo.buildings(id)
        ON DELETE SET NULL,

    CONSTRAINT CK_rooms_floor CHECK (floor >= 0),

    CONSTRAINT CK_rooms_capacity CHECK (capacity > 0),

    CONSTRAINT CK_rooms_current_occupants CHECK (current_occupants >= 0),

    CONSTRAINT CK_rooms_capacity_occupants CHECK (current_occupants <= capacity),

    CONSTRAINT CK_rooms_status CHECK (
        status IN ('AVAILABLE', 'FULL', 'MAINTENANCE', 'CLOSED')
    ),

    CONSTRAINT CK_rooms_type CHECK (
        room_type IN ('STANDARD', 'VIP', 'DISABLED_ACCESS')
    ),

    CONSTRAINT CK_rooms_rent_price CHECK (rent_price >= 0)
);
GO

CREATE UNIQUE INDEX UX_rooms_room_number
ON dbo.rooms(room_number);
GO

CREATE INDEX IX_rooms_building_floor
ON dbo.rooms(building_id, floor, room_number);
GO

CREATE INDEX IX_rooms_status
ON dbo.rooms(status);
GO


/* =========================================================
   5. STUDENTS
   Quan hệ:
   users 1 - 0/1 students
   rooms 1 - n students
   students 1 - n discipline_points
   students 1 - n room_transfer_requests
   students 1 - n visitors
   students 1 - n handover_records
   ========================================================= */

CREATE TABLE dbo.students (
    id                   INT IDENTITY(1,1) NOT NULL,
    user_id              INT NULL,
    full_name            NVARCHAR(150) NOT NULL,
    student_code         VARCHAR(30) NULL,
    id_card              VARCHAR(30) NULL,
    dob                  DATE NULL,
    gender               VARCHAR(10) NOT NULL CONSTRAINT DF_students_gender DEFAULT ('OTHER'),
    phone                VARCHAR(30) NULL,
    email                VARCHAR(150) NULL,
    hometown             NVARCHAR(255) NULL,
    room_id              INT NULL,
    join_date            DATE NULL,
    expected_leave_date  DATE NULL,
    status               VARCHAR(30) NOT NULL CONSTRAINT DF_students_status DEFAULT ('ACTIVE'),
    discipline_points    INT NOT NULL CONSTRAINT DF_students_discipline_points DEFAULT (100),
    notes                NVARCHAR(MAX) NULL,
    created_at           DATETIME2(0) NOT NULL CONSTRAINT DF_students_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_students PRIMARY KEY (id),

    CONSTRAINT FK_students_users
        FOREIGN KEY (user_id) REFERENCES dbo.users(id)
        ON DELETE SET NULL,

    CONSTRAINT FK_students_rooms
        FOREIGN KEY (room_id) REFERENCES dbo.rooms(id)
        ON DELETE SET NULL,

    CONSTRAINT CK_students_full_name_not_blank CHECK (
        LEN(LTRIM(RTRIM(full_name))) > 0
    ),

    CONSTRAINT CK_students_gender CHECK (
        gender IN ('MALE', 'FEMALE', 'OTHER')
    ),

    CONSTRAINT CK_students_status CHECK (
        status IN ('ACTIVE', 'TEMPORARY_ABSENT', 'MOVED_OUT')
    ),

    CONSTRAINT CK_students_points CHECK (
        discipline_points BETWEEN 0 AND 100
    ),

    CONSTRAINT CK_students_leave_after_join CHECK (
        expected_leave_date IS NULL
        OR join_date IS NULL
        OR expected_leave_date >= join_date
    )
);
GO

CREATE UNIQUE INDEX UX_students_user_id_not_null
ON dbo.students(user_id)
WHERE user_id IS NOT NULL;
GO

CREATE UNIQUE INDEX UX_students_student_code_not_blank
ON dbo.students(student_code)
WHERE student_code IS NOT NULL AND student_code <> '';
GO

CREATE UNIQUE INDEX UX_students_id_card_not_blank
ON dbo.students(id_card)
WHERE id_card IS NOT NULL AND id_card <> '';
GO

CREATE INDEX IX_students_room_status
ON dbo.students(room_id, status);
GO

CREATE INDEX IX_students_status_name
ON dbo.students(status, full_name);
GO


/* =========================================================
   6. PRICE CONFIG
   Quan hệ:
   users 1 - n price_config
   price_config 1 - n bills
   ========================================================= */

CREATE TABLE dbo.price_config (
    id              INT IDENTITY(1,1) NOT NULL,
    electric_price  DECIMAL(18,2) NOT NULL CONSTRAINT DF_price_config_electric DEFAULT (3500),
    water_price     DECIMAL(18,2) NOT NULL CONSTRAINT DF_price_config_water DEFAULT (15000),
    service_fee     DECIMAL(18,2) NOT NULL CONSTRAINT DF_price_config_service DEFAULT (50000),
    effective_date  DATE NOT NULL,
    created_by      INT NULL,
    created_at      DATETIME2(0) NOT NULL CONSTRAINT DF_price_config_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_price_config PRIMARY KEY (id),

    CONSTRAINT FK_price_config_users
        FOREIGN KEY (created_by) REFERENCES dbo.users(id)
        ON DELETE SET NULL,

    CONSTRAINT CK_price_config_values CHECK (
        electric_price >= 0
        AND water_price >= 0
        AND service_fee >= 0
    )
);
GO

CREATE INDEX IX_price_config_effective
ON dbo.price_config(effective_date DESC, created_at DESC);
GO


/* =========================================================
   7. ELECTRIC / WATER READINGS
   Quan hệ:
   rooms 1 - n electric_readings
   rooms 1 - n water_readings
   users 1 - n electric_readings.recorded_by
   users 1 - n water_readings.recorded_by
   electric_readings 1 - 0/1 bills
   water_readings 1 - 0/1 bills
   ========================================================= */

CREATE TABLE dbo.electric_readings (
    id                INT IDENTITY(1,1) NOT NULL,
    room_id            INT NOT NULL,
    reading_month      INT NOT NULL,
    reading_year       INT NOT NULL,
    previous_reading   DECIMAL(18,2) NOT NULL CONSTRAINT DF_electric_prev DEFAULT (0),
    current_reading    DECIMAL(18,2) NOT NULL CONSTRAINT DF_electric_current DEFAULT (0),
    consumption        AS (current_reading - previous_reading) PERSISTED,
    reading_date       DATE NOT NULL CONSTRAINT DF_electric_reading_date DEFAULT CAST(GETDATE() AS DATE),
    recorded_by        INT NULL,
    status             VARCHAR(20) NOT NULL CONSTRAINT DF_electric_status DEFAULT ('CONFIRMED'),
    created_at         DATETIME2(0) NOT NULL CONSTRAINT DF_electric_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_electric_readings PRIMARY KEY (id),

    CONSTRAINT FK_electric_readings_rooms
        FOREIGN KEY (room_id) REFERENCES dbo.rooms(id)
        ON DELETE CASCADE,

    CONSTRAINT FK_electric_readings_users
        FOREIGN KEY (recorded_by) REFERENCES dbo.users(id)
        ON DELETE SET NULL,

    CONSTRAINT UQ_electric_room_period UNIQUE (room_id, reading_month, reading_year),

    CONSTRAINT CK_electric_month CHECK (reading_month BETWEEN 1 AND 12),

    CONSTRAINT CK_electric_year CHECK (reading_year BETWEEN 2000 AND 2100),

    CONSTRAINT CK_electric_status CHECK (
        status IN ('SUBMITTED', 'REVIEWED', 'CONFIRMED', 'ACTIONED')
    ),

    CONSTRAINT CK_electric_reading CHECK (
        previous_reading >= 0
        AND current_reading >= previous_reading
    )
);
GO

CREATE INDEX IX_electric_room_period
ON dbo.electric_readings(room_id, reading_year DESC, reading_month DESC);
GO


CREATE TABLE dbo.water_readings (
    id                INT IDENTITY(1,1) NOT NULL,
    room_id            INT NOT NULL,
    reading_month      INT NOT NULL,
    reading_year       INT NOT NULL,
    previous_reading   DECIMAL(18,2) NOT NULL CONSTRAINT DF_water_prev DEFAULT (0),
    current_reading    DECIMAL(18,2) NOT NULL CONSTRAINT DF_water_current DEFAULT (0),
    consumption        AS (current_reading - previous_reading) PERSISTED,
    reading_date       DATE NOT NULL CONSTRAINT DF_water_reading_date DEFAULT CAST(GETDATE() AS DATE),
    recorded_by        INT NULL,
    status             VARCHAR(20) NOT NULL CONSTRAINT DF_water_status DEFAULT ('CONFIRMED'),
    created_at         DATETIME2(0) NOT NULL CONSTRAINT DF_water_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_water_readings PRIMARY KEY (id),

    CONSTRAINT FK_water_readings_rooms
        FOREIGN KEY (room_id) REFERENCES dbo.rooms(id)
        ON DELETE CASCADE,

    CONSTRAINT FK_water_readings_users
        FOREIGN KEY (recorded_by) REFERENCES dbo.users(id)
        ON DELETE SET NULL,

    CONSTRAINT UQ_water_room_period UNIQUE (room_id, reading_month, reading_year),

    CONSTRAINT CK_water_month CHECK (reading_month BETWEEN 1 AND 12),

    CONSTRAINT CK_water_year CHECK (reading_year BETWEEN 2000 AND 2100),

    CONSTRAINT CK_water_status CHECK (
        status IN ('SUBMITTED', 'REVIEWED', 'CONFIRMED', 'ACTIONED')
    ),

    CONSTRAINT CK_water_reading CHECK (
        previous_reading >= 0
        AND current_reading >= previous_reading
    )
);
GO

CREATE INDEX IX_water_room_period
ON dbo.water_readings(room_id, reading_year DESC, reading_month DESC);
GO


/* =========================================================
   8. BILLS
   Quan hệ:
   rooms 1 - n bills
   users 1 - n bills.created_by
   users 1 - n bills.paid_by
   price_config 1 - n bills
   electric_readings 1 - 0/1 bills
   water_readings 1 - 0/1 bills
   ========================================================= */

CREATE TABLE dbo.bills (
    id                    INT IDENTITY(1,1) NOT NULL,
    room_id                INT NULL,
    price_config_id        INT NULL,
    electric_reading_id    INT NULL,
    water_reading_id       INT NULL,
    bill_month             INT NOT NULL,
    bill_year              INT NOT NULL,
    electric_consumption   DECIMAL(18,2) NOT NULL CONSTRAINT DF_bills_electric_consumption DEFAULT (0),
    electric_amount        DECIMAL(18,2) NOT NULL CONSTRAINT DF_bills_electric_amount DEFAULT (0),
    water_consumption      DECIMAL(18,2) NOT NULL CONSTRAINT DF_bills_water_consumption DEFAULT (0),
    water_amount           DECIMAL(18,2) NOT NULL CONSTRAINT DF_bills_water_amount DEFAULT (0),
    rent_amount            DECIMAL(18,2) NOT NULL CONSTRAINT DF_bills_rent_amount DEFAULT (0),
    service_amount         DECIMAL(18,2) NOT NULL CONSTRAINT DF_bills_service_amount DEFAULT (0),
    total_amount           DECIMAL(18,2) NOT NULL CONSTRAINT DF_bills_total_amount DEFAULT (0),
    status                 VARCHAR(20) NOT NULL CONSTRAINT DF_bills_status DEFAULT ('UNPAID'),
    due_date               DATE NULL,
    paid_date              DATE NULL,
    paid_by                INT NULL,
    created_by             INT NULL,
    notes                  NVARCHAR(MAX) NULL,
    created_at             DATETIME2(0) NOT NULL CONSTRAINT DF_bills_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_bills PRIMARY KEY (id),

    CONSTRAINT FK_bills_rooms
        FOREIGN KEY (room_id) REFERENCES dbo.rooms(id)
        ON DELETE SET NULL,

    CONSTRAINT FK_bills_price_config
        FOREIGN KEY (price_config_id) REFERENCES dbo.price_config(id),

    CONSTRAINT FK_bills_electric_readings
        FOREIGN KEY (electric_reading_id) REFERENCES dbo.electric_readings(id),

    CONSTRAINT FK_bills_water_readings
        FOREIGN KEY (water_reading_id) REFERENCES dbo.water_readings(id),

    CONSTRAINT FK_bills_paid_by_users
        FOREIGN KEY (paid_by) REFERENCES dbo.users(id)
        ON DELETE SET NULL,

    CONSTRAINT FK_bills_created_by_users
        FOREIGN KEY (created_by) REFERENCES dbo.users(id),

    CONSTRAINT CK_bills_month CHECK (bill_month BETWEEN 1 AND 12),

    CONSTRAINT CK_bills_year CHECK (bill_year BETWEEN 2000 AND 2100),

    CONSTRAINT CK_bills_status CHECK (
        status IN ('UNPAID', 'PAID', 'OVERDUE', 'CANCELLED', 'WAIVED', 'BILLED')
    ),

    CONSTRAINT CK_bills_amounts CHECK (
        electric_consumption >= 0
        AND electric_amount >= 0
        AND water_consumption >= 0
        AND water_amount >= 0
        AND rent_amount >= 0
        AND service_amount >= 0
        AND total_amount >= 0
    ),

    CONSTRAINT CK_bills_paid_logic CHECK (
        (status = 'PAID' AND paid_date IS NOT NULL)
        OR
        (status <> 'PAID')
    )
);
GO

CREATE UNIQUE INDEX UX_bills_room_period
ON dbo.bills(room_id, bill_month, bill_year)
WHERE room_id IS NOT NULL;
GO

CREATE UNIQUE INDEX UX_bills_electric_reading_not_null
ON dbo.bills(electric_reading_id)
WHERE electric_reading_id IS NOT NULL;
GO

CREATE UNIQUE INDEX UX_bills_water_reading_not_null
ON dbo.bills(water_reading_id)
WHERE water_reading_id IS NOT NULL;
GO

CREATE INDEX IX_bills_status_due
ON dbo.bills(status, due_date);
GO

CREATE INDEX IX_bills_room_period_lookup
ON dbo.bills(room_id, bill_year DESC, bill_month DESC);
GO


/* =========================================================
   9. ASSETS
   Quan hệ:
   rooms 1 - n assets
   ========================================================= */

CREATE TABLE dbo.assets (
    id                INT IDENTITY(1,1) NOT NULL,
    room_id            INT NULL,
    asset_name         NVARCHAR(150) NOT NULL,
    asset_code         VARCHAR(50) NULL,
    category           VARCHAR(30) NOT NULL CONSTRAINT DF_assets_category DEFAULT ('OTHER'),
    quantity           INT NOT NULL CONSTRAINT DF_assets_quantity DEFAULT (1),
    condition_status   VARCHAR(20) NOT NULL CONSTRAINT DF_assets_condition DEFAULT ('GOOD'),
    purchase_price     DECIMAL(18,2) NOT NULL CONSTRAINT DF_assets_purchase_price DEFAULT (0),
    purchase_date      DATE NULL,
    notes              NVARCHAR(MAX) NULL,
    created_at         DATETIME2(0) NOT NULL CONSTRAINT DF_assets_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_assets PRIMARY KEY (id),

    CONSTRAINT FK_assets_rooms
        FOREIGN KEY (room_id) REFERENCES dbo.rooms(id)
        ON DELETE SET NULL,

    CONSTRAINT CK_assets_name_not_blank CHECK (
        LEN(LTRIM(RTRIM(asset_name))) > 0
    ),

    CONSTRAINT CK_assets_quantity CHECK (quantity > 0),

    CONSTRAINT CK_assets_price CHECK (purchase_price >= 0),

    CONSTRAINT CK_assets_category CHECK (
        category IN ('BED', 'LOCKER', 'AC', 'FAN', 'DESK', 'CHAIR', 'OTHER')
    ),

    CONSTRAINT CK_assets_condition CHECK (
        condition_status IN ('GOOD', 'FAIR', 'BROKEN', 'MISSING')
    )
);
GO

CREATE UNIQUE INDEX UX_assets_asset_code_not_blank
ON dbo.assets(asset_code)
WHERE asset_code IS NOT NULL AND asset_code <> '';
GO

CREATE INDEX IX_assets_room_category
ON dbo.assets(room_id, category);
GO

CREATE INDEX IX_assets_condition
ON dbo.assets(condition_status);
GO


/* =========================================================
   10. INCIDENTS
   Quan hệ:
   users 1 - n incidents.reporter_id
   users 1 - n incidents.assigned_to
   rooms 1 - n incidents
   ========================================================= */

CREATE TABLE dbo.incidents (
    id                INT IDENTITY(1,1) NOT NULL,
    reporter_id       INT NULL,
    room_id           INT NULL,
    title             NVARCHAR(200) NOT NULL,
    description       NVARCHAR(MAX) NULL,
    priority          VARCHAR(20) NOT NULL CONSTRAINT DF_incidents_priority DEFAULT ('MEDIUM'),
    status            VARCHAR(20) NOT NULL CONSTRAINT DF_incidents_status DEFAULT ('PENDING'),
    assigned_to       INT NULL,
    resolution_notes  NVARCHAR(MAX) NULL,
    resolved_date     DATETIME2(0) NULL,
    created_at        DATETIME2(0) NOT NULL CONSTRAINT DF_incidents_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_incidents PRIMARY KEY (id),

    CONSTRAINT FK_incidents_reporter_users
        FOREIGN KEY (reporter_id) REFERENCES dbo.users(id)
        ON DELETE SET NULL,

    CONSTRAINT FK_incidents_rooms
        FOREIGN KEY (room_id) REFERENCES dbo.rooms(id)
        ON DELETE SET NULL,

    CONSTRAINT FK_incidents_assigned_users
        FOREIGN KEY (assigned_to) REFERENCES dbo.users(id),

    CONSTRAINT CK_incidents_title_not_blank CHECK (
        LEN(LTRIM(RTRIM(title))) > 0
    ),

    CONSTRAINT CK_incidents_priority CHECK (
        priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT')
    ),

    CONSTRAINT CK_incidents_status CHECK (
        status IN ('PENDING', 'IN_PROGRESS', 'RESOLVED', 'CANCELLED')
    ),

    CONSTRAINT CK_incidents_resolved_logic CHECK (
        (status = 'RESOLVED' AND resolved_date IS NOT NULL)
        OR
        (status <> 'RESOLVED')
    )
);
GO

CREATE INDEX IX_incidents_status_priority_created
ON dbo.incidents(status, priority, created_at DESC);
GO

CREATE INDEX IX_incidents_reporter_created
ON dbo.incidents(reporter_id, created_at DESC);
GO

CREATE INDEX IX_incidents_room_created
ON dbo.incidents(room_id, created_at DESC);
GO


/* =========================================================
   11. NOTIFICATIONS
   Quan hệ:
   users 1 - n notifications.sender_id
   rooms 1 - n notifications.target_room_id
   ========================================================= */

CREATE TABLE dbo.notifications (
    id              INT IDENTITY(1,1) NOT NULL,
    sender_id       INT NULL,
    title           NVARCHAR(200) NOT NULL,
    content         NVARCHAR(MAX) NOT NULL,
    target_role     VARCHAR(30) NOT NULL CONSTRAINT DF_notifications_target_role DEFAULT ('ALL'),
    target_room_id  INT NULL,
    is_important    BIT NOT NULL CONSTRAINT DF_notifications_important DEFAULT (0),
    created_at      DATETIME2(0) NOT NULL CONSTRAINT DF_notifications_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_notifications PRIMARY KEY (id),

    CONSTRAINT FK_notifications_sender_users
        FOREIGN KEY (sender_id) REFERENCES dbo.users(id)
        ON DELETE SET NULL,

    CONSTRAINT FK_notifications_rooms
        FOREIGN KEY (target_room_id) REFERENCES dbo.rooms(id)
        ON DELETE SET NULL,

    CONSTRAINT CK_notifications_title_not_blank CHECK (
        LEN(LTRIM(RTRIM(title))) > 0
    ),

    CONSTRAINT CK_notifications_content_not_blank CHECK (
        LEN(LTRIM(RTRIM(content))) > 0
    ),

    CONSTRAINT CK_notifications_target_role CHECK (
        target_role IN ('ALL', 'ADMIN_SUPER', 'ADMIN_BASE', 'BASE1', 'BASE2', 'BASE3')
    )
);
GO

CREATE INDEX IX_notifications_target_created
ON dbo.notifications(target_role, is_important DESC, created_at DESC);
GO

CREATE INDEX IX_notifications_room_created
ON dbo.notifications(target_room_id, created_at DESC);
GO


/* =========================================================
   12. DISCIPLINE POINTS
   Quan hệ:
   students 1 - n discipline_points
   users 1 - n discipline_points.created_by
   ========================================================= */

CREATE TABLE dbo.discipline_points (
    id          INT IDENTITY(1,1) NOT NULL,
    student_id  INT NOT NULL,
    points      INT NOT NULL,
    type        VARCHAR(20) NOT NULL,
    reason      NVARCHAR(255) NOT NULL,
    detail      NVARCHAR(MAX) NULL,
    created_by  INT NULL,
    created_at  DATETIME2(0) NOT NULL CONSTRAINT DF_discipline_points_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_discipline_points PRIMARY KEY (id),

    CONSTRAINT FK_discipline_points_students
        FOREIGN KEY (student_id) REFERENCES dbo.students(id)
        ON DELETE CASCADE,

    CONSTRAINT FK_discipline_points_users
        FOREIGN KEY (created_by) REFERENCES dbo.users(id)
        ON DELETE SET NULL,

    CONSTRAINT CK_discipline_points_type CHECK (
        type IN ('BONUS', 'PENALTY')
    ),

    CONSTRAINT CK_discipline_points_points CHECK (
        points > 0 AND points <= 100
    ),

    CONSTRAINT CK_discipline_points_reason_not_blank CHECK (
        LEN(LTRIM(RTRIM(reason))) > 0
    )
);
GO

CREATE INDEX IX_discipline_student_created
ON dbo.discipline_points(student_id, created_at DESC);
GO


/* =========================================================
   13. ROOM TRANSFER REQUESTS
   Quan hệ:
   students 1 - n room_transfer_requests
   rooms 1 - n room_transfer_requests.from_room_id
   rooms 1 - n room_transfer_requests.to_room_id
   users 1 - n room_transfer_requests.processed_by
   ========================================================= */

CREATE TABLE dbo.room_transfer_requests (
    id             INT IDENTITY(1,1) NOT NULL,
    student_id     INT NOT NULL,
    from_room_id   INT NULL,
    to_room_id     INT NULL,
    reason         NVARCHAR(MAX) NOT NULL,
    status         VARCHAR(20) NOT NULL CONSTRAINT DF_room_transfer_status DEFAULT ('PENDING'),
    processed_by   INT NULL,
    admin_notes    NVARCHAR(MAX) NULL,
    processed_at   DATETIME2(0) NULL,
    created_at     DATETIME2(0) NOT NULL CONSTRAINT DF_room_transfer_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_room_transfer_requests PRIMARY KEY (id),

    CONSTRAINT FK_room_transfer_students
        FOREIGN KEY (student_id) REFERENCES dbo.students(id)
        ON DELETE CASCADE,

    CONSTRAINT FK_room_transfer_from_rooms
        FOREIGN KEY (from_room_id) REFERENCES dbo.rooms(id),

    CONSTRAINT FK_room_transfer_to_rooms
        FOREIGN KEY (to_room_id) REFERENCES dbo.rooms(id),

    CONSTRAINT FK_room_transfer_processed_users
        FOREIGN KEY (processed_by) REFERENCES dbo.users(id),

    CONSTRAINT CK_room_transfer_status CHECK (
        status IN ('PENDING', 'APPROVED', 'REJECTED')
    ),

    CONSTRAINT CK_room_transfer_reason_not_blank CHECK (
        LEN(LTRIM(RTRIM(reason))) > 0
    ),

    CONSTRAINT CK_room_transfer_room_diff CHECK (
        from_room_id IS NULL
        OR to_room_id IS NULL
        OR from_room_id <> to_room_id
    )
);
GO

CREATE INDEX IX_room_transfer_status_created
ON dbo.room_transfer_requests(status, created_at DESC);
GO

CREATE INDEX IX_room_transfer_student_created
ON dbo.room_transfer_requests(student_id, created_at DESC);
GO


/* =========================================================
   14. VISITORS
   Quan hệ:
   students 1 - n visitors
   users 1 - n visitors.approved_by
   ========================================================= */

CREATE TABLE dbo.visitors (
    id                INT IDENTITY(1,1) NOT NULL,
    student_id         INT NOT NULL,
    visitor_name       NVARCHAR(150) NOT NULL,
    visitor_phone      VARCHAR(30) NULL,
    visitor_id_card    VARCHAR(30) NULL,
    visit_date         DATE NOT NULL,
    visit_time_start   TIME(0) NULL,
    visit_time_end     TIME(0) NULL,
    purpose            NVARCHAR(MAX) NULL,
    status             VARCHAR(20) NOT NULL CONSTRAINT DF_visitors_status DEFAULT ('PENDING'),
    approved_by        INT NULL,
    approved_at        DATETIME2(0) NULL,
    notes              NVARCHAR(MAX) NULL,
    created_at         DATETIME2(0) NOT NULL CONSTRAINT DF_visitors_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_visitors PRIMARY KEY (id),

    CONSTRAINT FK_visitors_students
        FOREIGN KEY (student_id) REFERENCES dbo.students(id)
        ON DELETE CASCADE,

    CONSTRAINT FK_visitors_approved_users
        FOREIGN KEY (approved_by) REFERENCES dbo.users(id),

    CONSTRAINT CK_visitors_name_not_blank CHECK (
        LEN(LTRIM(RTRIM(visitor_name))) > 0
    ),

    CONSTRAINT CK_visitors_status CHECK (
        status IN ('PENDING', 'APPROVED', 'REJECTED', 'CHECKED_IN', 'CHECKED_OUT')
    ),

    CONSTRAINT CK_visitors_time_range CHECK (
        visit_time_start IS NULL
        OR visit_time_end IS NULL
        OR visit_time_end >= visit_time_start
    )
);
GO

CREATE INDEX IX_visitors_status_visit
ON dbo.visitors(status, visit_date DESC, created_at DESC);
GO

CREATE INDEX IX_visitors_student_visit
ON dbo.visitors(student_id, visit_date DESC);
GO


/* =========================================================
   15. ROOM FUNDS
   Quan hệ:
   rooms 1 - n room_funds
   users 1 - n room_funds.created_by
   ========================================================= */

CREATE TABLE dbo.room_funds (
    id                INT IDENTITY(1,1) NOT NULL,
    room_id            INT NOT NULL,
    amount             DECIMAL(18,2) NOT NULL,
    type               VARCHAR(20) NOT NULL,
    description        NVARCHAR(255) NOT NULL,
    transaction_date   DATE NOT NULL CONSTRAINT DF_room_funds_transaction_date DEFAULT CAST(GETDATE() AS DATE),
    created_by         INT NULL,
    created_at         DATETIME2(0) NOT NULL CONSTRAINT DF_room_funds_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_room_funds PRIMARY KEY (id),

    CONSTRAINT FK_room_funds_rooms
        FOREIGN KEY (room_id) REFERENCES dbo.rooms(id)
        ON DELETE CASCADE,

    CONSTRAINT FK_room_funds_users
        FOREIGN KEY (created_by) REFERENCES dbo.users(id)
        ON DELETE SET NULL,

    CONSTRAINT CK_room_funds_type CHECK (
        type IN ('INCOME', 'EXPENSE')
    ),

    CONSTRAINT CK_room_funds_amount CHECK (
        amount > 0
    ),

    CONSTRAINT CK_room_funds_description_not_blank CHECK (
        LEN(LTRIM(RTRIM(description))) > 0
    )
);
GO

CREATE INDEX IX_room_funds_room_date
ON dbo.room_funds(room_id, transaction_date DESC, created_at DESC);
GO


/* =========================================================
   16. HANDOVER RECORDS
   Quan hệ:
   students 1 - n handover_records
   rooms 1 - n handover_records
   users 1 - n handover_records.created_by
   ========================================================= */

CREATE TABLE dbo.handover_records (
    id              INT IDENTITY(1,1) NOT NULL,
    student_id      INT NOT NULL,
    room_id         INT NOT NULL,
    type            VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL CONSTRAINT DF_handover_status DEFAULT ('SUBMITTED'),
    note            NVARCHAR(MAX) NULL,
    created_by      INT NULL,
    created_at      DATETIME2(0) NOT NULL CONSTRAINT DF_handover_created_at DEFAULT SYSDATETIME(),

    CONSTRAINT PK_handover_records PRIMARY KEY (id),

    CONSTRAINT FK_handover_students
        FOREIGN KEY (student_id) REFERENCES dbo.students(id)
        ON DELETE CASCADE,

    CONSTRAINT FK_handover_rooms
        FOREIGN KEY (room_id) REFERENCES dbo.rooms(id),

    CONSTRAINT FK_handover_users
        FOREIGN KEY (created_by) REFERENCES dbo.users(id)
        ON DELETE SET NULL,

    CONSTRAINT CK_handover_type CHECK (
        type IN ('MOVE_IN', 'MOVE_OUT')
    ),

    CONSTRAINT CK_handover_status CHECK (
        status IN ('SUBMITTED', 'REVIEWED', 'ACTIONED', 'CONFIRMED')
    )
);
GO

CREATE INDEX IX_handover_student_created
ON dbo.handover_records(student_id, created_at DESC);
GO

CREATE INDEX IX_handover_room_created
ON dbo.handover_records(room_id, created_at DESC);
GO


/* =========================================================
   17. TRIGGERS
   Đồng bộ current_occupants theo students.
   ========================================================= */

CREATE TRIGGER dbo.trg_students_recount_room_occupants
ON dbo.students
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;

    ;WITH affected_rooms AS (
        SELECT room_id FROM inserted WHERE room_id IS NOT NULL
        UNION
        SELECT room_id FROM deleted WHERE room_id IS NOT NULL
    )
    UPDATE r
    SET current_occupants = (
        SELECT COUNT(*)
        FROM dbo.students s
        WHERE s.room_id = r.id
          AND s.status = 'ACTIVE'
    )
    FROM dbo.rooms r
    INNER JOIN affected_rooms ar ON ar.room_id = r.id;
END;
GO


CREATE TRIGGER dbo.trg_incidents_resolved_date
ON dbo.incidents
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    UPDATE i
    SET resolved_date = SYSDATETIME()
    FROM dbo.incidents i
    INNER JOIN inserted ins ON i.id = ins.id
    INNER JOIN deleted del ON del.id = ins.id
    WHERE ins.status = 'RESOLVED'
      AND del.status <> 'RESOLVED'
      AND i.resolved_date IS NULL;
END;
GO


/* =========================================================
   18. SEED DATA
   Default admin:
   username: admin
   password: 123456
   SHA-256: 8d969eef...
   ========================================================= */

INSERT INTO dbo.users (
    username,
    password_hash,
    role,
    full_name,
    email,
    phone,
    is_active
)
VALUES (
    'admin',
    '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
    'ADMIN_SUPER',
    N'Quản trị viên hệ thống',
    'admin@dormassist.local',
    NULL,
    1
);
GO

INSERT INTO dbo.buildings(name, total_floors, description)
VALUES
(N'Khu A', 5, N'Khu ký túc xá A'),
(N'Khu B', 5, N'Khu ký túc xá B');
GO

INSERT INTO dbo.rooms (
    room_number,
    floor,
    building_id,
    capacity,
    current_occupants,
    status,
    rent_price,
    room_type,
    notes
)
VALUES
('A101', 1, 1, 8, 0, 'AVAILABLE', 550000, 'STANDARD', N'Phòng tiêu chuẩn'),
('A102', 1, 1, 8, 0, 'AVAILABLE', 550000, 'STANDARD', N'Phòng tiêu chuẩn'),
('B201', 2, 2, 6, 0, 'AVAILABLE', 750000, 'VIP', N'Phòng cao cấp');
GO

INSERT INTO dbo.price_config (
    electric_price,
    water_price,
    service_fee,
    effective_date,
    created_by
)
VALUES (
    3500,
    15000,
    50000,
    CAST(GETDATE() AS DATE),
    1
);
GO

INSERT INTO dbo.tokens(token_code, role, created_by, is_used)
VALUES
('SVVKU-0001', 'BASE3', 1, 0),
('TTVKU-0001', 'BASE1', 1, 0),
('TPVKU-0001', 'BASE2', 1, 0),
('ADMINVKU-0001', 'ADMIN_SUPER', 1, 0);
GO


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_students_student_code_not_blank'
      AND object_id = OBJECT_ID('dbo.students')
)
BEGIN
    CREATE UNIQUE INDEX UX_students_student_code_not_blank
    ON dbo.students(student_code)
    WHERE student_code IS NOT NULL AND student_code <> '';


IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes
    WHERE name = 'UX_students_user_id_not_null'
      AND object_id = OBJECT_ID('dbo.students')
)
BEGIN
    CREATE UNIQUE INDEX UX_students_user_id_not_null
    ON dbo.students(user_id)
    WHERE user_id IS NOT NULL;
END


CREATE LOGIN dorm_app WITH PASSWORD = 'DormApp@2026';
GO

USE dormassist;
GO

CREATE USER dorm_app FOR LOGIN dorm_app;
GO

ALTER ROLE db_datareader ADD MEMBER dorm_app;
ALTER ROLE db_datawriter ADD MEMBER dorm_app;
GO


IF OBJECT_ID('dbo.room_funds', 'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.room_funds;
    PRINT N'Đã xóa bảng dbo.room_funds';
END
ELSE
BEGIN
    PRINT N'Bảng dbo.room_funds không tồn tại';
END
GO

-- Xóa bảng biên bản bàn giao nếu tồn tại
IF OBJECT_ID('dbo.handover_records', 'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.handover_records;
    PRINT N'Đã xóa bảng dbo.handover_records';
END
ELSE
BEGIN
    PRINT N'Bảng dbo.handover_records không tồn tại';
END
GO


IF OBJECT_ID('dbo.visitors', 'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.visitors;
    PRINT N'Đã xóa bảng dbo.visitors';
END
ELSE
BEGIN
    PRINT N'Bảng dbo.visitors không tồn tại';
END
GO