package com.restaurant.ordering.controller.api;

import com.restaurant.ordering.model.dto.request.MenuItemRequest;
import com.restaurant.ordering.model.dto.response.ApiResponse;
import com.restaurant.ordering.model.dto.response.MenuItemResponse;
import com.restaurant.ordering.model.entity.Category;
import com.restaurant.ordering.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    public ApiResponse<List<Category>> getCategories() {
        List<Category> categories = menuService.getAllActiveCategories();
        return ApiResponse.success(categories);
    }

    @GetMapping("/items")
    public ApiResponse<Page<MenuItemResponse>> getMenuItems(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 前端分页从1开始，Spring Data分页从0开始，需要转换
        int pageNumber = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageNumber, size);
        Page<MenuItemResponse> items;

        if (categoryId != null) {
            items = menuService.getMenuItemsByCategory(categoryId, pageable);
        } else {
            // 获取所有菜品
            items = menuService.getAllMenuItems(pageable);
        }

        return ApiResponse.success(items);
    }

    @GetMapping("/items/search")
    public ApiResponse<Page<MenuItemResponse>> searchMenuItems(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 前端分页从1开始，Spring Data分页从0开始，需要转换
        int pageNumber = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageNumber, size);
        Page<MenuItemResponse> items = menuService.searchMenuItems(keyword, pageable);
        return ApiResponse.success(items);
    }

    // 管理端：获取所有菜品（包括停售的）
    @GetMapping("/admin/items")
    public ApiResponse<Page<MenuItemResponse>> getAllMenuItemsForAdmin(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {

        int pageNumber = page > 0 ? page - 1 : 0;
        Pageable pageable = PageRequest.of(pageNumber, size);
        Page<MenuItemResponse> items;

        if (keyword != null && !keyword.isEmpty()) {
            items = menuService.searchAllMenuItems(keyword, pageable);
        } else if (categoryId != null) {
            items = menuService.getMenuItemsByCategoryIncludingInactive(categoryId, pageable);
        } else {
            items = menuService.getAllMenuItemsIncludingInactive(pageable);
        }

        return ApiResponse.success(items);
    }

    @GetMapping("/items/recommended")
    public ApiResponse<List<MenuItemResponse>> getRecommendedItems() {
        List<MenuItemResponse> items = menuService.getRecommendedItems();
        return ApiResponse.success(items);
    }

    @GetMapping("/items/hot")
    public ApiResponse<List<MenuItemResponse>> getHotItems() {
        List<MenuItemResponse> items = menuService.getHotItems();
        return ApiResponse.success(items);
    }

    @GetMapping("/items/{id}")
    public ApiResponse<MenuItemResponse> getMenuItemDetail(@PathVariable Long id) {
        MenuItemResponse item = menuService.getMenuItemDetail(id);
        return ApiResponse.success(item);
    }

    @PostMapping("/items")
    public ApiResponse<MenuItemResponse> createMenuItem(@RequestBody MenuItemRequest request) {
        MenuItemResponse item = menuService.createMenuItem(request);
        return ApiResponse.success(item);
    }

    @PutMapping("/items/{id}")
    public ApiResponse<MenuItemResponse> updateMenuItem(@PathVariable Long id, @RequestBody MenuItemRequest request) {
        MenuItemResponse item = menuService.updateMenuItem(id, request);
        return ApiResponse.success(item);
    }

    @DeleteMapping("/items/{id}")
    public ApiResponse<Void> deleteMenuItem(@PathVariable Long id) {
        menuService.deleteMenuItem(id);
        return ApiResponse.success(null);
    }

    @PutMapping("/items/{id}/image")
    public ApiResponse<Void> updateMenuItemImage(@PathVariable Long id, @RequestParam String imageUrl) {
        menuService.updateMenuItemImage(id, imageUrl);
        return ApiResponse.success(null);
    }

    @GetMapping("/featured")
    public ApiResponse<List<MenuItemResponse>> getFeaturedItems() {
        List<MenuItemResponse> items = menuService.getRecommendedItems();
        return ApiResponse.success(items);
    }
}