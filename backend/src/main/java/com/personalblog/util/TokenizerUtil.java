package com.personalblog.util;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TokenizerUtil {

    private static final JiebaSegmenter SEGMENTER = new JiebaSegmenter();

    private TokenizerUtil() {}

    /**
     * 中英文混合分词，过滤单字和空白。
     */
    public static List<String> tokenize(String text) {
        if (text == null || text.isBlank()) return List.of();
        List<SegToken> tokens = SEGMENTER.process(text, JiebaSegmenter.SegMode.SEARCH);
        return tokens.stream()
                .map(t -> t.word)
                .map(String::trim)
                .filter(w -> w.length() >= 2 || isEnglishWord(w))
                .filter(w -> !w.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private static boolean isEnglishWord(String w) {
        return w.length() == 1 && Character.isLetter(w.charAt(0));
    }

    /**
     * 从文章中提取匹配片段。
     */
    public static String extractSnippet(String content, String query, int maxLen) {
        if (content == null || query == null) return "";
        int idx = content.indexOf(query);
        if (idx == -1) {
            // 用查询的第一个词定位
            String firstWord = query.split("\\s+")[0];
            idx = content.indexOf(firstWord);
        }
        if (idx == -1) {
            return content.length() > maxLen ? content.substring(0, maxLen) + "..." : content;
        }
        int start = Math.max(0, idx - maxLen / 3);
        int end = Math.min(content.length(), start + maxLen);
        String snippet = (start > 0 ? "..." : "") + content.substring(start, end);
        if (end < content.length()) snippet += "...";
        return snippet;
    }
}
