package com.liu.nkcommunity;

import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description:
 * @author: lms
 * @date: 2022-05-02 12:42
 */
public class DateTest extends NkcommunityApplicationTests{

    @Test
    public void test01() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String date = format.format(new Date());
        System.out.println("date = " + date);
    }

}
