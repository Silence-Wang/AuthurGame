package org.awalong.gaming.entitys;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseData {

    private int code;
    private String message;
    private Object data;
}
