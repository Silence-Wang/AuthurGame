package org.awalong.gaming.entitys;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VoteInfo {

    private String gameId;

    private int round;

    private String username;

    private Boolean support;
}
