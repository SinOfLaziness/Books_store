package com.github.Books_store.service;

import org.springframework.stereotype.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class OneTimeCodeService {
    public static class SocialRecord {
        public final Long userId;
        public final String socNet;
        public SocialRecord(Long userId, String socNet) {
            this.userId = userId;
            this.socNet = socNet;
        }
    }

    private final Cache<String, SocialRecord> cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    // Генерирует уникальный код и сохраняет пару (код → (userId, socNet))
    public String generate(Long userId, String socNet) {
        String code;
        do {
            code = java.util.UUID.randomUUID().toString()
                    .replaceAll("[^A-Za-z0-9]", "")
                    .substring(0,6).toUpperCase();
        } while (cache.getIfPresent(code) != null);
        cache.put(code, new SocialRecord(userId, socNet));
        return code;
    }

    // «Потребляет» код: возвращает исходную пару и удаляет из кеша
    public Optional<SocialRecord> consume(String code) {
        SocialRecord rec = cache.getIfPresent(code);
        if (rec != null) {
            cache.invalidate(code);
            return Optional.of(rec);
        }
        return Optional.empty();
    }
}
