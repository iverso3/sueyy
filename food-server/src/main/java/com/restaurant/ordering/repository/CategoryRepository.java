package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByIsActiveTrueOrderBySortOrderAsc();

    long countByIsActive(Boolean isActive);
}