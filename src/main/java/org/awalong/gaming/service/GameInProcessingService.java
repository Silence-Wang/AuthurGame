package org.awalong.gaming.service;

import org.awalong.gaming.entitys.*;
import org.awalong.gaming.service.game.GameService;
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

    /**
     * 组车队的逻辑
     * @param tempTeamInfo
     * @return
     */
    public GameInfo dealGameProcess(final TempTeamInfo tempTeamInfo) {
        GameInfo gameInfo = gameService.getGameInfo(tempTeamInfo.getGameId());
        if (CollectionUtils.isEmpty(gameInfo.getRoundInfos())) {
            RoundInfo roundInfo = new RoundInfo();
            roundInfo.setCaptain(tempTeamInfo.getCaptain());
            roundInfo.setRound(1);
            roundInfo.setTeamMembers(tempTeamInfo.getTeamMember());

            gameInfo.setRoundInfos(List.of(roundInfo));
        } else {
            // 如果不是更新，就是临时选队，新增一条记录即可，原来是2条，加一条第3条；
            // 否则就是最终队伍，要去修改前面刚刚创建的临时队伍3。
            if (!tempTeamInfo.getUpdate()) {
                List<RoundInfo> oldRounds = new ArrayList<>(gameInfo.getRoundInfos());
                RoundInfo roundInfo = new RoundInfo();
                roundInfo.setCaptain(tempTeamInfo.getCaptain());
                roundInfo.setRound(oldRounds.size() + 1);
                roundInfo.setTeamMembers(tempTeamInfo.getTeamMember());
                oldRounds.add(roundInfo);

                gameInfo.setRoundInfos(oldRounds);
            } else {
                List<RoundInfo> oldRounds = new ArrayList<>(gameInfo.getRoundInfos());
                // 根据oldRounds里的RoundInfo对象，取round最大的这个对象
                Optional<RoundInfo> newestRound = oldRounds.stream()
                        .max(Comparator.comparingInt(RoundInfo::getRound));
                newestRound.get().setTeamMembers(tempTeamInfo.getTeamMember());
                gameInfo.setRoundInfos(oldRounds);
            }
        }

        redisService.saveEntityData(tempTeamInfo.getGameId(), gameInfo);
        return gameInfo;
    }


    /**
     * 投票决定车长所选队伍是否可以做任务。
     * VoteInfo里面有gameId和round，根据这两个，找到RoundInfo。
     * 如果这个support为 true，表示赞成此车，那么对应修改RoundInfo的信息。
     * @param voteInfo
     */
    public GameInfo vote(final VoteInfo voteInfo) {
        GameInfo gameInfo = gameService.getGameInfo(voteInfo.getGameId());
        //TODO 这里检查一下投票人，防止出现重复投票。
        List<RoundInfo> roundInfos = gameInfo.getRoundInfos();
        RoundInfo roundInfo = roundInfos.stream()
                .filter(round -> round.getRound() == voteInfo.getRound())
                .collect(Collectors.toList()).get(0);

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

    /**
     * 根据gameId来获取round信息。
     * 先计算Round的成功或者失败。
     * 如果有3局失败或者3就成功，则直接判定胜负。
     * @param gameId
     * @return
     */
    public GameInfo getGameInfo(final String gameId) {
        GameInfo gameInfo = gameService.getGameInfo(gameId);
        List<RoundInfo> roundInfos = gameInfo.getRoundInfos().stream()
                .filter(round -> round.getOrganized())
                .collect(Collectors.toList());

        //先判断本轮次任务是否成功，根据黑票数判定
        if (!CollectionUtils.isEmpty(roundInfos)) {
            for (RoundInfo roundInfo : roundInfos) {
                if (roundInfo.getRound() == 4 && 7 <= gameInfo.getGamerNumber()) {
                    if (roundInfo.getBlackTicketsNumber() >= 2) {
                        roundInfo.setSuccess(Boolean.FALSE);
                    }
                } else {
                    if (roundInfo.getBlackTicketsNumber() >= 1) {
                        roundInfo.setSuccess(Boolean.FALSE);
                    }
                }
            }
        }

        //再判断游戏是否结束。
        Long successRounds = roundInfos.stream().filter(roundInfo -> roundInfo.getSuccess()).count();
        if (successRounds == 3) {
            gameInfo.setMessage("好人获胜！");
            gameInfo.setEnd(Boolean.TRUE);
        }

        Long failedRounds = roundInfos.stream().filter(roundInfo -> !roundInfo.getSuccess()).count();
        if (failedRounds == 3) {
            gameInfo.setMessage("坏人获胜！");
            gameInfo.setEnd(Boolean.TRUE);
        }


        redisService.saveEntityData(gameInfo.getGameId(), gameInfo);
        return gameInfo;
    }
}
