package com.kuluqi.authingjavademo.controller;


import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.dto.GetNewAccessTokenByRefreshTokenRespDto;
import cn.authing.sdk.java.dto.IntrospectTokenWithClientSecretPostRespDto;
import cn.authing.sdk.java.dto.authentication.*;
import cn.hutool.core.util.StrUtil;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
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
        IOidcParams iOidcParams = new IOidcParams();
        iOidcParams.setRedirectUri("http://localhost:8080/callback");
        // 此处权限要在默认权限后用空格分隔，添加
//        iOidcParams.setScope("openid profile email phone address ecs:Start");
        return "redirect:" + authClientFactory.getObject().buildAuthorizeUrl(iOidcParams);
    }

    @GetMapping("/callback")
    @SuppressWarnings("unchecked")
    public String callback(@RequestParam String code) throws Exception {
        // 注意：一个 code 只能被消费一次
        OIDCTokenResponse oidcTokenResponse = authClientFactory.getObject().getAccessTokenByCode(code);
        // 将请求回应设置到本地 session 中
        session.setAttribute(KEY_ACCESS_TOKEN, oidcTokenResponse.getAccessToken());
        session.setAttribute(KEY_ID_TOKEN, oidcTokenResponse.getIdToken());
        session.setAttribute(KEY_REFRESH_TOKEN, oidcTokenResponse.getRefreshToken());

        // 设置用户登录态
        authClientFactory.getObject().setAccessToken((String) session.getAttribute(KEY_ACCESS_TOKEN));
        return "redirect:/index";
    }

    @GetMapping("/logout")
    public String logout() {
        String idToken = (String) session.getAttribute(KEY_ID_TOKEN);
        // 用户当前未登录
        if(idToken == null) {
            return "redirect:/error";
        }

        // 撤回 accessToken
        if(StrUtil.isNotBlank((String) session.getAttribute(KEY_ACCESS_TOKEN))) {
            authClientFactory.getObject().revokeToken((String) session.getAttribute(KEY_ACCESS_TOKEN));
        }
        // 清空 session
        session.invalidate();

        BuildLogoutUrlParams params = new BuildLogoutUrlParams();
        // 设置登出回调（要与控制台保持一致）
        params.setPostLogoutRedirectUri("http://localhost:8080/error");
        params.setIdTokenHint(idToken);

        String logoutUrl = "";

        try {
            logoutUrl = authClientFactory.getObject().buildLogoutUrl(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:" + logoutUrl;
    }

    @GetMapping("/index")
    public String index() {
        String accessToken = (String) session.getAttribute(KEY_ACCESS_TOKEN);
        if(accessToken == null) {
            return "redirect:/login";
        }
        return "index.html";
    }

    @GetMapping("/error")
    public String errorPage(){
        return "error.html";
    }

    @ResponseBody
    @GetMapping("/profile")
    public String profile(){
        String accessToken = (String) session.getAttribute(KEY_ACCESS_TOKEN);
        if(StrUtil.isBlank(accessToken)) {
            return "未登录";
        }

        IntrospectTokenWithClientSecretPostRespDto res =
                authClientFactory.getObject().introspectToken(accessToken);
        if(!res.getActive()){
            return "accessToken 已失效";
        }

        return authClientFactory.getObject().getUserInfoByAccessToken(accessToken).toString();
    }

    @GetMapping("/checkScope")
    @ResponseBody
    public String checkScope(){
        String accessToken = (String) session.getAttribute(KEY_ACCESS_TOKEN);
        if(StrUtil.isBlank(accessToken)) {
            return "未登录";
        }

        IntrospectTokenWithClientSecretPostRespDto res =
                authClientFactory.getObject().introspectToken(accessToken);
        if(!res.getActive()){
            return "accessToken 已失效";
        }

        // 查找返回结果类中的权限字符串是否包含你所设定的权限
        if(StrUtil.contains(res.getScope(),"ecs:Start")){
            return "你拥有该权限";
        }else {
            return "你没有该权限";
        }
    }

    @GetMapping("/refresh")
    public String refresh(){
        String accessToken = (String) session.getAttribute(KEY_ACCESS_TOKEN);
        if(StrUtil.isBlank(accessToken)) {
            return "redirect:/error";
        }
        // 用 refreshToken 换 newAccessToken
        GetNewAccessTokenByRefreshTokenRespDto newAccessToken =
                authClientFactory.getObject().getNewAccessTokenByRefreshToken((String) session.getAttribute(KEY_REFRESH_TOKEN));

        // 重新设置 session
        session.setAttribute(KEY_ACCESS_TOKEN, newAccessToken.getAccessToken());
        session.setAttribute(KEY_ID_TOKEN, newAccessToken.getIdToken());
        session.setAttribute(KEY_REFRESH_TOKEN, newAccessToken.getRefreshToken());

        // 设置用户登录态
        authClientFactory.getObject().setAccessToken((String) session.getAttribute(KEY_ACCESS_TOKEN));

        return "redirect:/index";
    }


}
