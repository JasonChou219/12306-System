package com.example.train.member.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SpringBootApplication
@ComponentScan(basePackages = "com.example.train.member")
public class MemberApplication {


    private static final Logger logger = LoggerFactory.getLogger(MemberApplication.class);
	public static void main(String[] args) {
        SpringApplication app = new SpringApplication(MemberApplication.class);
        Environment env = app.run(args).getEnvironment();
        logger.info("项目启动成功！！");
        logger.info("Application started at http://localhost:{}", env.getProperty("server.port"));
	}


}
