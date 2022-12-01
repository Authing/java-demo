package org.javaboy.vhr.config;

import cn.authing.sdk.java.client.AuthenticationClient;
import cn.authing.sdk.java.util.JsonUtils;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.javaboy.vhr.model.CurrentUserAccessToken;
import org.javaboy.vhr.model.Hr;
import org.javaboy.vhr.model.RespBean;
import org.javaboy.vhr.service.HrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.session.ConcurrentSessionFilter;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * @作者 江南一点雨
 * @公众号 江南一点雨
 * @微信号 a_java_boy
 * @GitHub https://github.com/lenve
 * @博客 http://wangsong.blog.csdn.net
 * @网站 http://www.javaboy.org
 */
@Configuration
@EnableWebSecurity(debug = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    HrService hrService;
    @Autowired
    CustomFilterInvocationSecurityMetadataSource customFilterInvocationSecurityMetadataSource;
    @Autowired
    CustomUrlDecisionManager customUrlDecisionManager;
    @Autowired
    AuthenticationClient authenticationClient;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 自定义provider
     * @return
     */
    @Bean
    public MyAuthenticationProvider myAuthenticationProvider() {
        MyAuthenticationProvider myAuthenticationProvider = new MyAuthenticationProvider();
        //设置passorderEncoder
        myAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        //设置UserDetailsService
        myAuthenticationProvider.setUserDetailsService(hrService);
        return myAuthenticationProvider;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 注释，加上会调用默认的 DaoAuthenticationProvider，导致 myAuthenticationProvider 抛出的异常不被捕获
//        auth.userDetailsService(hrService);
        auth.authenticationProvider(myAuthenticationProvider());
    }

    /**
     * 重写父类自定义AuthenticationManager 将provider注入进去
     * 当然我们也可以考虑不重写 在父类的manager里面注入provider
     * @return
     */
    @Override
    protected AuthenticationManager authenticationManager(){
        ProviderManager manager = new ProviderManager(Arrays.asList(myAuthenticationProvider()));
        return manager;
    }

    // 放行的请求
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/css/**", "/js/**", "/index.html", "/img/**", "/fonts/**", "/favicon.ico", "/verifyCode");
    }

    @Bean
    LoginFilter loginFilter() throws Exception {
        LoginFilter loginFilter = new LoginFilter();
        loginFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
            response.setContentType("application/json;charset=utf-8");
            PrintWriter out = response.getWriter();
            // 获取 loadUserByUserName 中返回的 hr 对象，返回给前端
            Hr hr = (Hr) authentication.getPrincipal();
            hr.setPassword(null);
            // 获取 ThreadLocal 中的 accessToken
            hr.setAccessToken(CurrentUserAccessToken.getAccessToken());
            RespBean ok = RespBean.ok("登录成功!", hr);
            String s = new ObjectMapper().writeValueAsString(ok);
            out.write(s);
            out.flush();
            out.close();
            // 清除 ThreadLocal 中的 accessToken
            CurrentUserAccessToken.removeAccessToken();
            }
        );
        loginFilter.setAuthenticationFailureHandler((request, response, exception) -> {
                    response.setContentType("application/json;charset=utf-8");
                    PrintWriter out = response.getWriter();
                    RespBean respBean = RespBean.error(exception.getMessage());
                    // 改用 authing 返回的提示信息
//                    if (exception instanceof LockedException) {
//                        respBean.setMsg("账户被锁定，请联系管理员!");
//                    } else if (exception instanceof CredentialsExpiredException) {
//                        respBean.setMsg("密码过期，请联系管理员!");
//                    } else if (exception instanceof AccountExpiredException) {
//                        respBean.setMsg("账户过期，请联系管理员!");
//                    } else if (exception instanceof DisabledException) {
//                        respBean.setMsg("账户被禁用，请联系管理员!");
//                    } else if (exception instanceof BadCredentialsException) {
//                        respBean.setMsg("用户名或者密码输入错误，请重新输入!");
//                    }
                    out.write(new ObjectMapper().writeValueAsString(respBean));
                    out.flush();
                    out.close();
                }
        );
        // set 新的 AuthenticationManager 对象
        loginFilter.setAuthenticationManager(authenticationManagerBean());
        // 登录接口
        loginFilter.setFilterProcessesUrl("/doLogin");
        ConcurrentSessionControlAuthenticationStrategy sessionStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
        sessionStrategy.setMaximumSessions(1);
        loginFilter.setSessionAuthenticationStrategy(sessionStrategy);
        return loginFilter;
    }

    @Bean
    SessionRegistryImpl sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O object) {
                        object.setAccessDecisionManager(customUrlDecisionManager);
                        object.setSecurityMetadataSource(customFilterInvocationSecurityMetadataSource);
                        return object;
                    }
                })
                .and()
                .logout()
                .logoutUrl("/logout")
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((req, resp, authentication) -> {
                            // 添加自定义逻辑，撤销 authing accessToken
                            if(StrUtil.isNotBlank(req.getParameter("accessToken"))) {
                                authenticationClient.revokeToken(req.getParameter("accessToken"));
                            }
                            resp.setContentType("application/json;charset=utf-8");
                            PrintWriter out = resp.getWriter();
                            out.write(new ObjectMapper().writeValueAsString(RespBean.ok("注销成功!")));
                            out.flush();
                            out.close();
                        }
                )
                .permitAll()
                .and()
                .csrf().disable().exceptionHandling()
                //没有认证时，在这里处理结果，不要重定向
                .authenticationEntryPoint((req, resp, authException) -> {
                            resp.setContentType("application/json;charset=utf-8");
                            resp.setStatus(401);
                            PrintWriter out = resp.getWriter();
                            RespBean respBean = RespBean.error("访问失败!");
                            if (authException instanceof InsufficientAuthenticationException) {
                                respBean.setMsg("请求失败，请联系管理员!");
                            }
                            out.write(new ObjectMapper().writeValueAsString(respBean));
                            out.flush();
                            out.close();
                        }
                );
        http.addFilterAt(new ConcurrentSessionFilter(sessionRegistry(), event -> {
                HttpServletResponse resp = event.getResponse();
                resp.setContentType("application/json;charset=utf-8");
                resp.setStatus(401);
                PrintWriter out = resp.getWriter();
                out.write(new ObjectMapper().writeValueAsString(RespBean.error("您已在另一台设备登录，本次登录已下线!")));
                out.flush();
                out.close();
        }), ConcurrentSessionFilter.class);
        // 自定义 filter 替换 UsernamePasswordAuthenticationFilter
        http.addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
