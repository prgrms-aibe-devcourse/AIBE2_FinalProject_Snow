package com.snow.popin.domain.category.repository;

import com.snow.popin.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c  ORDER BY c.id ASC")
    List<Category> findAllOrderById();
}