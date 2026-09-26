package com.example.demo.repository.school;

import com.example.demo.entity.school.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    @Query("SELECT s FROM Student s WHERE " +
           "(:name IS NULL OR :name = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:minAge IS NULL OR s.age >= :minAge) AND " +
           "(:maxAge IS NULL OR s.age <= :maxAge)")
    Page<Student> searchStudents(
            @Param("name") String name,
            @Param("minAge") Integer minAge,
            @Param("maxAge") Integer maxAge,
            Pageable pageable);

    java.util.List<Student> findByMajorId(Long majorId);
}
