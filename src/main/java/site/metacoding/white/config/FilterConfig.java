package site.metacoding.white.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.metacoding.white.config.auth.JwtAuthenticationFilter;
import site.metacoding.white.config.auth.JwtAuthorizationFilter;
import site.metacoding.white.domain.UserRepository;

@Slf4j
@Configuration // IOC에 등록이 됨
@RequiredArgsConstructor
public class FilterConfig { // di실제로는 여기서함

    private final UserRepository userRepository;// DI(스프링IOC컨터이너에서 옴)

    @Bean // IOC에등록이됨(서버실행시)
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegister() {
        log.debug("디버그 : 인증 필터 등록");// 토큰생성
        FilterRegistrationBean<JwtAuthenticationFilter> bean = new FilterRegistrationBean<>(
                new JwtAuthenticationFilter(userRepository));// 실제로 DI되서 여기 들어옴
        bean.addUrlPatterns("/login");
        bean.setOrder(1);// 낮은 순서대로 실행(숫자)
        return bean;
        // jwt토큰을 만들어줘야함! /아이디.유저네임.패스워드
        // / login일때 아이디.유저네임.패스워드 확인
        // 토큰을 만들어줌
    }

    @Profile("dev")
    @Bean
    public FilterRegistrationBean<JwtAuthorizationFilter> jwtAuthorizationFilterRegister() {
        log.debug("디버그 : 인가 필터 등록");
        FilterRegistrationBean<JwtAuthorizationFilter> bean = new FilterRegistrationBean<>(
                new JwtAuthorizationFilter());
        bean.addUrlPatterns("/s/*"); // 원래 두개인데, 이 친구만 예외
        bean.setOrder(2);
        return bean;
    }

    // 추가적인 필터 등록시
}
