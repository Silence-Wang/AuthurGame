package org.awalong.gaming.controller.room;

import org.awalong.gaming.entitys.ResponseData;
import org.awalong.gaming.entitys.RoomInfo;
import org.awalong.gaming.service.room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/room")
public class RoomsController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/create")
    public ResponseData createRoom(@RequestBody RoomInfo roomInfo) {
        ResponseData responseData = new ResponseData();
        RoomInfo roomInfoResult = roomService.createRoom(roomInfo);
        responseData.setData(roomInfoResult);
        if (null == roomInfoResult.getMessage()) {
            responseData.setCode(200);
        } else {
            responseData.setCode(500);
        }
        return responseData;
    }


}
