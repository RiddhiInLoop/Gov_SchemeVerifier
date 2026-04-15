-- database/schema.sql
-- Create database
CREATE DATABASE IF NOT EXISTS scheme_verifier_db;
USE scheme_verifier_db;
-- Citizen Table
CREATE TABLE IF NOT EXISTS Citizen (
    citizen_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    middle_name VARCHAR(100),
    last_name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    gender ENUM('Male', 'Female', 'Other') NOT NULL,
    category ENUM('General', 'SC', 'ST', 'OBC', 'Others') NOT NULL,
    citizenship VARCHAR(100) NOT NULL DEFAULT 'Indian',
    income DECIMAL(15, 2) NOT NULL,
    residence_type ENUM('Owned', 'Rented', 'Other') NOT NULL,
    area VARCHAR(200) NOT NULL,
    landmark VARCHAR(200),
    pin_code VARCHAR(20) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);
-- Scheme Table
CREATE TABLE IF NOT EXISTS Scheme (
    scheme_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    min_age INT,
    max_age INT,
    income_limit DECIMAL(15, 2),
    gender_req ENUM('Male', 'Female', 'All') DEFAULT 'All',
    category_req ENUM('General', 'SC', 'ST', 'OBC', 'Others', 'All') DEFAULT 'All',
    citizenship_req VARCHAR(100) DEFAULT 'Indian'
);
-- Application Table
CREATE TABLE IF NOT EXISTS Application (
    application_id INT AUTO_INCREMENT PRIMARY KEY,
    status VARCHAR(50) DEFAULT 'Pending',
    applied_date DATE NOT NULL,
    rejection_reason TEXT,
    citizen_id INT NOT NULL,
    scheme_id INT NOT NULL,
    FOREIGN KEY (citizen_id) REFERENCES Citizen(citizen_id) ON DELETE CASCADE,
    FOREIGN KEY (scheme_id) REFERENCES Scheme(scheme_id) ON DELETE CASCADE
);
-- Admin Table
CREATE TABLE IF NOT EXISTS Admin (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);
-- Insert Sample Data for Schemes
INSERT INTO Scheme (
        name,
        description,
        min_age,
        max_age,
        income_limit,
        gender_req,
        category_req
    )
VALUES (
        'PM-KISAN (Pradhan Mantri Kisan Samman Nidhi)',
        'Financial assistance of ₹6,000/year for small & marginal farmers. Excludes high-income individuals.',
        NULL,
        NULL,
        NULL,
        'All',
        'All'
    ),
    (
        'Ayushman Bharat (PM-JAY)',
        'Health cover up to ₹5 lakh for BPL/SECC low-income families.',
        NULL,
        NULL,
        100000.00,
        'All',
        'All'
    ),
    (
        'PM Awas Yojana (PMAY)',
        'Housing subsidy (₹1.2-₹2.5 lakh) for homeless and economically weaker sections.',
        18,
        NULL,
        300000.00,
        'All',
        'All'
    ),
    (
        'PM Matru Vandana Yojana (PMMVY)',
        'Maternal health benefit of ₹5,000 for pregnant women (first child).',
        18,
        45,
        NULL,
        'Female',
        'All'
    ),
    (
        'Atal Pension Yojana (APY)',
        'Pension (₹1,000-₹5,000/month) for unorganised sector workers after age 60.',
        18,
        40,
        NULL,
        'All',
        'All'
    ),
    (
        'PM Jeevan Jyoti Bima Yojana (PMJJBY)',
        'Life insurance cover of ₹2 lakh for bank account holders.',
        18,
        50,
        NULL,
        'All',
        'All'
    ),
    (
        'PM Suraksha Bima Yojana (PMSBY)',
        'Accidental insurance cover of ₹2 lakh for bank account holders.',
        18,
        70,
        NULL,
        'All',
        'All'
    ),
    (
        'MGNREGA',
        '100 days wage employment for willing rural households.',
        18,
        NULL,
        NULL,
        'All',
        'All'
    ),
    (
        'Mukhyamantri Majhi Ladki Bahin Yojana',
        '₹1,500-₹2,000/month for Maharashtra women residents in non-taxpayer families.',
        21,
        65,
        250000.00,
        'Female',
        'All'
    ),
    (
        'Post-Matric Scholarship (SC/ST/OBC/VJNT)',
        'Fee reimbursement and allowance for reserved category students.',
        NULL,
        NULL,
        800000.00,
        'All',
        'All'
    );
-- Insert Default Admin
INSERT INTO Admin (username, password)
VALUES ('admin@gov.in', 'admin123');