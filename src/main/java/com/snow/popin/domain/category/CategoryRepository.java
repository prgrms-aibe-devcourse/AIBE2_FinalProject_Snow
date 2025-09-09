package com.snow.popin.domain.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findBySlug(String slug);

    @Query("SELECT c FROM Category c  ORDER BY c.id ASC")
    List<Category> findAllActiveOrderByDisplayOrder();

    boolean existsBySlug(String slug);

    boolean existsByName(String name);
}