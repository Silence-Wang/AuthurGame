package org.awalong.gaming.controller.room;

import org.awalong.gaming.entitys.RoomInfo;
import org.awalong.gaming.service.room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class RoomsController {

    @Autowired
    private RoomService roomService;

    @PostMapping("/createRoom")
    public RoomInfo createRoom(@RequestBody RoomInfo roomInfo) {
        return roomService.createRoom(roomInfo);
    }


}
