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
        return  gameService.startGame(gameId);
    }

    @GetMapping("/refreshGame")
    public void refresh() {
        gameService.refreshGame();
    }

    /**
     * 在每一次出任务以后调用一次，来检查round里面的信息
     *
     * @param gameId
     * @return
     */
    @GetMapping("/getGameInfo/{gameId}")
    public GameInfo getGameInfo(@PathVariable("gameId") String gameId) {
        return  gameService.startGame(gameId);
    }

}
