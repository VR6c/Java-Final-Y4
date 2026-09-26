package com.example.demo.repository.school;

import com.example.demo.entity.school.Teacher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    
    @Query("SELECT t FROM Teacher t WHERE " +
           "(:name IS NULL OR :name = '' OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:subject IS NULL OR :subject = '' OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :subject, '%')))")
    Page<Teacher> searchTeachers(
            @Param("name") String name,
            @Param("subject") String subject,
            Pageable pageable);
}
