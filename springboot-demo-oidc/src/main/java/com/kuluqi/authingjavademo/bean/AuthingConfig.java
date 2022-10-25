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
        options.setAppId("633bdf6dec36475453151590");
        options.setAppSecret("b6a0918c9a56ab14ebdbc26a29140054");
        options.setAppHost("https://kuluqi2.authing.cn");
        AuthenticationClient client = new AuthenticationClient(options);
        return client;
    }
}
