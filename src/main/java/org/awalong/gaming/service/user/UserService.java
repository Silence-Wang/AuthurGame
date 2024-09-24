package org.awalong.gaming.service.user;

import org.awalong.gaming.entitys.RoomInfo;
import org.awalong.gaming.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private static final String ROOM_NUMBER_PREFIX  = "No.";

    @Autowired
    private RedisService redisService;

    /**
     * 加入房间的时候，先检查房间是否创建好。
     * 如果创建好，将自己加入等待列表，返回信息给前端；
     * 如果没有创建好，就返回信息给前段。
     * @param roomId
     * @param username
     * @return
     */
    public RoomInfo joinRoom(final String roomId, final String username) {
        RoomInfo room = (RoomInfo) redisService.getData(roomId);
        if (null != room) {
            List<String> roomMembers = new ArrayList<>(room.getRoomMembers());
            if (room.getJoinedNumber() == room.getNumber()) {
                room.setMessage("房间已满员！");
            } else if (roomMembers.contains(username)) {
                room.setMessage("你已经加入房间，请等待房主开始游戏。");
            } else {
                roomMembers.add(username);
                room.setRoomMembers(roomMembers);
                room.setJoinedNumber(room.getJoinedNumber() + 1);
                redisService.saveEntityData(roomId, room);
            }
            System.out.println(username + " 加入房间 " + roomId);
            return room;
        }
        room = new RoomInfo();
        room.setRoomStatus(Boolean.FALSE);
        room.setMessage("这个房间号不存在，你可以自己创建房间或者等待其他人创建后再加入。");

        return room;
    }
}
