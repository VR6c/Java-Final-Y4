package com.example.demo.entity.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long id;

    @Column(name = "customer_name", length = 30, nullable = false)
    @NotBlank(message = "Customer name is required")
    @Size(max = 30, message = "Customer name must not exceed 30 characters")
    private String customerName;

    @Column(length = 20)
    @Size(max = 20, message = "Address must not exceed 20 characters")
    private String address;

    @Column(name = "phone_number", length = 12)
    @Size(max = 12, message = "Phone number must not exceed 12 characters")
    private String phoneNumber;

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = com.example.demo.util.PhoneNumberUtils.formatPhoneNumber(phoneNumber);
    }

    @PrePersist
    @PreUpdate
    public void formatPhoneNumber() {
        if (this.phoneNumber != null) {
            this.phoneNumber = com.example.demo.util.PhoneNumberUtils.formatPhoneNumber(this.phoneNumber);
        }
    }

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "email_id", unique = true)
    private Email email;

    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();
}
