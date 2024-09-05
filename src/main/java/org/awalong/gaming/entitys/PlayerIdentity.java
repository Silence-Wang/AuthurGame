package org.awalong.gaming.entitys;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class PlayerIdentity implements Serializable {
    private static final long serialVersionUID = 1L;

    // 自己的身份
    private String identity;

    // 视野
    private List<String> vision;
}
