package com.liu.nkcommunity;

import com.liu.nkcommunity.util.SensitiveFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

public class SensitiveFilterTest extends NkcommunityApplicationTests {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void test() {
        String text = "这里不可以赌博，也不可以吸毒和开票！！！";
        String filter = sensitiveFilter.filter(text);
        System.out.println("filter = " + filter);
    }

}
