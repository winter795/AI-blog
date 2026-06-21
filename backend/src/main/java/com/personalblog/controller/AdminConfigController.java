package com.personalblog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalblog.dto.Result;
import com.personalblog.entity.FriendLink;
import com.personalblog.entity.SiteConfig;
import com.personalblog.mapper.FriendLinkMapper;
import com.personalblog.mapper.SiteConfigMapper;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminConfigController {

    private final SiteConfigMapper configMapper;
    private final FriendLinkMapper linkMapper;

    public AdminConfigController(SiteConfigMapper configMapper, FriendLinkMapper linkMapper) {
        this.configMapper = configMapper;
        this.linkMapper = linkMapper;
    }

    // ── 站点配置 ──
    @GetMapping("/config")
    public Result<Map<String, String>> getConfig() {
        Map<String, String> map = new LinkedHashMap<>();
        configMapper.selectList(null).forEach(c -> map.put(c.getConfigKey(), c.getConfigValue()));
        return Result.success(map);
    }

    @PostMapping("/config")
    public Result<Void> saveConfig(@RequestBody Map<String, String> body) {
        for (var entry : body.entrySet()) {
            SiteConfig config = configMapper.selectOne(new LambdaQueryWrapper<SiteConfig>()
                    .eq(SiteConfig::getConfigKey, entry.getKey()));
            if (config != null) {
                config.setConfigValue(entry.getValue());
                configMapper.updateById(config);
            } else {
                config = new SiteConfig();
                config.setConfigKey(entry.getKey());
                config.setConfigValue(entry.getValue());
                configMapper.insert(config);
            }
        }
        return Result.success();
    }

    // ── 友链管理 ──
    @GetMapping("/friend-links")
    public Result<List<FriendLink>> list() {
        return Result.success(linkMapper.selectList(
                new LambdaQueryWrapper<FriendLink>().orderByAsc(FriendLink::getSortOrder)));
    }

    @PostMapping("/friend-links")
    public Result<Void> save(@RequestBody FriendLink link) {
        linkMapper.insert(link);
        return Result.success();
    }

    @PutMapping("/friend-links")
    public Result<Void> update(@RequestBody FriendLink link) {
        linkMapper.updateById(link);
        return Result.success();
    }

    @DeleteMapping("/friend-links/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        linkMapper.deleteById(id);
        return Result.success();
    }
}
