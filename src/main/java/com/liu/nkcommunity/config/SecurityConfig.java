package com.liu.nkcommunity.config;

import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: lms
 * @date: 2022-04-28 22:51
 */
//@Configuration
@Deprecated
//public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {
public class SecurityConfig {

    /**
     * 忽略掉对静态资源的拦截
     *
     * @param web
     * @throws Exception
     */
//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        web.ignoring().antMatchers("/resources/**");
//    }


//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        // 授权
//        http.authorizeRequests()
//                .antMatchers(  // 设置哪些访问路径需要权限(必须登录之后才可以访问)
//                        "/user/setting",
//                        "/user/upload",
//                        "/discuss/add",
//                        "/comment/add/**",
//                        "/letter/**",
//                        "/notice/**",
//                        "/like",
//                        "/follow",
//                        "/unfollow"
//                )
//                .hasAnyAuthority( // 只需要有下面的任何一个角色权限都可以访问上面的路径
//                        AUTHORITY_ADMIN,
//                        AUTHORITY_USER,
//                        AUTHORITY_MODERATOR
//                )
//                // 其他的请求任何权限都可以访问
//                .anyRequest().permitAll()
//                // 关闭csrf
//                .and().csrf().disable();
//
//
//        // 权限不够时的处理
//        http.exceptionHandling()
//                .authenticationEntryPoint(new AuthenticationEntryPoint() {
//                    // 没有登录
//                    @Override
//                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
//                        String xRequestedWith = request.getHeader("x-requested-with");
//                        // 判断是普通请求还是异步请求，进行不同的处理
//                        if ("XMLHttpRequest".equals(xRequestedWith)) {
//                            response.setContentType("application/plain;charset=utf-8");
//                            PrintWriter writer = response.getWriter();
//                            writer.write(CommunityUtil.getJSONString(403, "你还没有登录哦!"));
//                        } else {
//                            response.sendRedirect(request.getContextPath() + "/login");
//                        }
//                    }
//                })
//                .accessDeniedHandler(new AccessDeniedHandler() {
//                    // 权限不足
//                    @Override
//                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
//                        String xRequestedWith = request.getHeader("x-requested-with");
//                        if ("XMLHttpRequest".equals(xRequestedWith)) {
//                            response.setContentType("application/plain;charset=utf-8");
//                            PrintWriter writer = response.getWriter();
//                            writer.write(CommunityUtil.getJSONString(403, "你没有访问此功能的权限!"));
//                        } else {
//                            response.sendRedirect(request.getContextPath() + "/denied");
//                        }
//                    }
//                });
//        /**
//         * 为了执行我们自己设置的logout页面
//         * security底层会默认的去拦截logout请求，进行退出的操作
//         * 因此，我们需要覆盖它默认的执行逻辑，这样子才能让security去执行我们设置
//         * 的退出代码，此处只是一个欺骗请求，程序当中并没有securityLogout请求，
//         * 所以拦截到这个路径并不会执行相应的处理
//         */
//        http.logout().logoutUrl("/securityLogout");

//    }
}
