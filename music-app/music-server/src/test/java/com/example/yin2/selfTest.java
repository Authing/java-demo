package com.example.yin2;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.authing.sdk.java.dto.authentication.*;
import cn.authing.sdk.java.enums.ProtocolEnum;
import cn.authing.sdk.java.model.AuthenticationClientOptions;
import cn.authing.sdk.java.model.ClientCredentialInput;
import cn.authing.sdk.java.model.ManagementClientOptions;
import cn.authing.sdk.java.util.JsonUtils;
import com.example.yin2.domain.CustomData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class selfTest {

    @Autowired
    ManagementClient managementClient;

    @Autowired
    AuthenticationClient authenticationClient;

    @Test
    public void testAuthenticationClient() throws IOException, ParseException {
        AuthenticationClientOptions options = new AuthenticationClientOptions();
        options.setAppId("634521175dc75ced7033ccdb");
        options.setAppHost("https://kuluqi3.authing.cn");
        options.setAppSecret("1231231231");
        AuthenticationClient authenticationClient = new AuthenticationClient(options);
    }
    @Test
    public void testManagementClient(){

        IOidcParams iOidcParams = new IOidcParams();
        authenticationClient.buildAuthorizeUrl(iOidcParams);
    }

    @Test
    public void testPath(){
        System.out.println(System.getProperty("user.dir"));
    }

}
