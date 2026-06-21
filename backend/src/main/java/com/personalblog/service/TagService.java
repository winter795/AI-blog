package com.personalblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalblog.entity.Tag;
import com.personalblog.mapper.TagMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    private final TagMapper tagMapper;

    public TagService(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    public List<Tag> list() {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Tag::getCreatedAt);
        return tagMapper.selectList(wrapper);
    }

    public void save(Tag tag) {
        tagMapper.insert(tag);
    }

    public void update(Tag tag) {
        tagMapper.updateById(tag);
    }

    public void delete(Long id) {
        tagMapper.deleteById(id);
    }
}
