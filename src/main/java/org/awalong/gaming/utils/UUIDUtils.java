package org.awalong.gaming.utils;

import java.util.UUID;

public class UUIDUtils {

    public static String getUUID() {
        UUID uniqueID = UUID.randomUUID();
        // 将UUID转换为字符串表示
        return uniqueID.toString();
    }
}
