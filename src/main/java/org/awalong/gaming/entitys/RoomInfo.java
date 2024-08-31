package org.awalong.gaming.entitys;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class RoomInfo implements Serializable {
    private static final long serialVersionUID = 2L;

    //房间号
    private String roomId;

    //房主
    private String roomMaster;

    //进入房间的人
    private List<String> roomMembers;

    //房间人数
    private int number;

    //房间状态
    private Boolean roomStatus;

    //提示信息
    private String message;
}

