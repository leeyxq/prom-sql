package io.github.leeyxq.promsql.util;

import lombok.experimental.UtilityClass;

/**
 * 字符串工具类
 *
 * @author lixiangqian
 * @since 2022/11/23 08:58
 */
@UtilityClass
public class StrUtil {
    public static final String EMPTY = "";

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 转义 PromQL 标签值中的特殊字符
     */
    public static String escapeLabelValue(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * 转义 PromQL 正则中的特殊字符
     */
    public static String escapeRegex(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("([\\\\.^$|?*+()\\[\\]{}])", "\\\\$1");
    }
}
