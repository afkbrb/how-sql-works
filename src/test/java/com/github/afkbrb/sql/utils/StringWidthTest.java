package com.github.afkbrb.sql.utils;

import org.junit.Assert;
import org.junit.Test;

import static com.github.afkbrb.sql.utils.StringWidth.stringWidth;

public class StringWidthTest {

    @Test
    public void test() {
        Assert.assertEquals(2, stringWidth("az"));
        Assert.assertEquals(2, stringWidth("09"));
        Assert.assertEquals(2, stringWidth("AZ"));
        Assert.assertEquals(13, stringWidth("how-sql-works"));
        Assert.assertEquals(8, stringWidth("中文宽度"));
        Assert.assertEquals(17, stringWidth("哈哈哈 something "));
        Assert.assertEquals(4, stringWidth("😂🙃"));
    }
}
