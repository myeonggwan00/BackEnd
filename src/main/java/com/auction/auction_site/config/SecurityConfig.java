package com.auction.auction_site.config;

import com.auction.auction_site.security.spring_security.CustomAuthenticationEntryPoint;
import com.auction.auction_site.repository.RefreshTokenRepository;
import com.auction.auction_site.security.jwt.JWTFilter;
import com.auction.auction_site.security.jwt.JWTUtil;
import com.auction.auction_site.security.oauth.CustomOAuth2UserService;
import com.auction.auction_site.security.oauth.CustomSuccessHandler;
import com.auction.auction_site.security.spring_security.CustomJsonLoginFilter;
import com.auction.auction_site.security.spring_security.CustomLogoutFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

/**
 * 스프링 시큐리티 설정
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ObjectMapper objectMapper;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.formLogin((formLogin) -> formLogin.disable());

        httpSecurity.httpBasic((httpBasic) -> httpBasic.disable());

        httpSecurity.csrf((auth) -> auth.disable());

        httpSecurity.addFilterBefore(new CustomJsonLoginFilter(
                authenticationManager(authenticationConfiguration), objectMapper, jwtUtil, refreshTokenRepository),
                UsernamePasswordAuthenticationFilter.class);

        httpSecurity.addFilterAfter(new JWTFilter(jwtUtil), CustomJsonLoginFilter.class);

        httpSecurity.addFilterBefore(new CustomLogoutFilter(jwtUtil, refreshTokenRepository), LogoutFilter.class);

        httpSecurity.oauth2Login(
                (oauth2) -> oauth2
                        .userInfoEndpoint(
                                userInfoEndpointConfig -> userInfoEndpointConfig.userService(customOAuth2UserService))
                        .successHandler(customSuccessHandler)
        );


        httpSecurity.authorizeHttpRequests(
                (auth) -> auth
                        .requestMatchers("/", "/oauth2/**", "/login/**", "/members",
                                "/members/email-verification", "/email-verification"
                                , "/members/id", "members/nickname", "/reissue","/pwdhelp/**").permitAll()
                        .requestMatchers("/admin").hasRole("ADMIN")
                        .anyRequest().authenticated()
        );

        // 인증 실패하거나 인증 정보가 없으면 로그인 페이지가 아닌 JSON 응답하도록 설정
        httpSecurity.exceptionHandling(exception ->
                exception.authenticationEntryPoint(customAuthenticationEntryPoint));

        httpSecurity.sessionManagement(
                (session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return httpSecurity.build();
    }
}