package io.github.leeyxq.promsql.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.StringTokenizer;

/**
 * 字符串工具类
 *
 * @author lixiangqian
 * @since 2022/11/23 08:58
 */
@UtilityClass
public class StringUtil {
    public static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    public static boolean isBlank(String str) {
        return isEmpty(str) || str.trim().isEmpty();
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNull(Object object) {
        return object == null;
    }

    public static boolean isNullStr(String str) {
        return str != null && "null".equalsIgnoreCase(str.trim());
    }

    public static boolean isNotNullStr(String str) {
        return str != null && (!"null".equalsIgnoreCase(str.trim()));
    }

    public static boolean equals(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equals(str2);
    }

    public static boolean isBlank(String... strs) {
        if (strs == null || strs.length == 0) {
            throw new IllegalArgumentException("strs can not be null or empty");
        }

        for (String str : strs) {
            if (!isBlank(str)) {
                return false;
            }
        }

        return true;
    }

    public static String join(Collection<String> collection, String separator) {
        if (collection == null) {
            return null;
        }
        String[] array = new String[collection.size()];
        return join(collection.toArray(array), separator);
    }

    public static String join(String[] array, String separator) {
        if (array == null) {
            return null;
        }
        if (array.length <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(array[i]);
        }
        return sb.toString();
    }

    public static String[] split(String str, String delim) {
        StringTokenizer stringTokenizer = new StringTokenizer(str, delim);
        String[] strings = new String[stringTokenizer.countTokens()];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = stringTokenizer.nextToken();
        }
        return strings;
    }
}
