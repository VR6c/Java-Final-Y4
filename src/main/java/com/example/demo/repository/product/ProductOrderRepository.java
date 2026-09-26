package com.example.demo.repository.product;

import com.example.demo.entity.product.ProductOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductOrderRepository extends JpaRepository<ProductOrder, Long> {

    List<ProductOrder> findByOrderId(Long orderId);

    List<ProductOrder> findByProductId(Long productId);
}
