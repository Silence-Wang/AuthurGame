package org.awalong.gaming.controller.game;

import org.awalong.gaming.entitys.*;
import org.awalong.gaming.service.GameInProcessingService;
import org.awalong.gaming.service.game.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
public class GameInProcessingController {

    @Autowired
    private GameService gameService;

    @Autowired
    private GameInProcessingService gameInProcessingService;

    /**
     * 选取首轮车长
     * @param gamerNumber
     */
    @GetMapping("/firstCaptain/{gamerNumber}")
    private String chooseFirstCaptain(@PathVariable("gamerNumber") String gamerNumber) {
        GameInfo gameInfo = gameService.getGameInfo(gamerNumber);
        if (null != gameInfo) {
            List<String> hasJoined = gameInfo.getHasJoined();
            Collections.shuffle(hasJoined);

            return hasJoined.stream().findFirst().get();
        }

        return null;
    }

    /**
     * 组队
     * @param tempTeamInfo
     * @return
     */
    @PostMapping("/teamUp")
    public RoundInfo teamUp(@RequestBody TempTeamInfo tempTeamInfo) {
        return gameInProcessingService.teamUp(tempTeamInfo);
    }

    /**
     * 投票
     * @param voteInfo
     * @return
     */
    @PostMapping("/vote")
    public GameInfo vote(@RequestBody VoteInfo voteInfo) {
        return gameInProcessingService.vote(voteInfo);
    }

    /**
     * 通过投票后，判断是否能发车的逻辑
     * @param voteInfo
     * @return
     */
    @PostMapping("/canDrive")
    public boolean canDrive(@RequestBody VoteInfo voteInfo) {
        return gameInProcessingService.canDrive(voteInfo);
    }

    /**
     * 做任务
     * @param taskInfo
     * @return
     */
    @PostMapping("/doTask")
    public String doTask(@RequestBody TaskInfo taskInfo) {
        gameInProcessingService.doTask(taskInfo);
        return "已投票！";
    }



}
