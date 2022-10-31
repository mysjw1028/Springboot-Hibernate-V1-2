package site.metacoding.white.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import site.metacoding.white.config.auth.JwtAuthenticationFilter;
import site.metacoding.white.domain.UserRepository;

@Slf4j
@Configuration // IOC에 등록이 됨
@RequiredArgsConstructor
public class FilterConfig { // di실제로는 여기서함

    private final UserRepository userRepository;// DI(스프링IOC컨터이너에서 옴)

    @Bean // IOC에등록이됨(서버실행시)
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegister() {
        log.debug("디버그 : 인증 필터 등록");
        FilterRegistrationBean<JwtAuthenticationFilter> bean = new FilterRegistrationBean<>(
                new JwtAuthenticationFilter(userRepository));// 실제로 DI되서 여기 들어옴
        bean.addUrlPatterns("/login");
        return bean;
        // jwt토큰을 만들어줘야함! /아이디.유저네임.패스워드
        // / login일때 아이디.유저네임.패스워드 확인
        // 토큰을 만들어줌
    }

    // 추가적인 필터 등록시
}
