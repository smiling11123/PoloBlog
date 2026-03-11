package com.polo.Blog.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射路径：http://localhost:9000/travel/abc.png
        // 物理路径：D:/uploads/abc.png
        registry.addResourceHandler("/travel/**")
                .addResourceLocations("file:D:/uploads/");
    }
}

