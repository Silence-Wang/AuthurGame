package org.awalong.gaming.controller.game;

import org.awalong.gaming.entitys.GameInfo;
import org.awalong.gaming.service.game.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GameController {

    @Autowired
    private GameService gameService;

    @GetMapping("/checkGameCanStart/{roomId}")
    public GameInfo checkGameStatus(@PathVariable("roomId") String roomId) {
        return gameService.checkGameCanStart(roomId);
    }

    @GetMapping("/start/{gameId}")
    public GameInfo startGame(@PathVariable("gameId") String gameId) {
        return gameService.startGame(gameId);
    }

    @GetMapping("/refreshGame")
    public String refresh() {
        gameService.refreshGame();
        return "游戏人数与对应需要身份已经准备好。";
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
