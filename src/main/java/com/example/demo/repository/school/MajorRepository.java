package com.example.demo.repository.school;

import com.example.demo.entity.school.Major;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MajorRepository extends JpaRepository<Major, Long> {

    @Query("SELECT COUNT(m) > 0 FROM Major m WHERE LOWER(TRIM(m.name)) = LOWER(TRIM(:name))")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    @Query("SELECT COUNT(m) > 0 FROM Major m WHERE LOWER(TRIM(m.name)) = LOWER(TRIM(:name)) AND m.id != :id")
    boolean existsByNameIgnoreCaseAndIdNot(@Param("name") String name, @Param("id") Long id);

    @Query("SELECT m FROM Major m WHERE " +
           "(:name IS NULL OR :name = '' OR LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:description IS NULL OR :description = '' OR LOWER(m.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<Major> searchMajors(
            @Param("name") String name,
            @Param("description") String description,
            Pageable pageable);
}
