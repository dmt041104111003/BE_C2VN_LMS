package com.cardano_lms.server.Service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitService {
    
    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MINUTES = 15;
    private static final long ATTEMPT_WINDOW_MINUTES = 5;
    
    private final ConcurrentHashMap<String, AttemptInfo> attempts = new ConcurrentHashMap<>();
    
    private record AttemptInfo(int count, long firstAttemptTime, long blockedUntil) {}
    
    public boolean isBlocked(String key) {
        AttemptInfo info = attempts.get(key);
        if (info == null) return false;
        
        long now = System.currentTimeMillis();
        
        if (info.blockedUntil > 0 && now < info.blockedUntil) {
            return true;
        }
        
        if (info.blockedUntil > 0 && now >= info.blockedUntil) {
            attempts.remove(key);
            return false;
        }
        
        return false;
    }
    
    public long getBlockedSecondsRemaining(String key) {
        AttemptInfo info = attempts.get(key);
        if (info == null || info.blockedUntil <= 0) return 0;
        
        long remaining = (info.blockedUntil - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }
    
    public void recordFailedAttempt(String key) {
        long now = System.currentTimeMillis();
        long windowStart = now - TimeUnit.MINUTES.toMillis(ATTEMPT_WINDOW_MINUTES);
        
        attempts.compute(key, (k, info) -> {
            if (info == null || info.firstAttemptTime < windowStart) {
                return new AttemptInfo(1, now, 0);
            }
            
            int newCount = info.count + 1;
            
            if (newCount >= MAX_ATTEMPTS) {
                long blockedUntil = now + TimeUnit.MINUTES.toMillis(BLOCK_DURATION_MINUTES);
                return new AttemptInfo(newCount, info.firstAttemptTime, blockedUntil);
            }
            
            return new AttemptInfo(newCount, info.firstAttemptTime, 0);
        });
    }
    
    public void recordSuccessfulAttempt(String key) {
        attempts.remove(key);
    }
    
    public int getRemainingAttempts(String key) {
        AttemptInfo info = attempts.get(key);
        if (info == null) return MAX_ATTEMPTS;
        
        long windowStart = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(ATTEMPT_WINDOW_MINUTES);
        if (info.firstAttemptTime < windowStart) return MAX_ATTEMPTS;
        
        return Math.max(0, MAX_ATTEMPTS - info.count);
    }
}

