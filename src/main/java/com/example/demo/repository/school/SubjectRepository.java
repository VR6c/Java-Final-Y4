package com.example.demo.repository.school;

import com.example.demo.entity.school.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("SELECT s FROM Subject s WHERE " +
           "(:name IS NULL OR :name = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:description IS NULL OR :description = '' OR LOWER(s.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<Subject> searchSubjects(
            @Param("name") String name,
            @Param("description") String description,
            Pageable pageable);
}
