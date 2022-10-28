package com.example.yin.config;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.model.AuthenticationClientOptions;
import cn.authing.sdk.java.model.ManagementClientOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.text.ParseException;

/**
 * 注入 authing 的用户认证模块和管理模块类
 */
@Configuration
public class AuthingConfig {
    @Value("${authing.config.appId}")
    String AUTHING_APP_ID;

    @Value("${authing.config.appSecret}")
    String AUTHING_APP_SECRET;

    @Value("${authing.config.appHost}")
    String AUTHING_APP_HOST;

    @Value("${authing.config.userPoolId}")
    String AUTHING_USERPOOL_ID;

    @Value("${authing.config.userPoolSecret}")
    String AUTHING_USERPOOL_SECRET;

    @Bean
    @Scope("prototype")
    public AuthenticationClient authenticationClient() throws IOException, ParseException {
        //在构造函数中分别填入自己的 App ID、App Secret、APP Host。
        AuthenticationClientOptions options = new AuthenticationClientOptions();
        options.setAppId(AUTHING_APP_ID);
        options.setAppSecret(AUTHING_APP_SECRET);
        options.setAppHost(AUTHING_APP_HOST);
        AuthenticationClient client = new AuthenticationClient(options);
        return client;
    }

    @Bean
    @Scope("prototype")
    public ManagementClient managementClient() {
        //在构造函数中分别填入自己的 用户池 ID、用户池 Secret、APP Host。
        ManagementClientOptions options = new ManagementClientOptions();
        options.setAccessKeyId(AUTHING_USERPOOL_ID);
        options.setAccessKeySecret(AUTHING_USERPOOL_SECRET);
        options.setHost(AUTHING_APP_HOST);
        ManagementClient client = new ManagementClient(options);
        return client;
    }

}
