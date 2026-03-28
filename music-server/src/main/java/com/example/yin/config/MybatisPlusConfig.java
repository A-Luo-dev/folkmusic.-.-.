package com.example.yin.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置类
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 添加分页插件
     * 这是新版MP的推荐配置方式
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 1. 定义一个 MybatisPlusInterceptor 拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        
        // 2. 向拦截器中添加一个 "分页内部拦截器"
        //    并需要指定你正在使用的数据库类型，这里假设是 MySQL
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        
        return interceptor;
    }
}