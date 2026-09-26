package com.example.demo.repository.product;

import com.example.demo.entity.product.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByEmailId(Long emailId);

    boolean existsByEmailIdAndIdNot(Long emailId, Long customerId);

    Optional<Customer> findByEmailId(Long emailId);

    @Query("SELECT c FROM Customer c WHERE " +
           "(:name IS NULL OR :name = '' OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:phone IS NULL OR :phone = '' OR c.phoneNumber LIKE CONCAT('%', :phone, '%'))")
    Page<Customer> searchCustomers(
            @Param("name") String name,
            @Param("phone") String phone,
            Pageable pageable);
}
