package org.awalong.gaming.service.room;

import org.awalong.gaming.entitys.RoomInfo;
import org.awalong.gaming.service.game.GameService;
import org.awalong.gaming.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {
    @Autowired
    private RedisService redisService;

    @Autowired
    private GameService gameService;

    /**
     * 创建房间，先检查房间是否已经存在，
     * 如果已经存在，就获取房间信息，返回给前端展示；
     * 如果不存在，就在redis中创建记录。
     *
     * 创建好房间后，预先处理游戏人物。
     * @param roomInfo
     * @return
     */
    public RoomInfo createRoom(final RoomInfo roomInfo) {
        RoomInfo room = (RoomInfo) redisService.getData(roomInfo.getRoomId());
        if (null != room) {
            System.out.println("房间 " + room.getRoomId() + "已经被创建，去加入吧。");
            room.setMessage("房间 " + room.getRoomId() + "已经被创建，去加入吧。");
            room.setRoomStatus(Boolean.FALSE);
            return room;
        }
        roomInfo.setRoomMembers(List.of(roomInfo.getRoomMaster()));
        roomInfo.setRoomStatus(Boolean.TRUE);
        roomInfo.setJoinedNumber(1);
        redisService.saveEntityData(roomInfo.getRoomId(), roomInfo);

        System.out.println("预先处理游戏信息.......");
        gameService.preDealCards(roomInfo.getRoomId());

        return roomInfo;
    }


}
