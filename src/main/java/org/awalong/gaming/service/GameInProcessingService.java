package org.awalong.gaming.service;

import org.awalong.gaming.entitys.*;
import org.awalong.gaming.service.game.GameService;
import org.awalong.gaming.service.game.RoundService;
import org.awalong.gaming.service.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.util.MapUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GameInProcessingService {

    @Autowired
    private GameService gameService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RoundService roundService;

    /**
     * 组车队
     * TODO 这里重新理一下逻辑，对于修改车队的做法，到底后端怎么处理这条记录？
     * 如果roun
     * @param tempTeamInfo
     * @return
     */
    public RoundInfo teamUp(final TempTeamInfo tempTeamInfo) {
        RoundInfo roundInfo = roundService.getRoundInfo(tempTeamInfo.getGameId() + "-" + tempTeamInfo.getRound());
        if (null == roundInfo) {
            roundInfo = new RoundInfo();
        }

        roundInfo.setGameId(tempTeamInfo.getGameId());
        roundInfo.setRound(tempTeamInfo.getRound());
        roundInfo.setTeamMembers(tempTeamInfo.getTeamMember());

        redisService.saveEntityData(tempTeamInfo.getGameId() + "-" + tempTeamInfo.getRound(), roundInfo);
        return roundInfo;
    }


    /**
     * 投票决定车长所选队伍是否可以做任务。
     * VoteInfo里面有gameId和round，根据这两个，找到RoundInfo。
     * 如果这个support为 true，表示赞成此车，那么对应修改RoundInfo的信息。
     * @param voteInfo
     */
    public GameInfo vote(final VoteInfo voteInfo) {
        GameInfo gameInfo = gameService.getGameInfo(voteInfo.getGameId());
        RoundInfo roundInfo = roundService.getRoundInfo(voteInfo.getGameId() + "-" + voteInfo.getRound());
        // 这里检查一下投票人，防止出现重复投票。
        List<String> combinedList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(roundInfo.getSupporters())) {
            combinedList.addAll(roundInfo.getSupporters());
        }

        if (!CollectionUtils.isEmpty(roundInfo.getObjectors())) {
            combinedList.addAll(roundInfo.getObjectors());
        }

        if (combinedList.contains(voteInfo.getUsername())) {
            gameInfo.setMessage("本轮你已投过票，请不要重复投票");
            return gameInfo;
        }

        if (voteInfo.getSupport()) {
            List<String> supportors;
            if (CollectionUtils.isEmpty(roundInfo.getSupporters())) {
                supportors = new ArrayList<>();
            } else {
                supportors = new ArrayList<>(roundInfo.getSupporters());
            }
            supportors.add(voteInfo.getUsername());
            roundInfo.setSupporters(supportors);
            roundInfo.setVoted(roundInfo.getVoted() + 1);
        } else {
            List<String> objectors;
            if (CollectionUtils.isEmpty(roundInfo.getObjectors())) {
                objectors = new ArrayList<>();
            } else {
                objectors = new ArrayList<>(roundInfo.getObjectors());
            }
            objectors.add(voteInfo.getUsername());
            roundInfo.setObjectors(objectors);
            roundInfo.setVoted(roundInfo.getVoted() + 1);
        }

        redisService.saveEntityData(voteInfo.getGameId(), gameInfo);
        return gameInfo;
    }

    /**
     *
     * @param voteInfo
     * @return
     */
    public Boolean canDrive(final VoteInfo voteInfo) {
        GameInfo gameInfo = gameService.getGameInfo(voteInfo.getGameId());
        List<RoundInfo> roundInfos = gameInfo.getRoundInfos();
        RoundInfo roundInfo = roundInfos.stream()
                .filter(round -> round.getRound() == voteInfo.getRound())
                .collect(Collectors.toList()).get(0);

        int supportNumber = roundInfo.getSupporters().size();
        if (supportNumber > gameInfo.getGamerNumber() / 2) {
            roundInfo.setOrganized(Boolean.TRUE);
        } else {
            roundInfo.setOrganized(Boolean.FALSE);
        }

        redisService.saveEntityData(gameInfo.getGameId(), gameInfo);
        return roundInfo.getOrganized();
    }

    /**
     * 做任务， 这里只要负责记录下来任务出票的信息，不要去做判断轮次任务是否成功。
     * 因为是每一个人的投票，如果现在出结果，会被看到谁投的黑票。
     *
     * 先检查投票的人是不是车队里的人；
     * 是的话，检查票型，如果是黑票就在黑票数字段上加1；
     * 检查是第几轮，与游戏人数比较下，对大于等于7人局，第4局的保护轮黑票数量做检查，来判断任务成功或失败。
     * 直接将gameInfo结构返回，让前端判断这里面的success信息。
     * @param taskInfo
     */
    public void doTask(final TaskInfo taskInfo) {
        GameInfo gameInfo = gameService.getGameInfo(taskInfo.getGameId());
        List<RoundInfo> roundInfos = gameInfo.getRoundInfos();
        RoundInfo roundInfo = roundInfos.stream()
                .filter(round -> round.getRound() == taskInfo.getRound())
                .collect(Collectors.toList()).get(0);

        // 先保存投票信息
        Map<String, Boolean> voteStatus = roundInfo.getVoteStatus();
        if (null == voteStatus || voteStatus.isEmpty()) {
            voteStatus = new HashMap<>();
        }
        voteStatus.put(taskInfo.getUsername(), taskInfo.getTicketType());
        roundInfo.setVoteStatus(voteStatus);

        if (!taskInfo.getTicketType()) {
            roundInfo.setBlackTicketsNumber(roundInfo.getBlackTicketsNumber() + 1);
        }

        redisService.saveEntityData(taskInfo.getGameId(), gameInfo);
    }


}
