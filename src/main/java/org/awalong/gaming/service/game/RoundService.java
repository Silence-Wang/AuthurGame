package org.awalong.gaming.service.game;

import org.awalong.gaming.entitys.RoundInfo;
import org.awalong.gaming.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoundService {

    @Autowired
    private RedisService redisService;

    public RoundInfo getRoundInfo(final String roundKey) {
        return  (RoundInfo) redisService.getData(roundKey);
    }
}
