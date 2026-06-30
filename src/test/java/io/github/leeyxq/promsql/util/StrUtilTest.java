package io.github.leeyxq.promsql.util;

import org.junit.Assert;
import org.junit.Test;

public class StrUtilTest {

    @Test
    public void testEscapeLabelValue() {
        Assert.assertEquals("foo\\\"bar\\\\baz", StrUtil.escapeLabelValue("foo\"bar\\baz"));
        Assert.assertEquals("", StrUtil.escapeLabelValue(null));
    }

    @Test
    public void testEscapeRegex() {
        Assert.assertEquals("cpu\\.total", StrUtil.escapeRegex("cpu.total"));
        Assert.assertEquals("a\\|b", StrUtil.escapeRegex("a|b"));
    }
}
