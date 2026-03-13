package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.entity.DishSpecification;
import com.restaurant.ordering.model.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DishSpecificationRepository extends JpaRepository<DishSpecification, Long> {

    List<DishSpecification> findByMenuItemOrderBySortOrderAsc(MenuItem menuItem);

    List<DishSpecification> findByMenuItemAndIsDefaultTrue(MenuItem menuItem);
}