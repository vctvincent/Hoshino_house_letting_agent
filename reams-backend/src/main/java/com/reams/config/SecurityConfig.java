package com.reams.config;

import com.reams.security.JwtAuthenticationFilter;
import com.reams.security.JwtAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 配置类 (适用于 Spring Security 5.x)
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 允许访问文件上传目录下的静态资源
        String userDir = System.getProperty("user.dir");
        // Windows 系统下确保路径分隔符正确（使用正斜杠）
        String absolutePath = "file:" + userDir.replace("\\", "/") + "/uploads/";

        // 配置两处映射：
        // 1. /uploads/** - 用于前端直接访问（通过 Vite 代理）
        // 2. /api/uploads/** - 用于 API 调用（带 context-path）
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(absolutePath)
                .setCachePeriod(3600);
                
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(absolutePath)
                .setCachePeriod(3600);
    }

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    /**
     * CORS 配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许的来源（开发环境允许所有，生产环境应指定具体域名）
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173"));
        // 允许的 HTTP 方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        // 允许携带凭证（Cookie、Authorization 头等）
        configuration.setAllowCredentials(true);
        // 预检请求缓存时间（秒）
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/auth/**", "/api/common/**").permitAll()
                // 首页公开访问的接口 (添加 /api 前缀)
                .antMatchers("/api/house/list", "/api/house/detail/**", "/api/house/hot", "/api/house/cities", "/api/house/districts", "/api/house/published").permitAll()
                // 评价相关公开接口
                .antMatchers("/api/review/list", "/api/review/house/**", "/api/review/agent/**", "/api/review/agent/*/rating").permitAll()
                // 消息未读数（允许未登录访问）
                .antMatchers("/api/message/unread/count").permitAll()
                .antMatchers("/api/file/upload/**", "/uploads/**", "/api/uploads/**").permitAll()
                // WebSocket 端点（用于实时推送）
                .antMatchers("/ws/**").permitAll()
                .anyRequest().authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
