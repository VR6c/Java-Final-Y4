package com.example.demo.repository.product;

import com.example.demo.entity.product.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailRepository extends JpaRepository<Email, Long> {

    Optional<Email> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @Query("SELECT e FROM Email e WHERE " +
           "(:email IS NULL OR :email = '' OR LOWER(e.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<Email> searchEmails(@Param("email") String email, Pageable pageable);
}
