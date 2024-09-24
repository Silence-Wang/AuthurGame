package org.awalong.gaming.entitys;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class TempTeamInfo  implements Serializable {
    private static final long serialVersionUID = 4L;

    //游戏ID
    private String gameId;

    //车长
    private String captain;

    //轮次
    private int round;

    //组队成员
    private List<String> teamMember;

    //是什么类型的组队，FIRST表示临时组队，FINAL表示最终组队。
    private String type;
}
