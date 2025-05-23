package edu.icet.Repocitory.impl;

import edu.icet.Db.DBConnection;
import edu.icet.Model.Customer;
import edu.icet.Repocitory.CustomerRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepositoryImpl implements CustomerRepository {
    
    @Override
    public boolean save(Customer customer) {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "INSERT INTO customers (name, nic_number, email, phone, address) VALUES (?, ?, ?, ?, ?)";
            stmt = connection.prepareStatement(sql);
            
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getNic());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getAddress());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public boolean update(Customer customer) {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "UPDATE customers SET name=?, nic_number=?, email=?, phone=?, address=? WHERE customer_id=?";
            stmt = connection.prepareStatement(sql);
            
            stmt.setString(1, customer.getName());
            stmt.setString(2, customer.getNic());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhone());
            stmt.setString(5, customer.getAddress());
            stmt.setInt(6, customer.getCustomerId());
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public boolean delete(int id) {
        Connection connection = null;
        PreparedStatement stmt = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "DELETE FROM customers WHERE customer_id=?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    @Override
    public Customer findById(int id) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT * FROM customers WHERE customer_id=?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractCustomerFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
    @Override
    public Customer findByNic(String nic) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT * FROM customers WHERE nic_number=?";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, nic);
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractCustomerFromResultSet(rs);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }
    
    @Override
    public List<Customer> findAll() {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Customer> customers = new ArrayList<>();
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT * FROM customers ORDER BY name";
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                customers.add(extractCustomerFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return customers;
    }
    
    @Override
    public List<Customer> searchByNameOrNic(String searchTerm) {
        Connection connection = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Customer> customers = new ArrayList<>();
        
        try {
            connection = DBConnection.getInstance().getConnection();
            String sql = "SELECT * FROM customers WHERE name LIKE ? OR nic_number LIKE ? ORDER BY name";
            stmt = connection.prepareStatement(sql);
            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            rs = stmt.executeQuery();
            
            while (rs.next()) {
                customers.add(extractCustomerFromResultSet(rs));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return customers;
    }
    
    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException {
        return new Customer(
            rs.getInt("customer_id"),
            rs.getString("name"),
            rs.getString("nic_number"),
            rs.getString("email"),
            rs.getString("phone"),
            rs.getString("address"),
            rs.getString("created_at"),
            rs.getString("updated_at")
        );
    }
} 