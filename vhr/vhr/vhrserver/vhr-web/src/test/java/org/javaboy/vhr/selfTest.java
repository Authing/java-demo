package org.javaboy.vhr;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.client.ManagementClient;
import cn.authing.sdk.java.dto.*;
import cn.authing.sdk.java.model.AuthenticationClientOptions;
import cn.authing.sdk.java.model.ManagementClientOptions;
import cn.authing.sdk.java.util.JsonUtils;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import org.javaboy.vhr.converter.AuthingUserEmployeeConverter;
import org.javaboy.vhr.mapper.MenuMapper;
import org.javaboy.vhr.model.Menu;
import org.javaboy.vhr.service.MenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class selfTest {

    @Autowired
    MenuService menuService;

    @Autowired
    MenuMapper menuMapper;

    @Autowired
    ManagementClient managementClient;

    @Autowired
    AuthenticationClient authenticationClient;

    @Value("${authing.config.appId}")
    String namespace;

    @Autowired
    AuthingUserEmployeeConverter authingUserEmployeeConverter;

    @Test
    public void testAuth() throws IOException, ParseException {
        SignInOptionsDto signInOptionsDto = new SignInOptionsDto();
        signInOptionsDto.setScope("openid phone email");
        LoginTokenRespDto respDto = authenticationClient.signInByUsernamePassword("vhr_admin", "123456", signInOptionsDto);
        System.out.println("accessToken:"+respDto.getData().getAccessToken());
        System.out.println("idToken:"+respDto.getData().getIdToken());
    }

    @Test
    public void testMag(){
        CreateRolesBatch reqDto = new CreateRolesBatch();
        List<RoleListItem> list = new ArrayList<>();
        RoleListItem roleListItem = new RoleListItem();
        roleListItem.setNamespace(namespace);
        roleListItem.setCode("kuluqi1");
        roleListItem.setDescription("dd");
        RoleListItem roleListItem1 = new RoleListItem();
        roleListItem1.setNamespace(namespace);
        roleListItem1.setCode("kuluqi1");
        roleListItem1.setDescription("dd");
        list.add(roleListItem);
        list.add(roleListItem1);
        reqDto.setList(list);
        IsSuccessRespDto rolesBatch = managementClient.createRolesBatch(reqDto);
    }


    // 执行批量创建用户
    @Test
    public void testNormal(){
        authingUserEmployeeConverter.batchCreateAuthingUser();
    }

    @Test
    public void jwt(){
        String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2MzhhZGU0OWI4ZDgzNWUyOTk1YTc0OTEiLCJhdWQiOiI2MzljMWRhYzZiN2ZmMGQyMWEzZWE1NGMiLCJpYXQiOjE2NzE2MzM2NjYsImV4cCI6MTY3Mjg0MzI2NiwiaXNzIjoiaHR0cHM6Ly9sbGxsbC5hdXRoaW5nLmNuL29pZGMiLCJuYW1lIjpudWxsLCJnaXZlbl9uYW1lIjpudWxsLCJtaWRkbGVfbmFtZSI6bnVsbCwiZmFtaWx5X25hbWUiOm51bGwsIm5pY2tuYW1lIjpudWxsLCJwcmVmZXJyZWRfdXNlcm5hbWUiOm51bGwsInByb2ZpbGUiOm51bGwsInBpY3R1cmUiOiJodHRwczovL2ZpbGVzLmF1dGhpbmcuY28vYXV0aGluZy1jb25zb2xlL2RlZmF1bHQtdXNlci1hdmF0YXIucG5nIiwid2Vic2l0ZSI6bnVsbCwiYmlydGhkYXRlIjpudWxsLCJnZW5kZXIiOiJVIiwiem9uZWluZm8iOm51bGwsImxvY2FsZSI6bnVsbCwidXBkYXRlZF9hdCI6IjIwMjItMTItMjFUMTQ6NDE6MDYuMjk2WiJ9.8a55g9CrQrxeBtjy9zWPVE4XBsbJcC6GzTyEfYvc9e8";
        try {
            Algorithm algorithm = Algorithm.HMAC256("7750c493f536688ca0d60a69d907a657");
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("auth0")
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
            System.out.println(jwt.getToken());
        } catch (JWTVerificationException exception){
            //Invalid signature/claims
            System.out.println("错误");
        }
    }

    @Test
    public void testParseJwt(){

        try {
            String idToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2MzdlY2M2OTJlMDU3MDQ2MWIxYWQwMjAiLCJhdWQiOiI2MzdlY2JkMjRmOTMzYmNmZDBiMGEzMWUiLCJpYXQiOjE2NzE2ODc2NTgsImV4cCI6MTY3Mjg5NzI1OCwiaXNzIjoiaHR0cHM6Ly9rdWx1cWk1LmF1dGhpbmcuY24vb2lkYyIsImVtYWlsIjpudWxsLCJlbWFpbF92ZXJpZmllZCI6ZmFsc2UsInBob25lX251bWJlciI6bnVsbCwicGhvbmVfbnVtYmVyX3ZlcmlmaWVkIjpmYWxzZX0.ODi9NtvNt17HnPWHc-18zco9vn4aO4cRPTuWmZNkzx0";
            JWTClaimsSet jwtClaimSet = JWTParser.parse(idToken).getJWTClaimsSet();
            Map<String, Object> map = jwtClaimSet.getClaims();
            System.out.println(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
