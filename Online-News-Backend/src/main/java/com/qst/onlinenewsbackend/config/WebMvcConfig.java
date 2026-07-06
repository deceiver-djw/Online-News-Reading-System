package com.qst.onlinenewsbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 将本地图片目录映射为HTTP可访问的静态资源
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${qianfan.image-save-path}")
    private String imageSavePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保路径以 file:/// 和 / 结尾
        String location = imageSavePath.replace("\\", "/");
        if (!location.startsWith("file:///")) {
            location = "file:///" + location;
        }
        if (!location.endsWith("/")) {
            location = location + "/";
        }

        registry.addResourceHandler("/images/**")
                .addResourceLocations(location);
    }
}
