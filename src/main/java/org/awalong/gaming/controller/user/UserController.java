package org.awalong.gaming.controller.user;

import org.awalong.gaming.entitys.RoomInfo;
import org.awalong.gaming.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/joinRoom/{roomNumber}/{username}")
    public RoomInfo joinRoom(@PathVariable("roomNumber") String roomId, @PathVariable("username") String username) {
        return userService.joinRoom(roomId, username);
    }

}
