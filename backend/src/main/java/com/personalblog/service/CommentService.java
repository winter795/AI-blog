package com.personalblog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalblog.entity.Comment;
import com.personalblog.entity.SiteStat;
import com.personalblog.mapper.CommentMapper;
import com.personalblog.mapper.SiteStatMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CommentService {

    private final CommentMapper commentMapper;
    private final SiteStatMapper siteStatMapper;

    public CommentService(CommentMapper commentMapper, SiteStatMapper siteStatMapper) {
        this.commentMapper = commentMapper;
        this.siteStatMapper = siteStatMapper;
    }

    public void submit(Comment comment) {
        comment.setStatus(0); // 待审核
        commentMapper.insert(comment);
    }

    public List<Comment> approvedComments(Long articleId) {
        return commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getArticleId, articleId)
                .eq(Comment::getStatus, 1)
                .orderByAsc(Comment::getCreatedAt));
    }

    public Page<Comment> page(int pageNum, int pageSize, Integer status) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (status != null) wrapper.eq(Comment::getStatus, status);
        wrapper.orderByDesc(Comment::getCreatedAt);
        return commentMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    public void audit(Long id, Integer status) {
        Comment comment = commentMapper.selectById(id);
        if (comment != null) {
            comment.setStatus(status);
            commentMapper.updateById(comment);
        }
    }

    public void delete(Long id) {
        commentMapper.deleteById(id);
    }

    // Site stats
    public void recordPV(String ip) {
        LocalDate today = LocalDate.now();
        SiteStat stat = siteStatMapper.selectOne(new LambdaQueryWrapper<SiteStat>()
                .eq(SiteStat::getStatDate, today));
        if (stat == null) {
            stat = new SiteStat();
            stat.setStatDate(today);
            stat.setPv(1L);
            stat.setUv(1L);
            siteStatMapper.insert(stat);
        } else {
            stat.setPv(stat.getPv() + 1);
            siteStatMapper.updateById(stat);
        }
    }

    public SiteStat todayStats() {
        return siteStatMapper.selectOne(new LambdaQueryWrapper<SiteStat>()
                .eq(SiteStat::getStatDate, LocalDate.now()));
    }

    public long totalPV() {
        List<SiteStat> all = siteStatMapper.selectList(null);
        return all.stream().mapToLong(SiteStat::getPv).sum();
    }
}
