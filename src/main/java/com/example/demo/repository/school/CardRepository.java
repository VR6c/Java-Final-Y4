package com.example.demo.repository.school;

import com.example.demo.entity.school.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    
    boolean existsByCardNumber(String cardNumber);
    
    boolean existsByCardNumberAndIdNot(String cardNumber, Long id);

    boolean existsByStudentId(Long studentId);

    boolean existsByStudentIdAndIdNot(Long studentId, Long id);

    @Query("SELECT c FROM Card c " +
           "LEFT JOIN c.student s " +
           "WHERE (:cardNumber IS NULL OR :cardNumber = '' OR LOWER(c.cardNumber) LIKE LOWER(CONCAT('%', :cardNumber, '%'))) AND " +
           "(:studentName IS NULL OR :studentName = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :studentName, '%')))")
    Page<Card> searchCards(
            @Param("cardNumber") String cardNumber,
            @Param("studentName") String studentName,
            Pageable pageable);
}
