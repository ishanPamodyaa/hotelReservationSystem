package edu.icet.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    private int customerId;
    private String name;
    private String nic;
    private String email;
    private String phone;
    private String address;
    private String createdAt;
    private String updatedAt;
} 