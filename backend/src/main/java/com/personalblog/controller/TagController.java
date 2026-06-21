package com.personalblog.controller;

import com.personalblog.dto.Result;
import com.personalblog.entity.Tag;
import com.personalblog.service.TagService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @GetMapping
    public Result<List<Tag>> list() {
        return Result.success(tagService.list());
    }

    @PostMapping
    public Result<Void> save(@RequestBody Tag tag) {
        tagService.save(tag);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Tag tag) {
        tagService.update(tag);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success();
    }
}
