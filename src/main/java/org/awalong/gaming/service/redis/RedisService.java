package org.awalong.gaming.service.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    public static final String FIVE_PLAYER = "FIVE_PLAYER";
    public static final String SIX_PLAYER = "SIX_PLAYER";
    public static final String SEVEN_PLAYER = "SEVEN_PLAYER";
    public static final String EIGHT_PLAYER = "EIGHT_PLAYER";
    public static final String NINE_PLAYER = "NINE_PLAYER";
    public static final String TEN_PLAYER = "TEN_PLAYER";

    @Autowired
    private RedisTemplate redisTemplate;

    public void saveEntityData(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void saveData(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public Object getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void cacheGamerNumberAndHeros() {
        redisTemplate.opsForValue().set(FIVE_PLAYER, "梅林,派西维尔,忠臣,莫甘娜,刺客");
        redisTemplate.opsForValue().set(SIX_PLAYER, "梅林,派西维尔,忠臣,忠臣,莫甘娜,刺客");
        redisTemplate.opsForValue().set(SEVEN_PLAYER, "梅林,派西维尔,忠臣,忠臣,莫甘娜,奥伯伦,刺客");
        redisTemplate.opsForValue().set(EIGHT_PLAYER, "梅林,派西维尔,忠臣,忠臣,忠臣,莫甘娜,刺客,爪牙");
        redisTemplate.opsForValue().set(NINE_PLAYER, "梅林,派西维尔,忠臣,忠臣,忠臣,忠臣,莫德雷德,莫甘娜,刺客");
        redisTemplate.opsForValue().set(TEN_PLAYER, "梅林,派西维尔,忠臣,忠臣,忠臣,忠臣,莫德雷德,莫甘娜,刺客,奥伯伦");
    }
}
