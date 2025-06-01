-- Create database
CREATE DATABASE IF NOT EXISTS hotel_reservation_sys_db;
USE hotel_reservation_sys_db;

-- Update rooms table to match existing schema
CREATE TABLE IF NOT EXISTS rooms (
    room_id INT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(10) UNIQUE NOT NULL,
    room_type ENUM('Single', 'Double', 'Twin', 'Deluxe', 'Suite') NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    status ENUM('Available', 'Occupied', 'UnderRenovation', 'Maintenance', 'OutOfService') NOT NULL DEFAULT 'Available',
    capacity INT NOT NULL,
    description TEXT
);

-- Create or update payments table
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATETIME DEFAULT CURRENT_TIMESTAMP,
    payment_method VARCHAR(50),
    FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id)
);

-- Insert sample room data if not exists
INSERT IGNORE INTO rooms (room_number, room_type, price_per_night, status, capacity, description) 
VALUES 
('101', 'Single', 100.00, 'Available', 1, 'Comfortable single room'),
('102', 'Double', 150.00, 'Occupied', 2, 'Spacious double room'),
('103', 'Twin', 160.00, 'Available', 2, 'Twin room with separate beds'),
('201', 'Deluxe', 200.00, 'Available', 2, 'Luxury deluxe room'),
('202', 'Suite', 300.00, 'Available', 4, 'Premium suite with living area'),
('203', 'Suite', 350.00, 'Occupied', 4, 'Executive suite with city view');

-- Insert sample customer if not exists
INSERT IGNORE INTO customers (name, nic_number, email, phone, address)
VALUES 
('John Doe', 'NIC123456', 'john@example.com', '1234567890', '123 Main St'),
('Jane Smith', 'NIC789012', 'jane@example.com', '0987654321', '456 Oak Ave');

-- Insert sample reservation if not exists
INSERT IGNORE INTO reservations (customer_id, room_id, check_in_date, check_out_date, num_guests, total_price, status, payment_status, special_requests)
SELECT 
    c.customer_id,
    r.room_id,
    CURRENT_DATE,
    DATE_ADD(CURRENT_DATE, INTERVAL 3 DAY),
    2,
    r.price_per_night * 3,
    'Confirmed',
    'Pending',
    'Extra towels please'
FROM customers c
JOIN rooms r ON r.status = 'Available'
WHERE c.name = 'John Doe'
LIMIT 1;

-- Insert sample invoice if not exists
INSERT IGNORE INTO invoices (reservation_id, invoice_date, room_charges, additional_charges, tax_amount, total_amount, payment_status)
SELECT 
    reservation_id,
    CURRENT_DATE,
    total_price,
    50.00,
    (total_price + 50.00) * 0.1,
    (total_price + 50.00) * 1.1,
    'PENDING'
FROM reservations
LIMIT 1;

-- Insert sample payment for today if not exists
INSERT IGNORE INTO payments (invoice_id, amount, payment_date, payment_method)
SELECT 
    invoice_id,
    total_amount * 0.5,
    CURRENT_TIMESTAMP,
    'Credit Card'
FROM invoices
LIMIT 1; 