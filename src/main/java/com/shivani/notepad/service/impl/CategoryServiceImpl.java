package com.shivani.notepad.service.impl;

import com.shivani.notepad.entity.Category;
import com.shivani.notepad.entity.User;
import com.shivani.notepad.exception.CategoryNotFoundException;
import com.shivani.notepad.repository.CategoryRepository;
import com.shivani.notepad.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final String[] DEFAULT_CATEGORIES = {
            "Work", "Personal", "Finance", "Health", "Education",
            "Travel", "Shopping", "Legal & Admin", "Projects", "Miscellaneous"
    };

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> getCategories(User owner) {
        List<Category> existing = categoryRepository.findByUser(owner);
        if (existing.isEmpty()) {
            existing = seedDefaultCategories(owner);
        }
        return existing;
    }

    @Override
    public Category findCategory(Integer categoryId, User owner) {
        return categoryRepository.findById(categoryId)
                .filter(cat -> cat.getUser().getId().equals(owner.getId()))
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found or you do not have access to it: " + categoryId));
    }

    private List<Category> seedDefaultCategories(User owner) {
        List<Category> created = new ArrayList<>();
        for (String name : DEFAULT_CATEGORIES) {
            Category category = new Category();
            category.setName(name);
            category.setUser(owner);
            created.add(categoryRepository.save(category));
        }
        return created;
    }
}