package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.entity.Category;
import com.restaurant.ordering.model.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByCategoryAndIsActiveTrueOrderBySortOrderAsc(Category category);

    List<MenuItem> findByIsRecommendedTrueAndIsActiveTrueOrderBySortOrderAsc();

    List<MenuItem> findByIsHotTrueAndIsActiveTrueOrderBySortOrderAsc();

    Page<MenuItem> findByIsActiveTrue(Pageable pageable);

    Page<MenuItem> findByCategoryAndIsActiveTrue(Category category, Pageable pageable);

    // 获取所有菜品（包括停售的）
    Page<MenuItem> findAllByOrderBySortOrderAsc(Pageable pageable);

    // 根据分类获取所有菜品（包括停售的）
    Page<MenuItem> findByCategoryOrderBySortOrderAsc(Category category, Pageable pageable);

    @Query("SELECT m FROM MenuItem m WHERE m.isActive = true AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<MenuItem> searchActiveItems(@Param("keyword") String keyword, Pageable pageable);

    // 搜索所有菜品（包括停售的）
    @Query("SELECT m FROM MenuItem m WHERE " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<MenuItem> searchAllItems(@Param("keyword") String keyword, Pageable pageable);

    long countByCategoryAndIsActive(Category category, Boolean isActive);

    long countByIsActive(Boolean isActive);
}