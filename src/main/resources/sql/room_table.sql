CREATE TABLE IF NOT EXISTS rooms (
    room_id INT AUTO_INCREMENT PRIMARY KEY,
    room_number VARCHAR(10) UNIQUE NOT NULL,
    room_type ENUM('Single', 'Double', 'Twin', 'Deluxe', 'Suite') NOT NULL,
    price_per_night DECIMAL(10, 2) NOT NULL,
    status ENUM('Available', 'Occupied', 'UnderRenovation', 'Maintenance', 'OutOfService') NOT NULL DEFAULT 'Available',
    capacity INT NOT NULL,
    description TEXT
);

INSERT INTO rooms (room_number, room_type, price_per_night, status, capacity, description) VALUES 
('101', 'Single', 5000.00, 'Available', 1, 'Single bed with garden view'),
('102', 'Single', 5000.00, 'Available', 1, 'Compact single room near lobby'),
('201', 'Double', 8000.00, 'Available', 2, 'Double room with balcony'),
('202', 'Double', 8000.00, 'Occupied', 2, 'Double bed, lake view'),
('301', 'Deluxe', 12000.00, 'Available', 3, 'Deluxe room with mini bar'),
('302', 'Suite', 15000.00, 'UnderRenovation', 4, 'Luxury suite with two bedrooms'),
('303', 'Suite', 15500.00, 'Available', 4, 'Top floor suite with panoramic view'),
('401', 'Twin', 8500.00, 'Maintenance', 2, 'Two single beds, garden side'),
('402', 'Twin', 8500.00, 'Available', 2, 'Modern twin room, ground floor'),
('403', 'Deluxe', 12500.00, 'OutOfService', 3, 'Deluxe room under inspection')
ON DUPLICATE KEY UPDATE room_number = room_number;

