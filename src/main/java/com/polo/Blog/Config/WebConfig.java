package com.polo.Blog.Config;

import com.polo.Blog.Interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册你的拦截器
        registry.addInterceptor(new LoginInterceptor())
                .addPathPatterns("/**"

                )  // 拦截所有路径
                .excludePathPatterns(    // 排除不需要登录的路径
                        //"/login/admin",   // 登录接口不能拦截
                        //"/auth/register",// 注册接口
                        "/file/**",      // 如果有公共文件访问
                        "/doc.html",     // Swagger 文档
                        "/webjars/**"
                );
    }
}
