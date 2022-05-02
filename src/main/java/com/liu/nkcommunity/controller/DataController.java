package com.liu.nkcommunity.controller;

import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.impl.DataService;
import com.liu.nkcommunity.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * @description:
 * @author: lms
 * @date: 2022-05-02 13:41
 */
@Controller
public class DataController {

    @Autowired
    private DataService dataService;

    @Autowired
    private HostHolder hostHolder;

    // 打开统计的页面
    @RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
    public String getDataPage() {
        User user = hostHolder.getUser();
        if (user == null) {
            // 跳转到登录页面
            return "redirect:/login";
        }
        if (!user.getUsername().equals("lms")) {
            return "redirect:/denied";
        }
        return "/site/admin/data";
    }

    // 统计uv
    @PostMapping("/data/uv")
    public String recordUV(@DateTimeFormat(pattern = "yyyy-MM-dd")Date start,
                           @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model) {
        long count = dataService.caculateUV(start, end);
        model.addAttribute("uvResult", count);
        model.addAttribute("uvStartDate", start);
        model.addAttribute("uvEndDate", end);
        return "forward:/data";
    }

    // 统计uv
    @PostMapping("/data/dau")
    public String recordDAU(@DateTimeFormat(pattern = "yyyy-MM-dd")Date start,
                           @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model) {
        long count = dataService.caculateDAU(start, end);
        model.addAttribute("dauResult", count);
        model.addAttribute("dauStartDate", start);
        model.addAttribute("dauEndDate", end);
        return "forward:/data";
    }








}
