package org.awalong.gaming.service.game;

import org.awalong.gaming.entitys.GameInfo;
import org.awalong.gaming.entitys.PlayerIdentity;
import org.awalong.gaming.entitys.RoomInfo;
import org.awalong.gaming.entitys.RoundInfo;
import org.awalong.gaming.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.awalong.gaming.service.redis.RedisService.*;

@Service
public class GameService {

    private static final String GAME_PREFIX  = "GAME_";

    private static final String SEEN_BY_MEILIN = "莫甘娜,刺客,奥伯伦,爪牙";
    private static final String SEEN_BY_PAI = "莫甘娜,梅林";
    private static final String CONFIRM_EACH_OTHER = "莫甘娜,刺客,莫德雷德,爪牙";


    @Autowired
    private RedisService redisService;

    /**
     * 这里前端会不断检查房间是否可以开始游戏。
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
                gameInfo.setHasJoined(room.getRoomMembers());

                //将房间里的人的名字和游戏人物身份随机绑定，并且都返回给前端。同时处理好视野信息。
                if (null != gameInfo) {
                    List<String> hasJoined = gameInfo.getHasJoined();
                    List<String> heroSink = gameInfo.getHeroSink();

                    Collections.shuffle(hasJoined);
                    Collections.shuffle(heroSink);
                    Map<String, PlayerIdentity> match = new HashMap<>();

                    // 先把玩家自己的身份随机安排好
                    Map<String, String> partInfo = new HashMap<>();
                    for (int i = 0; i < hasJoined.size(); i++) {
                        partInfo.put(hasJoined.get(i), heroSink.get(i));
                    }

                    //这里处理一下视野信息
                    for (Map.Entry<String, String> partInfoEntry : partInfo.entrySet()) {
                        PlayerIdentity playerIdentity = new PlayerIdentity();
                        playerIdentity.setIdentity(partInfoEntry.getValue());
                        playerIdentity.setVision(getVision(partInfoEntry.getValue(), partInfo));

                        match.put(partInfoEntry.getKey(), playerIdentity);
                    }
                    gameInfo.setPlayerIdentitys(match);
                    gameInfo.setStart(Boolean.TRUE);
                }
                redisService.saveEntityData(gameId, gameInfo);
            } else {
                gameInfo.setStart(Boolean.FALSE);
                gameInfo.setHasJoined(room.getRoomMembers());
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
        gameInfo.setEnd(Boolean.FALSE);

        List<String> heroSink = new ArrayList<>();
        switch (room.getNumber()) {
            case 2:
                heroSink = Arrays.asList(String.valueOf(redisService.getData(TWO_PLAYER)).split(","));
                break;
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
     *
     *
     * @param gameId
     * @return
     */
    public PlayerIdentity startGame(final String gameId, final String player) {
        GameInfo game = (GameInfo) redisService.getData(gameId);

        Map<String, PlayerIdentity> playerIdentitys = game.getPlayerIdentitys();
        return playerIdentitys.get(player);
    }

    public GameInfo getGameInfo(final String gameNumber) {
        return (GameInfo) redisService.getData(gameNumber);
    }

    /**
     * 根据gameId来获取round信息。
     * 先计算Round的成功或者失败。
     * 如果有3局失败或者3局成功，则直接判定胜负。
     * TODO 这里暂时不考虑坏人阵营提前刀梅林的局面，场外直接处理。
     * @param gameId
     * @return
     */
    public GameInfo getGameRoundInfo(final String gameId) {
        GameInfo gameInfo = this.getGameInfo(gameId);
        List<Object> roundInfoObjs = redisService.getByKeyPartten(gameId);
        List<RoundInfo> allRoundInfos = new ArrayList<>();
        for (Object roundInfoObj : roundInfoObjs) {
            allRoundInfos.add((RoundInfo) roundInfoObj);
        }

        List<RoundInfo> organizedRoundInfos = allRoundInfos.stream()
                .filter(round -> round.getOrganized())
                .collect(Collectors.toList());

//        gameInfo.setRoundInfos(roundInfos);

        //判断游戏是否结束。
        Long successRounds = organizedRoundInfos.stream().filter(roundInfo -> roundInfo.getSuccess()).count();
        if (successRounds == 3) {
            gameInfo.setMessage("好人获胜！");
            gameInfo.setEnd(Boolean.TRUE);
            gameInfo.setWhoWin("Blue");
        }

        Long failedRounds = organizedRoundInfos.stream().filter(roundInfo -> !roundInfo.getSuccess()).count();
        if (failedRounds == 3) {
            gameInfo.setMessage("坏人获胜！");
            gameInfo.setEnd(Boolean.TRUE);
            gameInfo.setWhoWin("Red");
        }

        redisService.saveEntityData(gameInfo.getGameId(), gameInfo);
        return gameInfo;
    }

    /**
     * 获取对应身份的视野
     * @param identity
     * @param partInfo
     * @return
     */
    private List<String> getVision(final String identity, final Map<String, String> partInfo) {
        List<String> vision = new ArrayList<>();
        if ("梅林".equals(identity)) {
            for (Map.Entry<String, String> entry : partInfo.entrySet()) {
                if (Arrays.asList(SEEN_BY_MEILIN.split(",")).contains(entry.getValue())) {
                    vision.add(entry.getKey());
                }
            }
            return vision;
        }

        if ("派西维尔".equals(identity)) {
            for (Map.Entry<String, String> entry : partInfo.entrySet()) {
                if (Arrays.asList(SEEN_BY_PAI.split(",")).contains(entry.getValue())) {
                    vision.add(entry.getKey());
                }
            }
            return vision;
        }

        if (CONFIRM_EACH_OTHER.contains(identity)) {
            for (Map.Entry<String, String> entry : partInfo.entrySet()) {
                if (Arrays.asList(SEEN_BY_MEILIN.split(",")).contains(entry.getValue()) && !identity.equals(entry.getValue())) {
                    vision.add(entry.getKey());
                }
            }
            return vision;
        }


        return vision;
    }

}
