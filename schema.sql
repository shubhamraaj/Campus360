-- Enable Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- Users
CREATE TABLE IF NOT EXISTS users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL CHECK (role IN ('STUDENT', 'FACULTY', 'ADMIN', 'SECURITY', 'CANTEEN_STAFF', 'PARENT')),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Students
CREATE TABLE IF NOT EXISTS students (
    student_id UUID PRIMARY KEY REFERENCES users(user_id) ON DELETE CASCADE,
    enrollment_no VARCHAR(50) UNIQUE NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 10),
    division VARCHAR(10) NOT NULL,
    apaar_id VARCHAR(12) UNIQUE,
    abc_account_id VARCHAR(20) UNIQUE,
    total_cumulative_credits INT DEFAULT 0,
    wallet_balance DECIMAL(10,2) DEFAULT 0.00
);

-- PostGIS Campus Locations
CREATE TABLE IF NOT EXISTS campus_locations (
    location_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    building_name VARCHAR(100) NOT NULL,
    floor_number INT NOT NULL,
    room_number VARCHAR(50) NOT NULL,
    category VARCHAR(50) NOT NULL,
    capacity INT NOT NULL,
    current_status VARCHAR(20) DEFAULT 'AVAILABLE',
    geom GEOMETRY(Point, 4326) NOT NULL
);

CREATE INDEX idx_campus_locations_geom ON campus_locations USING GIST (geom);

-- Exam Results
CREATE TABLE IF NOT EXISTS exam_results (
    result_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID REFERENCES students(student_id) ON DELETE CASCADE,
    course_code VARCHAR(20) NOT NULL,
    semester INT NOT NULL,
    academic_year VARCHAR(10) NOT NULL,
    internal_marks DECIMAL(5,2),
    external_marks DECIMAL(5,2),
    total_marks DECIMAL(5,2),
    grade_point INT CHECK (grade_point BETWEEN 0 AND 10),
    letter_grade VARCHAR(5),
    credits_earned INT,
    abc_push_status VARCHAR(20) DEFAULT 'PENDING'
);

-- Certificates
CREATE TABLE IF NOT EXISTS certificate_requests (
    request_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID REFERENCES students(student_id) ON DELETE CASCADE,
    certificate_type VARCHAR(50) NOT NULL,
    purpose TEXT NOT NULL,
    delivery_mode VARCHAR(30) NOT NULL,
    overall_status VARCHAR(30) DEFAULT 'CLEARANCE_IN_PROGRESS',
    pickup_counter_info VARCHAR(150),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Facial Attendance Logs
CREATE TABLE IF NOT EXISTS facial_attendance_logs (
    log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID REFERENCES students(student_id) ON DELETE CASCADE,
    course_code VARCHAR(20) NOT NULL,
    confidence_score DECIMAL(5,2),
    liveness_verified BOOLEAN DEFAULT TRUE,
    captured_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);