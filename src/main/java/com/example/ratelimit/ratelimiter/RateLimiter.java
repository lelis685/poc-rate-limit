package com.example.ratelimit.ratelimiter;

import com.example.ratelimit.ratelimiter.dataprovider.RateLimitData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;


@Component
@RequiredArgsConstructor
public class RateLimiter {

    @Value("${tpm.interval-seconds}")
    private int intervalSeconds;

    private final RedisTemplate<String, String> template;
    private final RateLimitData rateLimitConfig;


    public boolean tryConsume(String key) {
        String lastResetTimeKey = key + "_last_reset_time";
        String counterKey = key + "_counter";

        template.watch(Arrays.asList(lastResetTimeKey, counterKey));

        String lastResetTimeValue = template.opsForValue().get(lastResetTimeKey);
        long lastResetTime = lastResetTimeValue != null ? Long.parseLong(lastResetTimeValue) : 0;

        if (hasTimeWindowElapsed(lastResetTime)) {
            template.opsForValue().set(counterKey, String.valueOf(rateLimitConfig.getTokenSize()));
            template.opsForValue().set(lastResetTimeKey, String.valueOf(System.currentTimeMillis() / 1000L));
        } else {
            String counterValue = template.opsForValue().get(counterKey);
            long requestLeft = counterValue != null ? Long.parseLong(counterValue) : 0;
            if (requestLeft <= 0) {
                return false;
            }
        }

        template.opsForValue().decrement(counterKey);

        template.unwatch();

        return true;
    }

    private boolean hasTimeWindowElapsed(long lastResetTime) {
        return System.currentTimeMillis() / 1000L - lastResetTime >= intervalSeconds;
    }


}
