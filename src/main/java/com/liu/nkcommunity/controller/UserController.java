package com.liu.nkcommunity.controller;

import com.liu.nkcommunity.annotation.LoginAnnotation;
import com.liu.nkcommunity.domain.User;
import com.liu.nkcommunity.service.LikeService;
import com.liu.nkcommunity.service.UserService;
import com.liu.nkcommunity.util.CommunityUtil;
import com.liu.nkcommunity.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("user")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private UserService userService;

    @Autowired
    private LikeService likeService;

    /**
     * 跳转至设置页面
     *
     * @return
     */
    @LoginAnnotation
    @GetMapping("setting")
    public String getSettingPage() {
        return "site/setting";
    }

    /**
     * 上传头像
     *
     * @param headerImage 上传的头像文件名
     * @param model
     * @return
     */
    @LoginAnnotation
    @PostMapping("upload")
    public String upload(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "site/setting";
        }
        // 获取原图像名称
        String filename = headerImage.getOriginalFilename();
        // 获取图像的后缀名
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "请选择正确的文件格式!");
            return "site/setting";
        }
        // 生成随机的文件名
        filename = CommunityUtil.generateUUID() + suffix;
        // 将图像上传到指定的位置
        File dest = new File(uploadPath + "/" + filename);
        try {
            // 存储文件信息,直接将文件进行传输发送
            headerImage.transferTo(dest);
        } catch (IOException e) {
            LOGGER.error("文件上传失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常!", e);
        }
        // 从ThreadLocal中获取当前登录的用户信息
        User user = hostHolder.getUser();
        // 更新当前用户头像的路径信息
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        // 更新用户的头像信息
        userService.updateHeader(user.getId(), headerUrl);
        // 跳转至首页
        return "redirect:/index";
    }

    /**
     * 查询用户的头像请求
     *
     * @param fileName
     * @param response
     */
    @GetMapping("header/{fileName}")
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 获取文件的真实的存放路径
        fileName = uploadPath + "/" + fileName;
        // 获取文件的后缀名
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片信息
        response.setContentType("image/" + suffix);
        // 以字节流的方式输出文件信息
        try (
                // 获取响应输出流
                OutputStream os = response.getOutputStream();
                // 输入流获取图像信息
                FileInputStream fis = new FileInputStream(fileName);) {
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
        } catch (IOException e) {
            LOGGER.error("读取文件失败: " + e.getMessage());
        }
    }


    /**
     * 更新用户的登录密码
     *
     * @param oldPwd
     * @param newPwd
     * @param againPwd
     * @param model
     * @param ticket
     * @return
     */
    @LoginAnnotation
    @PostMapping("updatePwd")
    public String updatePwd(String oldPwd, String newPwd, String againPwd, Model model, @CookieValue("ticket") String ticket) {
        if (StringUtils.isBlank(oldPwd)) {
            model.addAttribute("passwordMsg", "请输入原密码！");
            return "site/setting";
        }
        if (StringUtils.isBlank(newPwd)) {
            model.addAttribute("newPasswordMsg", "请输入新密码！");
            return "site/setting";
        }
        if (StringUtils.isBlank(againPwd)) {
            model.addAttribute("againPasswordMsg", "请输入确认密码！");
            return "site/setting";
        }
        User user = hostHolder.getUser();
        if (!user.getPassword().equals(CommunityUtil.md5(oldPwd + user.getSalt()))) {
            model.addAttribute("passwordMsg", "原密码不正确，请重新输入！");
            return "site/setting";
        }
        if (!newPwd.equals(againPwd)) {
            model.addAttribute("againPasswordMsg", "两次密码不相同，请重新输入！");
            return "site/setting";
        }
        // 更新密码
        userService.updatePassword(user.getId(), newPwd);
        // 退出登录
        userService.logout(ticket);
        // 退出后重定向到登陆页面
        return "redirect:/login";
    }

    /**
     * 跳转到指定用户的首页
     * @param userId
     * @param model
     * @return
     */
    @GetMapping("/profile/{userId}")
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.selectById(userId);
        if (user == null){
            throw new RuntimeException("该用户不存在!");
        }
        model.addAttribute("user", user);
        // 查询指定userId的用户的点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);
        return "site/profile";
    }
}












