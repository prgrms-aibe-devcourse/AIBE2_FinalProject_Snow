package com.snow.popin.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${uploadPath}")
    String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // CSS 파일들
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/")
                .setCachePeriod(0)
                .resourceChain(true);

        // JavaScript 파일들
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/")
                .setCachePeriod(0)
                .resourceChain(true);

        // 이미지 파일들
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/")
                .setCachePeriod(0);

        // 전체 static 폴더
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);

        // templates 폴더
        registry.addResourceHandler("/templates/**")
                .addResourceLocations("classpath:/static/templates/")
                .setCachePeriod(0);

        // favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico")
                .setCachePeriod(0);

        // 업로드된 파일
        String dir = uploadPath.endsWith("/") ? uploadPath : uploadPath + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + dir);
    }

    // CORS 설정 추가
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:8080", "http://127.0.0.1:8080")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // MIME 타입 문제 해결을 위해 추가
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorPathExtension(false)
                .favorParameter(false)
                .mediaType("css", MediaType.valueOf("text/css"))
                .mediaType("js", MediaType.valueOf("application/javascript"))
                .defaultContentType(MediaType.APPLICATION_JSON);
    }
}