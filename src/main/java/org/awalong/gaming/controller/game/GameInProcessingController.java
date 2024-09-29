package org.awalong.gaming.controller.game;

import org.awalong.gaming.entitys.*;
import org.awalong.gaming.service.game.GameInProcessingService;
import org.awalong.gaming.service.game.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/gameInProcess")
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
    public ResponseData teamUp(@RequestBody TempTeamInfo tempTeamInfo) {
        ResponseData responseData = new ResponseData();
        responseData.setData(gameInProcessingService.teamUp(tempTeamInfo));
        responseData.setCode(200);
        return responseData;
    }

    /**
     * 投票
     * @param voteInfo
     * @return
     */
    @PostMapping("/vote")
    public ResponseData vote(@RequestBody VoteInfo voteInfo) {
        ResponseData responseData = new ResponseData();
        responseData.setData(gameInProcessingService.vote(voteInfo));
        responseData.setCode(200);

        return responseData;
    }

    /**
     * 通过投票后，判断是否能发车的逻辑
     * @param gameId
     * @param round
     * @return
     */
    @GetMapping("/checkRoundInfoAfterVote/{gameId}/{round}")
    public ResponseData checkRoundInfoAfterVote(@PathVariable("gameId") String gameId, @PathVariable("round") int round) {
        ResponseData responseData = new ResponseData();
        responseData.setData(gameInProcessingService.checkRoundInfoAfterVote(gameId, round));
        responseData.setCode(200);
        return responseData;
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

    /**
     * 通过投票后，判断是否能发车的逻辑
     * @param gameId
     * @param round
     * @return
     */
    @PostMapping("/checkRoundInfoAfterTask/{gameId}/{round}")
    public RoundInfo checkRoundInfoAfterTask(@PathVariable("gameId") String gameId, @PathVariable("round") int round) {
        return gameInProcessingService.checkRoundInfoAfterTask(gameId, round);
    }


}
