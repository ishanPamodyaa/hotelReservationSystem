-- Create rooms table
CREATE TABLE IF NOT EXISTS rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10) UNIQUE NOT NULL,
    room_type ENUM('Single', 'Double', 'Twin', 'Deluxe', 'Suite') NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    status ENUM('Available', 'Occupied', 'UnderRenovation', 'Maintenance', 'OutOfService') NOT NULL DEFAULT 'Available'
);

-- Insert some sample rooms
INSERT INTO rooms (room_number, room_type, price, status) VALUES 
('101', 'Single', 5000.00, 'Available'),
('102', 'Single', 5000.00, 'Available'),
('201', 'Double', 8000.00, 'Available'),
('202', 'Double', 8000.00, 'Occupied'),
('301', 'Deluxe', 12000.00, 'Available'),
('302', 'Suite', 15000.00, 'UnderRenovation')
ON DUPLICATE KEY UPDATE room_number = room_number; 