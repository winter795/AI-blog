package com.personalblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalblog.entity.Category;
import com.personalblog.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> list() {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSortOrder);
        return categoryMapper.selectList(wrapper);
    }

    public void save(Category category) {
        categoryMapper.insert(category);
    }

    public void update(Category category) {
        categoryMapper.updateById(category);
    }

    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
