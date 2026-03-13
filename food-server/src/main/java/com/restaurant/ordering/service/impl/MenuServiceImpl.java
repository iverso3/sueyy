package com.restaurant.ordering.service.impl;

import com.restaurant.ordering.exception.BusinessException;
import com.restaurant.ordering.exception.ErrorCode;
import com.restaurant.ordering.model.dto.request.MenuItemRequest;
import com.restaurant.ordering.model.dto.response.MenuItemResponse;
import com.restaurant.ordering.model.entity.Category;
import com.restaurant.ordering.model.entity.MenuItem;
import com.restaurant.ordering.repository.CategoryRepository;
import com.restaurant.ordering.repository.MenuItemRepository;
import com.restaurant.ordering.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    @Override
    @Cacheable(value = "categories", key = "'allActive'")
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    @Override
    @Cacheable(value = "menuItems", key = "'category_' + #categoryId + '_page_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public Page<MenuItemResponse> getMenuItemsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "分类不存在"));

        Page<MenuItem> menuItems = menuItemRepository.findByCategoryAndIsActiveTrue(category, pageable);

        return menuItems.map(this::convertToResponse);
    }

    @Override
    @Cacheable(value = "menuItems", key = "'all_' + #pageable.pageNumber + '_size_' + #pageable.pageSize")
    public Page<MenuItemResponse> getAllMenuItems(Pageable pageable) {
        Page<MenuItem> menuItems = menuItemRepository.findByIsActiveTrue(pageable);
        return menuItems.map(this::convertToResponse);
    }

    @Override
    public Page<MenuItemResponse> getAllMenuItemsIncludingInactive(Pageable pageable) {
        Page<MenuItem> menuItems = menuItemRepository.findAllByOrderBySortOrderAsc(pageable);
        return menuItems.map(this::convertToResponse);
    }

    @Override
    public Page<MenuItemResponse> getMenuItemsByCategoryIncludingInactive(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "分类不存在"));
        Page<MenuItem> menuItems = menuItemRepository.findByCategoryOrderBySortOrderAsc(category, pageable);
        return menuItems.map(this::convertToResponse);
    }

    @Override
    public Page<MenuItemResponse> searchMenuItems(String keyword, Pageable pageable) {
        Page<MenuItem> menuItems = menuItemRepository.searchActiveItems(keyword, pageable);
        return menuItems.map(this::convertToResponse);
    }

    @Override
    public Page<MenuItemResponse> searchAllMenuItems(String keyword, Pageable pageable) {
        Page<MenuItem> menuItems = menuItemRepository.searchAllItems(keyword, pageable);
        return menuItems.map(this::convertToResponse);
    }

    @Override
    @Cacheable(value = "menuItems", key = "'recommended'")
    public List<MenuItemResponse> getRecommendedItems() {
        List<MenuItem> menuItems = menuItemRepository.findByIsRecommendedTrueAndIsActiveTrueOrderBySortOrderAsc();
        return menuItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "menuItems", key = "'hot'")
    public List<MenuItemResponse> getHotItems() {
        List<MenuItem> menuItems = menuItemRepository.findByIsHotTrueAndIsActiveTrueOrderBySortOrderAsc();
        return menuItems.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MenuItemResponse getMenuItemDetail(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));
        return convertToResponse(menuItem);
    }

    @Override
    public void updateMenuItemImage(Long id, String imageUrl) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND));
        menuItem.setImageUrl(imageUrl);
        menuItemRepository.save(menuItem);
    }

    private MenuItemResponse convertToResponse(MenuItem menuItem) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(menuItem.getId());
        response.setName(menuItem.getName());
        response.setDescription(menuItem.getDescription());
        response.setPrice(menuItem.getPrice());
        response.setOriginalPrice(menuItem.getOriginalPrice());
        response.setImageUrl(menuItem.getImageUrl());
        response.setIsRecommended(menuItem.getIsRecommended());
        response.setIsHot(menuItem.getIsHot());
        response.setStock(menuItem.getStock());
        response.setSortOrder(menuItem.getSortOrder());
        response.setIsActive(menuItem.getIsActive());
        response.setCategoryId(menuItem.getCategory().getId());
        response.setCategoryName(menuItem.getCategory().getName());
        return response;
    }

    @Override
    @CacheEvict(value = {"menuItems", "categories"}, allEntries = true)
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "分类不存在"));

        MenuItem menuItem = new MenuItem();
        menuItem.setCategory(category);
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setOriginalPrice(request.getOriginalPrice());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setStock(request.getStock() != null ? request.getStock() : -1);
        menuItem.setIsRecommended(request.getIsRecommended() != null ? request.getIsRecommended() : false);
        menuItem.setIsHot(request.getIsHot() != null ? request.getIsHot() : false);
        menuItem.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        menuItem.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        MenuItem saved = menuItemRepository.save(menuItem);
        return convertToResponse(saved);
    }

    @Override
    @CacheEvict(value = {"menuItems", "categories"}, allEntries = true)
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND, "菜品不存在"));

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND, "分类不存在"));
            menuItem.setCategory(category);
        }

        if (request.getName() != null) {
            menuItem.setName(request.getName());
        }
        if (request.getDescription() != null) {
            menuItem.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            menuItem.setPrice(request.getPrice());
        }
        if (request.getOriginalPrice() != null) {
            menuItem.setOriginalPrice(request.getOriginalPrice());
        }
        if (request.getImageUrl() != null) {
            menuItem.setImageUrl(request.getImageUrl());
        }
        if (request.getStock() != null) {
            menuItem.setStock(request.getStock());
        }
        if (request.getIsRecommended() != null) {
            menuItem.setIsRecommended(request.getIsRecommended());
        }
        if (request.getIsHot() != null) {
            menuItem.setIsHot(request.getIsHot());
        }
        if (request.getSortOrder() != null) {
            menuItem.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            menuItem.setIsActive(request.getIsActive());
        }

        MenuItem updated = menuItemRepository.save(menuItem);
        return convertToResponse(updated);
    }

    @Override
    @CacheEvict(value = {"menuItems", "categories"}, allEntries = true)
    public void deleteMenuItem(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.MENU_ITEM_NOT_FOUND, "菜品不存在"));
        // 逻辑删除：将isActive设为false，而不是真正删除
        menuItem.setIsActive(false);
        menuItemRepository.save(menuItem);
    }
}