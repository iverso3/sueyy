package com.restaurant.ordering.service;

import com.restaurant.ordering.model.dto.request.MenuItemRequest;
import com.restaurant.ordering.model.dto.response.MenuItemResponse;
import com.restaurant.ordering.model.entity.Category;
import com.restaurant.ordering.model.entity.MenuItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MenuService {

    List<Category> getAllActiveCategories();

    Page<MenuItemResponse> getMenuItemsByCategory(Long categoryId, Pageable pageable);

    Page<MenuItemResponse> getAllMenuItems(Pageable pageable);

    Page<MenuItemResponse> getAllMenuItemsIncludingInactive(Pageable pageable);

    Page<MenuItemResponse> getMenuItemsByCategoryIncludingInactive(Long categoryId, Pageable pageable);

    Page<MenuItemResponse> searchMenuItems(String keyword, Pageable pageable);

    Page<MenuItemResponse> searchAllMenuItems(String keyword, Pageable pageable);

    List<MenuItemResponse> getRecommendedItems();

    List<MenuItemResponse> getHotItems();

    MenuItemResponse getMenuItemDetail(Long id);

    void updateMenuItemImage(Long id, String imageUrl);

    MenuItemResponse createMenuItem(MenuItemRequest request);

    MenuItemResponse updateMenuItem(Long id, MenuItemRequest request);

    void deleteMenuItem(Long id);
}