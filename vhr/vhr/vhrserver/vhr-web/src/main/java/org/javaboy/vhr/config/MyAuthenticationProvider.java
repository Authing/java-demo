package org.javaboy.vhr.config;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.dto.LoginTokenRespDto;
import cn.authing.sdk.java.dto.SignInOptionsDto;
import cn.authing.sdk.java.util.JsonUtils;
import org.javaboy.vhr.service.HrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
//@Component
public class MyAuthenticationProvider extends DaoAuthenticationProvider {
    @Autowired
    AuthenticationClient authenticationClient;
    @Autowired
    HrService hrService;

    public MyAuthenticationProvider() {
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        String username = (String) authentication.getPrincipal();
        String password = (String) authentication.getCredentials();
        LoginTokenRespDto respDto = authenticationClient.signInByUsernamePassword(username, password, new SignInOptionsDto());
        if(respDto.getStatusCode() != 200){
            throw new BadCredentialsException(respDto.getMessage());
        }
//        super.additionalAuthenticationChecks(userDetails, authentication);
    }

}
