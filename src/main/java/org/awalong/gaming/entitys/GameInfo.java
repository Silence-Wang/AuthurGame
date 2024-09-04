package org.awalong.gaming.entitys;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class GameInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    // 房间号
    private String roomId;

    //游戏ID
    private String gameId;

    //是否已经开始
    private Boolean start;

    //异常消息
    private String message;

    //加入的人
    private List<String> hasJoined;

    //游戏人数
    private Integer gamerNumber;

    //玩家与身份
    private Map<String, PlayerIdentity> playerIdentitys;

    //游戏人物池
    private List<String> heroSink;

    //轮次信息
    private List<RoundInfo> roundInfos;

    //哪一方胜利
    private String whoWin;

    //游戏结束
    private Boolean end;
}
