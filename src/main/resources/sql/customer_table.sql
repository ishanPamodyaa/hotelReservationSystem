-- Create customers table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    nic_number VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    phone VARCHAR(20) NOT NULL,
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
); 

INSERT INTO customers (name, nic_number, email, phone, address)
VALUES 
('Kasun Perera', '901234567V', 'kasun@example.com', '0771234567', 'No 10, Galle Road, Colombo'),
('Nimali Silva', '911234568V', 'nimali@example.com', '0772345678', 'No 15, Kandy Road, Kandy'),
('Amal Jayasuriya', '921234569V', 'amal@example.com', '0773456789', 'No 21, Beach Road, Matara'),
('Saman Kumara', '931234570V', 'saman@example.com', '0774567890', 'No 30, Lake Road, Kurunegala'),
('Iresha Fernando', '941234571V', 'iresha@example.com', '0775678901', 'No 12, Temple Road, Negombo'),
('Dilan Weerasinghe', '951234572V', 'dilan@example.com', '0776789012', 'No 25, Hill Street, Nuwara Eliya'),
('Tharuka Rajapaksha', '961234573V', 'tharuka@example.com', '0777890123', 'No 9, River View, Gampaha'),
('Malsha Gunawardena', '971234574V', 'malsha@example.com', '0778901234', 'No 18, Forest Lane, Badulla'),
('Nuwan Siriwardena', '981234575V', 'nuwan@example.com', '0779012345', 'No 33, Garden Street, Ratnapura'),
('Harsha Dissanayake', '991234576V', 'harsha@example.com', '0770123456', 'No 6, Main Street, Anuradhapura');
