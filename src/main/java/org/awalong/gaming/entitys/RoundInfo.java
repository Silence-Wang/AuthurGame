package org.awalong.gaming.entitys;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class RoundInfo implements Serializable {
    private static final long serialVersionUID = 3L;

    //游戏ID
    private String gameId;

    //轮次
    private int round;

    //车长
    private String captain;

    //车上人员
    private List<String> teamMembers;

    // 以上是组队的时候会创建出来的信息
    
    //反对组队人员
    private List<String> objectors;

    //赞成组队人员
    private List<String> supporters;

    //已投票人数
    private int voted;

    //以上是投票过程中会记录的信息

    //是否成功组队
    private Boolean organized;

    //本轮任务是否成功
    private Boolean success;

    //黑票票数
    private int blackTicketsNumber;

    //出任务的情况
    private Map<String, Boolean> voteStatus;
}
