-- Create invoices table
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id INT PRIMARY KEY AUTO_INCREMENT,
    reservation_id INT NOT NULL,
    invoice_date DATE NOT NULL,
    room_charges DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    additional_charges DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    payment_method VARCHAR(50),
    payment_date DATE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id) ON DELETE RESTRICT,
    INDEX idx_reservation (reservation_id),
    INDEX idx_payment_status (payment_status),
    INDEX idx_invoice_date (invoice_date)
);

-- Create invoice_items table
CREATE TABLE IF NOT EXISTS invoice_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    invoice_id INT NOT NULL,
    description VARCHAR(255) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id) ON DELETE CASCADE,
    INDEX idx_invoice (invoice_id)
);

-- Create trigger to update invoice total amount when items are added/updated/deleted
DELIMITER //
CREATE TRIGGER update_invoice_total AFTER INSERT ON invoice_items
FOR EACH ROW
BEGIN
    UPDATE invoices 
    SET total_amount = (
        SELECT room_charges + additional_charges + tax_amount + COALESCE(SUM(amount), 0)
        FROM invoice_items
        WHERE invoice_id = NEW.invoice_id
    )
    WHERE invoice_id = NEW.invoice_id;
END //

CREATE TRIGGER update_invoice_total_on_update AFTER UPDATE ON invoice_items
FOR EACH ROW
BEGIN
    UPDATE invoices 
    SET total_amount = (
        SELECT room_charges + additional_charges + tax_amount + COALESCE(SUM(amount), 0)
        FROM invoice_items
        WHERE invoice_id = NEW.invoice_id
    )
    WHERE invoice_id = NEW.invoice_id;
END //

CREATE TRIGGER update_invoice_total_on_delete AFTER DELETE ON invoice_items
FOR EACH ROW
BEGIN
    UPDATE invoices 
    SET total_amount = (
        SELECT room_charges + additional_charges + tax_amount + COALESCE(SUM(amount), 0)
        FROM invoice_items
        WHERE invoice_id = OLD.invoice_id
    )
    WHERE invoice_id = OLD.invoice_id;
END //
DELIMITER ;

-- Create view for invoice details with customer and room information
CREATE OR REPLACE VIEW invoice_details AS
SELECT 
    i.*,
    r.check_in_date,
    r.check_out_date,
    r.total_price as reservation_total,
    c.name as customer_name,
    c.email as customer_email,
    c.phone as customer_phone,
    rm.room_number,
    rm.room_type,
    rm.price as room_price
FROM invoices i
JOIN reservations r ON i.reservation_id = r.reservation_id
JOIN customers c ON r.customer_id = c.customer_id
JOIN rooms rm ON r.room_id = rm.room_id; 