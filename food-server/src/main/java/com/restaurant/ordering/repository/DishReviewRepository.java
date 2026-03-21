package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.entity.DishReview;
import com.restaurant.ordering.model.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishReviewRepository extends JpaRepository<DishReview, Long> {

    List<DishReview> findByMenuItemOrderByCreatedAtDesc(MenuItem menuItem);

    Page<DishReview> findByMenuItemOrderByCreatedAtDesc(MenuItem menuItem, Pageable pageable);

    boolean existsByOrderItemId(Long orderItemId);

    long countByMenuItem(MenuItem menuItem);

    java.util.Optional<DishReview> findByOrderItemId(Long orderItemId);

    List<DishReview> findByMenuItemIdOrderByCreatedAtDesc(Long menuItemId);

    List<DishReview> findAllByOrderByCreatedAtDesc();
}