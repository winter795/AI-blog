package com.personalblog.util;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CaptchaUtil {

    private final Map<String, Integer> store = new ConcurrentHashMap<>();

    public Map<String, String> generate() {
        int a = new Random().nextInt(10) + 1;
        int b = new Random().nextInt(10) + 1;
        String expression = a + " + " + b + " = ?";
        String token = UUID.randomUUID().toString();
        store.put(token, a + b);
        // 5分钟过期
        new Thread(() -> {
            try { Thread.sleep(300000); } catch (Exception ignored) {}
            store.remove(token);
        }).start();
        return Map.of("expression", expression, "token", token);
    }

    public boolean verify(String token, int answer) {
        Integer correct = store.remove(token);
        return correct != null && correct == answer;
    }
}
