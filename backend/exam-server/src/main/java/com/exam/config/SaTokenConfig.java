package com.exam.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 权限认证配置
 * <p>
 * 拦截所有 /** 请求，对需要登录的路径进行认证校验。
 * 白名单路径（如登录、注册、文档等）不需要登录即可访问。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 根据路由进行权限校验
                    SaRouter
                            // 排除不需要登录的路径
                            .notMatch(
                                    "/api/v1/auth/**",      // 登录注册相关
                                    "/doc.html",             // Knife4j 文档
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/webjars/**",
                                    "/favicon.ico",
                                    "/error"
                            )
                            // 所有其他路径需要登录
                            .check(r -> StpUtil.checkLogin());

                    // 管理端接口需要管理员角色
                    SaRouter.match("/api/v1/admin/**")
                            .check(r -> StpUtil.checkRoleOr("admin", "teacher"));

                    // 用户管理仅限管理员
                    SaRouter.match("/api/v1/admin/users/**")
                            .check(r -> StpUtil.checkRoleOr("admin"));

                })).addPathPatterns("/**");
    }
}
