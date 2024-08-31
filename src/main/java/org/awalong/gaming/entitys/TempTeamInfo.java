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

    //组队成员
    private List<String> teamMember;

    //是否更新过
    private Boolean update;
}
