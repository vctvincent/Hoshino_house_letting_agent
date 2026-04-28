package com.reams;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 房屋中介管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.reams.mapper")
public class ReamsApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReamsApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  房屋中介管理系统启动成功!");
        System.out.println("  API文档: http://localhost:8080/api");
        System.out.println("=================================================");
    }
}
