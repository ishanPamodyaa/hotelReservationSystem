-- Create users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role ENUM('Admin', 'Manager', 'Receptionist', 'User') NOT NULL
);

-- Insert initial admin user (password: admin123)
-- Note: In a real application, use BCrypt to generate password hash
INSERT INTO users (username, password_hash, role) 
VALUES ('admin', '$2a$10$iAGZMzfCZjPxKcqqXCIeVOI2iKvEFPBu0qSiOHPZG/MmY7.ZOHn1y', 'Admin')
ON DUPLICATE KEY UPDATE username = username;

-- Create customers table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    nic VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_nic (nic),
    INDEX idx_name (name)
); 