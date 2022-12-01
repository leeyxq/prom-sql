package io.github.leeyxq.promsql.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;

/**
 * 断言工具类
 *
 * @author lixiangqian
 * @since 2022/11/23 08:56
 */
@UtilityClass
public class Asserts {

    public static void isTrue(boolean expression) {
        isTrue(expression, "this expression must be true");
    }

    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notNull(Object object) {
        notNull(object, "this argument is required; it must not be null");
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notBlank(String text) {
        notBlank(text, "this String argument must not blank; it must not be null or empty");
    }

    public static void notBlank(String text, String message) {
        if (StringUtil.isBlank(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Collection<?> collection) {
        notEmpty(collection, "this collection must not be empty: it must contain at least 1 element");
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if ((collection == null || collection.isEmpty())) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(String[] arr, String message) {
        if ((arr == null || arr.length == 0)) {
            throw new IllegalArgumentException(message);
        }
    }

}
