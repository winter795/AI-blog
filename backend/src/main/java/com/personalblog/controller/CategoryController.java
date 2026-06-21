package com.personalblog.controller;

import com.personalblog.dto.Result;
import com.personalblog.entity.Category;
import com.personalblog.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.list());
    }

    @PostMapping
    public Result<Void> save(@RequestBody Category category) {
        categoryService.save(category);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Category category) {
        categoryService.update(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
