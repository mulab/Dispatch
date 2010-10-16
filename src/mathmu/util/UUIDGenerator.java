/*
 * All reserved by XiaoR
 */

package mathmu.util;

import java.util.UUID;

/**
 *
 * @author XiaoR
 * Created on: 2010-3-25 14:54:32
 */
public class UUIDGenerator {
    /**
     * 获得一个UUID
     * @return String UUID
     */
    public static String getUUID(){
        String s = UUID.randomUUID().toString();
        //去掉“-”符号
        return s.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24);
    }
}
