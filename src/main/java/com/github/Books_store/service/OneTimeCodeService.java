package com.github.Books_store.service;

import org.springframework.stereotype.Service;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Optional;
import java.util.UUID;
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

    public String generate(Long userId, String socNet) {
        String code;
        do {
            code = UUID.randomUUID().toString()
                    .replaceAll("[^A-Za-z0-9]", "")
                    .substring(0,6).toUpperCase();
        } while (cache.getIfPresent(code) != null);

        cache.put(code, new SocialRecord(userId, socNet));
        System.out.println("[OneTimeCodeService] generate() → code=" + code +
                ", userId=" + userId + ", socNet=" + socNet);
        return code;
    }

    public Optional<SocialRecord> consume(String code) {
        System.out.println("[OneTimeCodeService] consume() → trying to consume code=" + code);
        SocialRecord rec = cache.getIfPresent(code);
        if (rec != null) {
            cache.invalidate(code);
            System.out.println("[OneTimeCodeService] consume() → found record userId="
                    + rec.userId + ", socNet=" + rec.socNet);
            return Optional.of(rec);
        } else {
            System.out.println("[OneTimeCodeService] consume() → NO record for code=" + code);
        }
        return Optional.empty();
    }
}
