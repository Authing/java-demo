package com.kuluqi.authingjavademo.controller;


import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.dto.authentication.*;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpSession;


@Controller
public class DemoController {

    public static final String KEY_ACCESS_TOKEN = "access_token";
    public static final String KEY_ID_TOKEN = "id_token";
    public static final String KEY_REFRESH_TOKEN = "refresh_token";

    private final HttpSession session;
    private final ObjectFactory<AuthenticationClient> authClientFactory;

    public DemoController(HttpSession session, ObjectFactory<AuthenticationClient> authClientFactory) {
        this.session = session;
        this.authClientFactory = authClientFactory;
    }

    // 在这里添加路由方法……
    @GetMapping("/login")
    public String login() {
        BuildAuthUrlParams params = new BuildAuthUrlParams();
        String redirectUri = authClientFactory.getObject().getOptions().getRedirectUri();
        params.setRedirectUri(redirectUri);
        return "redirect:" + authClientFactory.getObject().buildAuthUrl(params).getUrl();
    }

    @GetMapping("/callback")
    @SuppressWarnings("unchecked")
    public String callback(@RequestParam String code) throws Exception {
        OIDCTokenResponse oidcTokenResponse = authClientFactory.getObject().getAccessTokenByCode(code);

        session.setAttribute(KEY_ACCESS_TOKEN, oidcTokenResponse.getAccessToken());
        session.setAttribute(KEY_ID_TOKEN, oidcTokenResponse.getIdToken());
        session.setAttribute(KEY_REFRESH_TOKEN, oidcTokenResponse.getRefreshToken());

        return "redirect:/profile";
    }

    @GetMapping("/logout")
    public String logout() {
        String idToken = (String) session.getAttribute(KEY_ID_TOKEN);
        // 用户当前未登录
        if(idToken == null) {
            return "redirect:/profile";
        }

        //清空 session
        session.invalidate();

        ILogoutParams params = new ILogoutParams();
        // 设置登出回调
        params.setPostLogoutRedirectUri("http://localhost:8080/profile");

        return "redirect:" + authClientFactory.getObject().buildLogoutUrlWithHost(params);
    }

    @ResponseBody
    @GetMapping("/profile")
    public String profile() {
        String accessToken = (String) session.getAttribute(KEY_ACCESS_TOKEN);
        if(accessToken == null) {
            return "未登录";
        }
        return authClientFactory.getObject().getUserinfo(accessToken).toString();
    }

}
