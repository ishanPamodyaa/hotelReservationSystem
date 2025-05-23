package edu.icet.Repocitory;

import edu.icet.Model.Customer;
import java.util.List;

public interface CustomerRepository {
    boolean save(Customer customer);
    boolean update(Customer customer);
    boolean delete(int id);
    Customer findById(int id);
    Customer findByNic(String nic);
    List<Customer> findAll();
    List<Customer> searchByNameOrNic(String searchTerm);
} 