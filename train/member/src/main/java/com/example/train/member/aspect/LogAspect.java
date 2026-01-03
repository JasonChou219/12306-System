package com.example.train.member.aspect;

import jakarta.annotation.PostConstruct;

import java.util.UUID;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;





@Aspect
@Component
public class LogAspect {
    public LogAspect() {
        System.out.println("LogAspect constructor");
    }

    private final static Logger logger = LoggerFactory.getLogger(LogAspect.class);


    @PostConstruct
    public void init() {
        System.out.println("LogAspect init");
    }
    /* 定义切点：拦截 com.example.train.member.controller 包下的所有方法 
        所有返回值 + member的controller类
    */
    @Pointcut("execution(* com.example.train.member.controller..*(..))")
    public void controllerPointcut() {}

    @Before("controllerPointcut()")
    public void doBefore(JoinPoint joinPoint) {
        //MDC 增加自定义参数
        MDC.put("LOG_ID", UUID.randomUUID().toString());

        //打印请求日志
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        Signature signature = joinPoint.getSignature();
        String name = signature.getName();

        logger.info("------------开始打印请求日志------------");
        logger.info("请求URL: {}", request.getRequestURL().toString());
        logger.info("请求方法: {}.{}", signature.getDeclaringTypeName(), name);
        logger.info("远程地址: {}", request.getRemoteAddr());

        // 打印请求参数
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            logger.info("请求参数: {}", Arrays.toString(args));
        }   

        Object[] arguments = new Object[args.length];

        for (int i = 0; i < args.length; i++) {
            //排除一些参数类型
            if (args[i] instanceof ServletRequest 
                || args[i] instanceof ServletResponse
                || args[i] instanceof MultipartFile
            ) continue;

            arguments[i] = args[i];
        }
        // 排除字段，敏感字段或者太长的字段不显示：身份证，手机号，邮箱，密码
        // String[] excludeProperties = {"idCard", "phone", "email", "password"};
        // PropertyPreFilters filters = new PropertyPreFilters();
        // PropertyPreFilter.MySimplePropertyFilter excludefilter = filters.addFilter(0, excludeProperties);
        // excludefilter.addExcludes(excludeProperties);
        // logger.info("请求参数: {}", JSON.toJSONString(arguments, filters));

        String[] excludeProperties = {"idCard", "phone", "email", "password"};
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
        filter.getExcludes().addAll(Arrays.asList(excludeProperties));

        logger.info("请求参数: {}", JSON.toJSONString(arguments, filter));

    }

    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint proceedomgJoinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = proceedomgJoinPoint.proceed();
        


        // String[] excludeProperties = {"idCard", "phone", "email", "password"};
        // PropertyPreFilters filters = new PropertyPreFilters();
        // PropertyPreFilter.MySimplePropertyFilter excludefilter = filters.addFilter(0, excludeProperties);
        // excludefilter.addExcludes(excludeProperties);
        // logger.info("返回结果: {}", JSON.toJSONString(result, excludefilter));
        String[] excludeProperties = {"idCard", "phone", "email", "password"};
        SimplePropertyPreFilter filter = new SimplePropertyPreFilter();
        filter.getExcludes().addAll(Arrays.asList(excludeProperties));
        logger.info("返回结果: {}", JSON.toJSONString(result, filter));
        logger.info("请求结束，耗时: {}ms", System.currentTimeMillis() - startTime);
        return result;
    }
}
