package com.exam;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 在线考试培训系统 - 启动入口
 */
@SpringBootApplication
public class ExamApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamApplication.class, args);
        System.out.println("========================================");
        System.out.println("  在线考试培训系统启动成功！");
        System.out.println("  API 文档: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
