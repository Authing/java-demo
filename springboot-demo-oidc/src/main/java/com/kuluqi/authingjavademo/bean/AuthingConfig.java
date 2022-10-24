package com.kuluqi.authingjavademo.bean;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.model.AuthenticationClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.IOException;
import java.text.ParseException;

@Configuration
public class AuthingConfig {
    @Bean
    @Scope(value = "prototype")
    public AuthenticationClient authenticationClient() throws IOException, ParseException {
        //在构造函数中分别填入自己的 App ID、App Secret、APP Host和之前设置的回调地址。
        AuthenticationClientOptions options = new AuthenticationClientOptions();
        options.setAppId("YOUR_APP_ID");
        options.setAppSecret("YOUR_APP_SECRET");
        options.setAppHost("YOUR_APP_HOST");
        AuthenticationClient client = new AuthenticationClient(options);
        return client;
    }
}
