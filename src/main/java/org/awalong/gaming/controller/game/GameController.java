package org.awalong.gaming.controller.game;

import org.awalong.gaming.entitys.GameInfo;
import org.awalong.gaming.entitys.ResponseData;
import org.awalong.gaming.service.game.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping("/refreshGame")
    public String refresh() {
        gameService.refreshGame();
        return "游戏人数与对应需要身份已经准备好。";
    }

    @GetMapping("/checkGameCanStart/{roomId}")
    public ResponseData checkGameStatus(@PathVariable("roomId") String roomId) {
        ResponseData responseData = new ResponseData();
        responseData.setData(gameService.checkGameCanStart(roomId));
        responseData.setCode(200);
        return responseData;
    }

    @GetMapping("/start/{gameId}/{username}")
    public ResponseData startGame(@PathVariable("gameId") String gameId, @PathVariable("username") String username) {
        ResponseData responseData = new ResponseData();
        responseData.setData(gameService.startGame(gameId, username));
        responseData.setCode(200);
        return responseData;
    }

    /**
     * 在每一次出任务以后调用一次，来检查round里面的信息。
     * 在这个接口里，把round info存进gameInfo里一起给到前端展示。
     *
     * @param gameId
     * @return
     */
    @GetMapping("/getGameRoundInfo/{gameId}")
    public GameInfo getGameRoundInfo(@PathVariable("gameId") String gameId) {
        return  gameService.getGameRoundInfo(gameId);
    }

}
