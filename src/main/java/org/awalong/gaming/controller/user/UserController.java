package org.awalong.gaming.controller.user;

import org.awalong.gaming.entitys.ResponseData;
import org.awalong.gaming.entitys.RoomInfo;
import org.awalong.gaming.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping(value = "/joinRoom/{roomId}/{username}")
    public ResponseData joinRoom(@PathVariable("roomId") String roomId, @PathVariable("username") String username) {
        ResponseData responseData = new ResponseData();
        responseData.setData(userService.joinRoom(roomId, username));
        responseData.setCode(200);
        return responseData;
    }

}
