package com.restaurant.ordering.repository;

import com.restaurant.ordering.model.entity.Cart;
import com.restaurant.ordering.model.entity.CartItem;
import com.restaurant.ordering.model.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCart(Cart cart);

    Optional<CartItem> findByCartAndMenuItem(Cart cart, MenuItem menuItem);

    void deleteByCart(Cart cart);

    long countByCart(Cart cart);
}