package site.metacoding.white.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration // IOC에 등록이 됨
public class FilterConfig {

    @Bean // IOC에등록이됨(서버실행시)
    public FilterRegistrationBean<HelloFilter> jwtAuthenticationFilterRegister() {
        log.debug("디버그 : 인증 필터 등록");
        FilterRegistrationBean<HelloFilter> bean = new FilterRegistrationBean<>(new HelloFilter());
        bean.addUrlPatterns("/hello");
        return bean;
    }
}

@Slf4j
class HelloFilter implements Filter {// 스프링에들오기전에 실행
    // /hello 요청시
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (req.getMethod().equals("POST")) {
            log.debug("디버그 : HelloFilter 실행됨");
        } else {
            log.debug("디버그 : HelloFilter 실행됨");
        }

        // chain.doFilter(request, response);
    }// chain.doFilter(request, response);를 안걸면 통신이 종료되서 하얀화면만 나옴
     // -> 전달을 해야함!!ㄴ

}
// 필터는chain이 되어있다.