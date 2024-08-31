package org.awalong.gaming.service.game;

import org.awalong.gaming.entitys.GameInfo;
import org.awalong.gaming.entitys.RoomInfo;
import org.awalong.gaming.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.awalong.gaming.service.redis.RedisService.*;

@Service
public class GameService {

    private static final String GAME_PREFIX  = "GAME_";

    @Autowired
    private RedisService redisService;

    /**
     * 这里是不断检查房间是否可以开始游戏。
     * 通过传进来的房间号，检查已经进入房间的人数与设置的游戏人数是否一致。
     * 如果一致的话，将游戏开始状态设置成true，同时，将房间里的人放进游戏人员里，保存进数据库。页面开始进入下一步；
     * 如果不一致，就将已经进入的人的信息返回到页面上，展示出来。
     * @param roomId
     * @return
     */
    public GameInfo checkGameCanStart(final String roomId) {
        RoomInfo room = (RoomInfo) redisService.getData(roomId);

        String gameId = roomId.split("_").length > 1 ? GAME_PREFIX + roomId.split("_")[1] : GAME_PREFIX + roomId;
        GameInfo gameInfo = (GameInfo) redisService.getData(gameId);
        if (null != gameInfo) {
            if (room.getNumber() == room.getRoomMembers().size()) {
                gameInfo.setStart(Boolean.TRUE);
                gameInfo.setMessage("人齐了，开始吧。");
                gameInfo.setHasJoined(room.getRoomMembers());
                redisService.saveEntityData(gameId, gameInfo);
            } else {
                gameInfo.setStart(Boolean.FALSE);
                gameInfo.setMessage("人没齐，再等等。");
            }
        } else {
            gameInfo.setMessage("房间未创建或者游戏已结束，房间已解散。");
            gameInfo.setStart(Boolean.FALSE);
        }
        return gameInfo;
    }

    public void refreshGame() {
        redisService.cacheGamerNumberAndHeros();
    }

    /**
     * 这里根据传进来的游戏人数，提前准备好游戏人物角色并绑定到房间号上
     * @param roomId
     */
    public void preDealCards(final String roomId) {
        RoomInfo room = (RoomInfo) redisService.getData(roomId);

        GameInfo gameInfo = new GameInfo();
        gameInfo.setRoomId(roomId);
        gameInfo.setGameId(roomId.split("_").length > 1 ? GAME_PREFIX + roomId.split("_")[1] : GAME_PREFIX + roomId);
        gameInfo.setStart(Boolean.FALSE);
        gameInfo.setGamerNumber(room.getNumber());
        gameInfo.setHasJoined(room.getRoomMembers());
        List<String> heroSink = new ArrayList<>();
                switch (room.getNumber()) {
            case 5:
                heroSink = Arrays.asList(String.valueOf(redisService.getData(FIVE_PLAYER)).split(","));
                break;
            case 6:
                heroSink = Arrays.asList(String.valueOf(redisService.getData(SIX_PLAYER)).split(","));
                break;
            case 7:
                heroSink = Arrays.asList(String.valueOf(redisService.getData(SEVEN_PLAYER)).split(","));
                break;
            case 8:
                heroSink = Arrays.asList(String.valueOf(redisService.getData(EIGHT_PLAYER)).split(","));
                break;
            case 9:
                heroSink = Arrays.asList(String.valueOf(redisService.getData(NINE_PLAYER)).split(","));
                break;
            case 10:
                heroSink = Arrays.asList(String.valueOf(redisService.getData(TEN_PLAYER)).split(","));
                break;
        }
        gameInfo.setHeroSink(heroSink);
        redisService.saveEntityData(gameInfo.getGameId(), gameInfo);
        System.out.println("Pre deal cards success......");
    }

    /**
     * 将房间里的人的名字和游戏人物身份随机绑定，并且都返回给前端。
     * @param gameId
     * @return
     */
    public GameInfo startGame(final String gameId) {
        GameInfo game = (GameInfo) redisService.getData(gameId);
        if (null != game) {
            List<String> hasJoined = game.getHasJoined();
            List<String> heroSink = game.getHeroSink();

            Collections.shuffle(hasJoined);
            Collections.shuffle(heroSink);
            Map<String, String> match = new HashMap<>();
            for (int i = 0; i < hasJoined.size(); i++) {
                match.put(hasJoined.get(i), heroSink.get(i));
            }
            game.setPlayerIdentity(match);
            game.setStart(Boolean.TRUE);
        }
        redisService.saveEntityData(game.getGameId(), game);
        //TODO 这里处理一下视野信息

        return game;
    }

    public GameInfo getGameInfo(final String gameNumber) {
        return (GameInfo) redisService.getData(gameNumber);
    }
}
