package com.example.demo.repository.product;

import com.example.demo.entity.product.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByCustomerId(Long customerId);

    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR :status = '' OR LOWER(o.status) = LOWER(:status)) AND " +
           "(:customerId IS NULL OR o.customer.id = :customerId)")
    Page<Order> searchOrders(
            @Param("status") String status,
            @Param("customerId") Long customerId,
            Pageable pageable);
}
